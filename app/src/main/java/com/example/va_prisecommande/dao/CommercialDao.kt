package com.example.va_prisecommande.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.va_prisecommande.model.Commercial
import kotlinx.coroutines.flow.Flow

@Dao
interface CommercialDao {
    @Query("SELECT * FROM commercial")
    fun getAll(): List<Commercial>

    @Insert
    fun insertAll(commerciaux: List<Commercial>)
}