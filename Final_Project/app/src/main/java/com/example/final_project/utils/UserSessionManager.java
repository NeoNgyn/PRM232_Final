package com.example.final_project.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * UserSessionManager handles user authentication session and user_id storage
 * Provides centralized access to current logged-in user information
 */
public class UserSessionManager {
    private static final String PREF_NAME = "FridgeManagerPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static UserSessionManager instance;
    private SharedPreferences preferences;
    private Context context;

    private UserSessionManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get singleton instance of UserSessionManager
     */
    public static synchronized UserSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserSessionManager(context);
        }
        return instance;
    }

    /**
     * Save user session after successful login
     */
    public void saveUserSession(String userId, String email, String fullName) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, fullName);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Get current logged-in user's ID
     */
    public String getCurrentUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    /**
     * Get current logged-in user's email
     */
    public String getCurrentUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Get current logged-in user's full name
     */
    public String getCurrentUserName() {
        return preferences.getString(KEY_USER_NAME, null);
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Clear user session (logout)
     */
    public void clearUserSession() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    /**
     * Get user ID or throw exception if not logged in
     */
    public String getRequiredUserId() {
        String userId = getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("User not logged in. Cannot proceed without user context.");
        }
        return userId;
    }
}
