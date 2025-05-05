# Personal Cloud Storage

A simple, secure personal cloud storage solution that uses your F: drive for storage, allowing you to store and access your files from anywhere.

## Features

- Secure login system
- File upload and download
- File management (view, download, delete)
- Modern, responsive web interface
- No file size limit (configurable)
- Uses F: drive for storage

## Prerequisites

1. Ensure your F: drive is accessible and mounted
2. Make sure you have write permissions on the F: drive
3. Python 3.8 or higher installed

## Setup

1. Install the required dependencies:
   ```bash
   pip install -r requirements.txt
   ```
2. Run the application:
   ```bash
   python app.py
   ```
3. Access the application at `http://localhost:5000`

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

## Production Deployment

For production use, consider:
1. Using a proper database instead of the in-memory user storage
2. Setting up HTTPS
3. Using a proper web server like Nginx or Apache
4. Implementing rate limiting
5. Setting up proper backup solutions for your F: drive 