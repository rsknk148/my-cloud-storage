@echo off
echo Installing required packages...
python -m pip install Flask==3.0.2 Flask-Login==0.6.3 Flask-SQLAlchemy==3.1.1 Flask-WTF==1.2.1 Werkzeug==3.0.1 python-dotenv==1.0.1 bcrypt==4.1.2
 
echo Starting the application...
python app.py 