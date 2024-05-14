import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.example.va_prisecommande.dao.CommercialDao
import com.example.va_prisecommande.model.Commercial

@Database(entities = [Commercial::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commercialDao(): CommercialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun initialiser(context: Context) {
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).createFromAsset("database/commercial.db")
            .fallbackToDestructiveMigration()
            .build()


        fun getInstance(): AppDatabase {
            return INSTANCE ?: throw IllegalStateException("Base de données non initialisée")
        }
    }
}
