package com.example.va_prisecommande.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Commercial(
    @PrimaryKey
    var prenom: String,
    var nom: String
) : Parcelable
