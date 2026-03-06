# Cloud Storage Android App

This is the Android companion app for the Cloud Storage web application. It provides mobile access to your personal cloud storage with the same core functionality as the web interface.

## Features

- **User Authentication**: Secure login and registration
- **File Management**: Upload, download, and delete files from your mobile device
- **File Browser**: View all your uploaded files with details (size, upload date)
- **Server Configuration**: Connect to your Flask backend server
- **Material Design**: Modern Android UI following Google's Material Design guidelines

## Prerequisites

1. Android Studio Arctic Fox or later
2. Android SDK API level 21 or higher
3. Flask backend server running (see main README.md)

## Setup

1. Open the `android-app` folder in Android Studio
2. Let Android Studio download and install required dependencies
3. Connect an Android device or start an emulator
4. Build and run the project

## Configuration

The app connects to your Flask backend server. By default, it's configured to connect to:
- `http://10.0.2.2:5000` (for Android emulator)
- This maps to `localhost:5000` on your development machine

For physical devices, update the server URL in the login screen to your computer's IP address:
- Example: `http://192.168.1.100:5000`

## Usage

1. **First Launch**: Enter your server URL, username, and password
2. **Registration**: Tap "Don't have an account? Register" to create a new account
3. **File Upload**: Tap "Select File" to choose a file, then "Upload"
4. **File Management**: Use download and delete buttons on each file
5. **Refresh**: Pull down to refresh the file list
6. **Logout**: Use the logout button in the toolbar

## File Operations

- **Upload**: Select files from your device storage and upload to the cloud
- **Download**: Files are downloaded to your device's Downloads folder
- **Delete**: Remove files from cloud storage with confirmation dialog

## Security

- Uses HTTP cookies for session management
- Passwords are validated on the server side
- All communication with the Flask backend is over HTTP (use HTTPS in production)

## Permissions

The app requires these permissions:
- `INTERNET`: To communicate with the Flask backend
- `READ_EXTERNAL_STORAGE`: To select files for upload
- `WRITE_EXTERNAL_STORAGE`: To save downloaded files (Android 9 and below)

## Technical Details

- **Language**: Kotlin
- **Architecture**: Single Activity with multiple fragments pattern
- **HTTP Client**: OkHttp with Gson for JSON parsing
- **UI**: Material Design Components
- **Async Operations**: Kotlin Coroutines
- **File Handling**: Android Storage Access Framework

## Troubleshooting

### Connection Issues
1. Verify the Flask server is running and accessible
2. Check the server URL format (include http:// prefix)
3. Ensure your device and server are on the same network

### Upload/Download Issues
1. Check file permissions on your device
2. Verify available storage space
3. Check Flask server logs for error details

### Authentication Issues
1. Verify username/password are correct
2. Check if user is registered in the Flask backend
3. Clear app data and try again

## Building for Production

1. Update the default server URL in `strings.xml`
2. Configure proper signing keys
3. Enable code obfuscation
4. Test on various devices and Android versions

## Future Enhancements

- File preview capabilities
- Folder organization
- Offline file caching
- Push notifications for file sharing
- Dark theme support
- Multi-user account switching