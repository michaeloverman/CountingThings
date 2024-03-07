package digital.overman.michael.countingteslas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TeslaDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertTesla(tesla: TeslaEntity): Long

    @Query("SELECT * FROM teslaentity")
    fun getTeslas(): Flow<List<TeslaEntity>>

    @Query("SELECT * FROM teslaentity WHERE color = :color")
    fun getTeslasByColor(color: String): Flow<List<TeslaEntity>>

    @Query("DELETE FROM teslaentity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM teslaentity WHERE timestamp = :timestamp")
    suspend fun removeByTimestamp(timestamp: Long)

    @Query("SELECT * FROM teslaentity ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): TeslaEntity?

//    fun insertTesla(tesla: TeslaEntity): Long {
//        return _insert(tesla)
//    }
//
//    @Insert(onConflict = REPLACE)
//    fun _insert(tesla: TeslaEntity): Long
}

@Dao
interface ItemDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Query("SELECT * FROM itementity")
    fun getItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM itementity WHERE title = :title")
    fun getItemsByTitle(title: String): Flow<List<ItemEntity>>

    @Query("DELETE FROM itementity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM itementity WHERE timestamp = :timestamp")
    suspend fun removeByTimestamp(timestamp: Long)

    @Query("SELECT * FROM itementity ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): ItemEntity?
}