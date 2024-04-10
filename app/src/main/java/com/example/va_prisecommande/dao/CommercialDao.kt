package com.example.va_prisecommande.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.va_prisecommande.model.Commercial
import kotlinx.coroutines.flow.Flow

@Dao
interface CommercialDao {
    @Query("SELECT * FROM commercial")
    fun getAll(): Flow<List<Commercial>>

    @Insert
    fun insertAll(commerciaux: List<Commercial>)
}
/*
@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
           "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): User

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}
 */