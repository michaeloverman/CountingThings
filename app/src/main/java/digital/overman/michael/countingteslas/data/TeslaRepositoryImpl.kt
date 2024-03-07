package digital.overman.michael.countingteslas.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class TeslaRepositoryImpl @Inject constructor (private val dao: TeslaDao) : TeslaRepository {
    override suspend fun insertTesla(tesla: Tesla): Long {
        val entity = tesla.toTeslaEntity()
        Timber.d("Inserting tesla id: ${entity.id}")
        return dao.insertTesla(entity)
    }

    override fun getTeslas(): Flow<List<Tesla>> {
        return dao.getTeslas().map { list ->
            list.map { entity ->
                entity.toTesla()
            }
        }
    }

    override fun getTeslasByColor(color: String): Flow<List<Tesla>> {
        return dao.getTeslasByColor(color).map { list ->
            list.map { entity ->
                entity.toTesla()
            }
        }
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
    }

    override suspend fun removeByTimestamp(timestamp: Long) {
        dao.removeByTimestamp(timestamp)
    }

    override suspend fun getLast(): Tesla? {
        return dao.getLast()?.toTesla()
    }
}

class ItemRepositoryImpl @Inject constructor (private val dao: ItemDao) : ItemRepository {
    override suspend fun insertItem(item: Item): Long {
        val entity = item.toItemEntity()
        return dao.insertItem(entity)
    }

    override fun getItems(): Flow<List<Item>> {
        return dao.getItems().map { list ->
            list.map { entity ->
                entity.toItem()
            }
        }
    }

    override fun getItemsByTitle(title: String): Flow<List<Item>> {
        return dao.getItemsByTitle(title).map { list ->
            list.map { entity ->
                entity.toItem()
            }
        }
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
    }

    override suspend fun removeByTimestamp(timestamp: Long) {
        dao.removeByTimestamp(timestamp)
    }

    override suspend fun getLast(): Item? {
        return dao.getLast()?.toItem()
    }

}