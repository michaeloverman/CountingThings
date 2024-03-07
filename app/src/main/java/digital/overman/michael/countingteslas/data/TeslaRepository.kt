package digital.overman.michael.countingteslas.data

import kotlinx.coroutines.flow.Flow


interface TeslaRepository {
    suspend fun insertTesla(tesla: Tesla): Long
    fun getTeslas(): Flow<List<Tesla>>
    fun getTeslasByColor(color: String): Flow<List<Tesla>>
    suspend fun removeById(id: Long)
    suspend fun removeByTimestamp(timestamp: Long)
    suspend fun getLast(): Tesla?
}

interface ItemRepository {
    suspend fun insertItem(item: Item): Long
    fun getItems(): Flow<List<Item>>
    fun getItemsByTitle(title: String): Flow<List<Item>>
    suspend fun removeById(id: Long)
    suspend fun removeByTimestamp(timestamp: Long)
    suspend fun getLast(): Item?
}