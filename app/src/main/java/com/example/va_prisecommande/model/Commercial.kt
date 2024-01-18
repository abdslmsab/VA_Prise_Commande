package com.example.va_prisecommande.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Commercial(
    var prenom: String,
    var nom: String
) : Parcelable
