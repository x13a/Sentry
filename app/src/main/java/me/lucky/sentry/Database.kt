package me.lucky.sentry

import android.content.Context
import androidx.room.*

@Database(entities = [Package::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packageDao(): PackageDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        private const val DATABASE_NAME = "app.db"

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        private fun buildDatabase(context: Context) = Room
            .databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
            .allowMainThreadQueries()
            .build()
    }
}

@Dao
interface PackageDao {
    @Insert
    fun insert(obj: Package)

    @Query("DELETE FROM package WHERE name = :name")
    fun delete(name: String)

    @Query("SELECT * FROM package WHERE name = :name")
    fun select(name: String): Package?

    @Query("DELETE FROM package")
    fun deleteAll()
}

@Entity(
    indices = [Index(value = ["name"], unique = true)],
    tableName = "package",
)
data class Package(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "name") val name: String,
)