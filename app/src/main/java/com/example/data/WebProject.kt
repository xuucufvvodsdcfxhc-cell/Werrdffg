package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "web_projects")
data class WebProject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val htmlContent: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface WebProjectDao {
    @Query("SELECT * FROM web_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<WebProject>>

    @Query("SELECT * FROM web_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Int): WebProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: WebProject): Long

    @Query("DELETE FROM web_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)
}

@Database(entities = [WebProject::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webProjectDao(): WebProjectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "websketch_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class WebProjectRepository(private val dao: WebProjectDao) {
    val allProjects: Flow<List<WebProject>> = dao.getAllProjects()

    suspend fun getProjectById(id: Int): WebProject? = dao.getProjectById(id)

    suspend fun insert(project: WebProject): Long = dao.insertProject(project)

    suspend fun deleteById(id: Int) = dao.deleteProjectById(id)
}
