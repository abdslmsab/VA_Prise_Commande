package com.example.va_prisecommande.utils

import android.os.Environment

object PathsConstants {
    val LOCAL_STORAGE =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            .toString()
}