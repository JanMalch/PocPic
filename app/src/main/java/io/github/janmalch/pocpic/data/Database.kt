package io.github.janmalch.pocpic.data

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.parcelize.Parcelize

class Converters {
    @TypeConverter
    fun toUri(value: String): Uri = Uri.parse(value)

    @TypeConverter
    fun fromUri(value: Uri): String = value.toString()

}

private const val tableName = "sources"

@Parcelize
@Entity(tableName = tableName)
data class SourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo val label: String,
    @ColumnInfo val uri: Uri,
    @ColumnInfo val weight: Int,
) : Parcelable {
    init {
        require(weight > 0) { "Weight must be positive." }
    }
}

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources")
    fun watchAll(): Flow<List<SourceEntity>>

    @Query("SELECT * FROM sources")
    fun findAll(): List<SourceEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(source: SourceEntity)

    @Update
    suspend fun update(source: SourceEntity)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun remove(id: Long)
}

@Database(entities = [SourceEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sourcesDao(): SourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
