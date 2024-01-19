import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DataCache::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataCacheDao(): DataCacheDao
}
