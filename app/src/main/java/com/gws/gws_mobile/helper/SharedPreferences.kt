package com.gws.gws_mobile.helper

import android.content.Context
import android.content.SharedPreferences

object SharedPreferences {
    private const val PREF_NAME = "user_prefs"
    private const val USER_ID_KEY = "user_id"

    fun saveUserId(context: Context, userId: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(USER_ID_KEY, userId)
        editor.apply()
    }

    fun getUserId(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(USER_ID_KEY, null)
    }

    fun clearUserId(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(USER_ID_KEY)
        editor.apply()
    }
}
