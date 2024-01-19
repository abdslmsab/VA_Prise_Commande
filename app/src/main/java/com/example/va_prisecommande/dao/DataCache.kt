import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_cache")
data class DataCache(
    @PrimaryKey val cacheKey: String,
    val xmlData: String
)
