# Personal Cloud Storage

A simple, secure personal cloud storage solution that uses your F: drive for storage, allowing you to store and access your files from anywhere via web browser or Android app.

## Features

- Secure login system with user registration
- File upload and download
- File management (view, download, delete)
- Modern, responsive web interface
- **NEW: Android companion app for mobile access**
- No file size limit (configurable)
- Uses F: drive for storage

## Components

### Web Application (Flask)
- Full-featured web interface accessible from any browser
- Drag & drop file uploads
- Search functionality for files
- Session-based authentication

### Android Application
- Native Android app for mobile file management
- Material Design interface
- Upload files from your Android device
- Download files to your device's Downloads folder
- Secure authentication with your existing account

## Prerequisites

1. Ensure your F: drive is accessible and mounted
2. Make sure you have write permissions on the F: drive
3. Python 3.8 or higher installed

## Setup

### Web Application

1. Install the required dependencies:
   ```bash
   pip install -r requirements.txt
   ```
2. Run the application:
   ```bash
   python app.py
   ```
3. Access the application at `http://localhost:5000`

### Android Application

1. Open the `android-app` folder in Android Studio
2. Let Android Studio download dependencies
3. Connect an Android device or start an emulator
4. Build and run the project
5. Configure server URL to point to your Flask backend

See `android-app/README.md` for detailed Android setup instructions.

## Default Login Credentials

- Username: `admin`
- Password: `admin123`

**Important**: Change these credentials after first login for security.

## Storage Configuration

- Files are stored in `F:/cloud_storage`
- The application will automatically create this directory if it doesn't exist
- Make sure the F: drive has sufficient space for your files

## Security Notes

- The application uses Flask's built-in security features
- Files are stored on your F: drive
- Make sure to configure proper firewall rules if accessing from outside your local network
- Consider using HTTPS in production

## Customization

- To change the maximum file size, modify `MAX_CONTENT_LENGTH` in `app.py`
- To add more users, modify the `users` dictionary in `app.py`

## API Endpoints (for Android App)

The Flask backend now includes REST API endpoints for mobile app integration:

- `POST /api/login` - User authentication
- `POST /api/register` - User registration  
- `GET /api/files` - Get user's file list
- `POST /api/upload` - Upload a file
- `DELETE /api/delete/<filename>` - Delete a file
- `GET /download/<filename>` - Download a file

## Production Deployment

For production use, consider:
1. Using a proper database instead of the in-memory user storage
2. Setting up HTTPS
3. Using a proper web server like Nginx or Apache
4. Implementing rate limiting
5. Setting up proper backup solutions for your F: drive 