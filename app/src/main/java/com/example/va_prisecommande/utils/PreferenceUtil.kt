package com.example.va_prisecommande.utils

import android.content.Context
import android.preference.PreferenceManager
import java.util.*

object PreferenceUtil {
    private const val KEY_LAST_STORED_DATE = "last_stored_date"

    fun getLastStoredDate(context: Context): Date? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val timestamp = sharedPreferences.getLong(KEY_LAST_STORED_DATE, 0)
        return if (timestamp != 0L) Date(timestamp) else null
    }

    fun setLastStoredDate(context: Context, date: Date) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.putLong(KEY_LAST_STORED_DATE, date.time)
        editor.apply()
    }
}
