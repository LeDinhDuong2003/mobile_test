package com.example.mobileproject.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mobileproject.model.User;
import com.example.mobileproject.model.UserMain;

public class SessionManager {
    private static final String PREF_NAME = "user_info";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_AVATAR_URL = "avatar_url";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ROLE = "role";

    private static SessionManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;

    private SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUserSession(User user) {
        editor.putInt(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_AVATAR_URL, user.getAvatarUrl());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return pref.getString(KEY_FULL_NAME, "");
    }
    public String getUserRole() {
        return pref.getString(KEY_ROLE, "");
    }
    public UserMain getCurrentUser() {
//        if (!isLoggedIn()) {
//            return null;
//        }

        String id = String.valueOf(pref.getInt(KEY_USER_ID, -1));
        String name = pref.getString(KEY_FULL_NAME, "");
        String email = pref.getString(KEY_EMAIL, "");
        String avatarUrl = pref.getString(KEY_AVATAR_URL, "");

        return new UserMain(id, name, email, avatarUrl);
    }

    public User getUserDetails() {
        if (!isLoggedIn()) {
            return null;
        }

        User user = new User();
        user.setUserId(pref.getInt(KEY_USER_ID, -1));
        user.setFullName(pref.getString(KEY_FULL_NAME, ""));
        user.setEmail(pref.getString(KEY_EMAIL, ""));
        user.setPhone(pref.getString(KEY_PHONE, ""));
        user.setAvatarUrl(pref.getString(KEY_AVATAR_URL, ""));

        return user;
    }
}