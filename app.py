from flask import Flask, render_template, request, redirect, url_for, flash, send_from_directory, jsonify, session
from flask_login import LoginManager, UserMixin, login_user, login_required, logout_user, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename
import os
from datetime import datetime, timedelta
import re
import sqlite3
from functools import wraps

app = Flask(__name__)
app.config['SECRET_KEY'] = os.urandom(24)
app.config['UPLOAD_FOLDER'] = os.path.join(os.path.expanduser('~'), 'cloud_storage')
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024 * 1024  # 16GB max file size
app.config['PERMANENT_SESSION_LIFETIME'] = timedelta(minutes=30)  # Session timeout
app.config['DATABASE'] = 'users.db'

# Initialize database
def init_db():
    conn = sqlite3.connect(app.config['DATABASE'])
    c = conn.cursor()
    c.execute('''CREATE TABLE IF NOT EXISTS users
                 (id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT UNIQUE NOT NULL,
                  password TEXT NOT NULL,
                  email TEXT UNIQUE NOT NULL,
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)''')
    c.execute('''CREATE TABLE IF NOT EXISTS files
                 (id INTEGER PRIMARY KEY AUTOINCREMENT,
                  filename TEXT NOT NULL,
                  user_id INTEGER NOT NULL,
                  path TEXT NOT NULL,
                  size INTEGER NOT NULL,
                  uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (user_id) REFERENCES users (id))''')
    conn.commit()
    conn.close()

def get_db():
    conn = sqlite3.connect(app.config['DATABASE'])
    conn.row_factory = sqlite3.Row
    return conn

def validate_password(password):
    if len(password) < 8:
        return False, "Password must be at least 8 characters long"
    if not re.search(r"[A-Z]", password):
        return False, "Password must contain at least one uppercase letter"
    if not re.search(r"[a-z]", password):
        return False, "Password must contain at least one lowercase letter"
    if not re.search(r"\d", password):
        return False, "Password must contain at least one number"
    if not re.search(r"[!@#$%^&*(),.?\":{}|<>]", password):
        return False, "Password must contain at least one special character"
    return True, "Password is valid"

# Ensure upload directory exists and is accessible
try:
    os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
    test_file = os.path.join(app.config['UPLOAD_FOLDER'], '.test_write')
    with open(test_file, 'w') as f:
        f.write('test')
    os.remove(test_file)
except Exception as e:
    print(f"Error accessing storage directory: {str(e)}")
    print("Please ensure you have write permissions in your user directory")
    exit(1)

# Initialize database
init_db()

login_manager = LoginManager()
login_manager.init_app(app)
login_manager.login_view = 'login'

class User(UserMixin):
    def __init__(self, id, username, email):
        self.id = id
        self.username = username
        self.email = email

@login_manager.user_loader
def load_user(user_id):
    db = get_db()
    user = db.execute('SELECT * FROM users WHERE id = ?', (user_id,)).fetchone()
    if user:
        return User(user['id'], user['username'], user['email'])
    return None

@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        email = request.form.get('email')
        
        # Validate password
        is_valid, message = validate_password(password)
        if not is_valid:
            flash(message)
            return redirect(url_for('register'))
        
        try:
            db = get_db()
            db.execute('INSERT INTO users (username, password, email) VALUES (?, ?, ?)',
                      (username, generate_password_hash(password), email))
            db.commit()
            flash('Registration successful! Please login.')
            return redirect(url_for('login'))
        except sqlite3.IntegrityError:
            flash('Username or email already exists')
            return redirect(url_for('register'))
    return render_template('register.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        remember = request.form.get('remember', False)
        
        db = get_db()
        user = db.execute('SELECT * FROM users WHERE username = ?', (username,)).fetchone()
        
        if user and check_password_hash(user['password'], password):
            user_obj = User(user['id'], user['username'], user['email'])
            login_user(user_obj, remember=remember)
            session.permanent = True
            return redirect(url_for('index'))
        flash('Invalid username or password')
    return render_template('login.html')

@app.route('/')
@login_required
def index():
    db = get_db()
    files = db.execute('''
        SELECT filename, size, uploaded_at as date
        FROM files
        WHERE user_id = ?
        ORDER BY uploaded_at DESC
    ''', (current_user.id,)).fetchall()
    
    # Convert date strings to datetime objects
    files = [dict(file) for file in files]
    for file in files:
        file['date'] = datetime.strptime(file['date'], '%Y-%m-%d %H:%M:%S')
    
    return render_template('index.html', files=files)

@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('login'))

@app.route('/upload', methods=['POST'])
@login_required
def upload_file():
    if 'file' not in request.files:
        return jsonify({'success': False, 'message': 'No file part'})
    
    files = request.files.getlist('file')
    if not files or files[0].filename == '':
        return jsonify({'success': False, 'message': 'No selected file'})
    
    db = get_db()
    for file in files:
        if file:
            filename = secure_filename(file.filename)
            file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            file.save(file_path)
            
            # Save file info to database
            db.execute('''
                INSERT INTO files (filename, user_id, path, size)
                VALUES (?, ?, ?, ?)
            ''', (filename, current_user.id, file_path, os.path.getsize(file_path)))
    
    db.commit()
    return jsonify({'success': True, 'message': 'Files uploaded successfully'})

@app.route('/download/<filename>')
@login_required
def download_file(filename):
    db = get_db()
    file = db.execute('''
        SELECT path FROM files
        WHERE filename = ? AND user_id = ?
    ''', (filename, current_user.id)).fetchone()
    
    if file:
        return send_from_directory(app.config['UPLOAD_FOLDER'], filename, as_attachment=True)
    flash('File not found')
    return redirect(url_for('index'))

@app.route('/delete/<filename>')
@login_required
def delete_file(filename):
    try:
        db = get_db()
        file = db.execute('''
            SELECT path FROM files
            WHERE filename = ? AND user_id = ?
        ''', (filename, current_user.id)).fetchone()
        
        if file:
            os.remove(file['path'])
            db.execute('DELETE FROM files WHERE filename = ? AND user_id = ?', (filename, current_user.id))
            db.commit()
            flash('File deleted successfully')
        else:
            flash('File not found')
    except Exception as e:
        flash(f'Error deleting file: {str(e)}')
    return redirect(url_for('index'))

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True) 