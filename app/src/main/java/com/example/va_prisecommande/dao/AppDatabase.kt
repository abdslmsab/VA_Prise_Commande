import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.va_prisecommande.dao.CommercialDao
import com.example.va_prisecommande.model.Commercial

@Database(entities = [Commercial::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commercialDao(): CommercialDao
}
