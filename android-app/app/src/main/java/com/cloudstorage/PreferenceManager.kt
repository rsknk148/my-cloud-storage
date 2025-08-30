package com.cloudstorage

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val preferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "cloud_storage_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    var serverUrl: String
        get() = preferences.getString(KEY_SERVER_URL, "http://10.0.2.2:5000") ?: "http://10.0.2.2:5000"
        set(value) = preferences.edit().putString(KEY_SERVER_URL, value).apply()
    
    var username: String?
        get() = preferences.getString(KEY_USERNAME, null)
        set(value) = preferences.edit().putString(KEY_USERNAME, value).apply()
    
    var userId: Int
        get() = preferences.getInt(KEY_USER_ID, -1)
        set(value) = preferences.edit().putInt(KEY_USER_ID, value).apply()
    
    var email: String?
        get() = preferences.getString(KEY_EMAIL, null)
        set(value) = preferences.edit().putString(KEY_EMAIL, value).apply()
    
    var isLoggedIn: Boolean
        get() = preferences.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = preferences.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()
    
    fun logout() {
        preferences.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
    
    fun saveUser(user: User) {
        preferences.edit()
            .putString(KEY_USERNAME, user.username)
            .putInt(KEY_USER_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }
}