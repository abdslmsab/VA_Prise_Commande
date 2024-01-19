import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DataCacheDao {
    /*@Query("SELECT * FROM data_cache WHERE cacheKey = :cacheKey")
    fun getData(cacheKey: String): LiveData<DataCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(dataCache: DataCache)
     */
}


