package io.github.janmalch.pocpic.data

import android.content.Context
import android.net.Uri
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
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun toSourceType(value: String) = enumValueOf<SourceEntity.Type>(value)

    @TypeConverter
    fun fromSourceType(value: SourceEntity.Type) = value.name

    @TypeConverter
    fun toUri(value: String) = Uri.parse(value)

    @TypeConverter
    fun fromUri(value: Uri) = value.toString()

}

private const val tableName = "sources"

@Entity(tableName = tableName)
data class SourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo val label: String,
    @ColumnInfo val uri: Uri,
    @ColumnInfo val type: Type,
    @ColumnInfo val weight: Int,
    @ColumnInfo val isRemoteRedirect: Boolean,
) {
    init {
        require(weight > 0) { "Weight must be positive." }
    }

    enum class Type {
        REMOTE,
        LOCAL_FILE,
        LOCAL_DIRECTORY,
    }
}

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources")
    fun watchAll(): Flow<List<SourceEntity>>

    @Query("SELECT * FROM sources")
    fun findAll(): List<SourceEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sources: List<SourceEntity>)

    @Update
    suspend fun update(source: SourceEntity)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun remove(id: Long)
}

@Database(entities = [SourceEntity::class], version = 1, exportSchema = false)
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
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)

                            db.execSQL(
                                """INSERT INTO $tableName (label, uri, type, weight, isRemoteRedirect)
                                | SELECT 'Nature (unsplash.com)', 'https://source.unsplash.com/random?nature', 'REMOTE', 1, 1
                                | WHERE NOT EXISTS (SELECT * FROM $tableName)
                            """.trimMargin()
                            )
                        }
                    })
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
