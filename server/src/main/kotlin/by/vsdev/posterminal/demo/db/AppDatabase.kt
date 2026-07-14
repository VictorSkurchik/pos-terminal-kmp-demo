package by.vsdev.posterminal.demo.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File

@androidx.room.Database(
    entities = [DeviceEntity::class, CommandEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun commandDao(): CommandDao
}

/**
 * Room on the JVM server via the self-contained [BundledSQLiteDriver] (no external DB).
 * The `pos.db` file is created in the process working directory; data survives restarts.
 */
fun createDatabase(
    // DATABASE_PATH lets a Render persistent disk override the location; defaults to the working dir.
    dbPath: String = System.getenv("DATABASE_PATH")
        ?: File(System.getProperty("user.dir"), "pos.db").absolutePath,
): AppDatabase =
    Room.databaseBuilder<AppDatabase>(name = dbPath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        // Demo: no explicit migrations — recreate the DB on schema change.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
