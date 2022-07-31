package io.github.janmalch.pocpic.data

import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.janmalch.pocpic.R

class Converters {
    @TypeConverter
    fun toSourceTypEnum(value: String) = enumValueOf<SourceFactoryConfig.SourceType>(value)

    @TypeConverter
    fun fromSourceTypEnum(value: SourceFactoryConfig.SourceType) = value.name

    @TypeConverter
    fun toUri(value: String) = Uri.parse(value)

    @TypeConverter
    fun fromUri(value: Uri) = value.toString()
}

private const val tableName = "Configs"

@Entity(tableName = tableName)
data class SourceFactoryConfig(
    @PrimaryKey(autoGenerate = false) val label: String,
    @ColumnInfo val uri: Uri,
    @ColumnInfo val sourceType: SourceType,
    @ColumnInfo val remoteCacheable: Boolean?,
    @ColumnInfo val remoteSeedQueryParam: String?
) {
    enum class SourceType(@StringRes val translationRes: Int) {
        REMOTE(R.string.source_type_remote),
        LOCAL_FILE(R.string.source_type_local_file),
        LOCAL_DIRECTORY(R.string.source_type_local_directory),
    }
}

@Dao
interface SourceFactoryConfigDao {
    @Query("SELECT * FROM Configs")
    fun watchAll(): LiveData<List<SourceFactoryConfig>>

    @Query("SELECT * FROM Configs")
    suspend fun getAll(): List<SourceFactoryConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(vararg configs: SourceFactoryConfig)

    @Delete
    suspend fun remove(vararg configs: SourceFactoryConfig)
}

@Database(entities = [SourceFactoryConfig::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun configsDao(): SourceFactoryConfigDao

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
                    context,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)

                            db.execSQL(
                                """INSERT INTO $tableName (uri, sourceType, label, remoteCacheable, remoteSeedQueryParam)
                                | SELECT 'https://source.unsplash.com/random?nature', 'REMOTE', 'Random nature image from unsplash.com', 0, NULL
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
