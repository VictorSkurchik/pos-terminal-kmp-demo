package by.vsdev.posterminal.demo.feature.pos.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import androidx.sqlite.driver.AndroidSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cart_items")
data class CartItemEntity(@PrimaryKey val productId: String, val name: String, val priceCents: Long, val quantity: Int)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY name")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun find(productId: String): CartItemEntity?

    @Upsert
    suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun delete(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}

@Database(entities = [CartItemEntity::class], version = 1, exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}

/** On-device Android database via [AndroidSQLiteDriver] (same Room as on the server — shared style). */
fun createLocalDatabase(context: Context): LocalDatabase =
    Room.databaseBuilder<LocalDatabase>(context, context.getDatabasePath("pos-local.db").absolutePath)
        .setDriver(AndroidSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
