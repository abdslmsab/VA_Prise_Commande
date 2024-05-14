package com.example.va_prisecommande.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Commercial(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id_Commercial")
    var id: Int = 0,

    @ColumnInfo(name = "Prenom_Commercial")
    var prenom: String,

    @ColumnInfo(name = "Nom_Commercial")
    var nom: String
) : Parcelable