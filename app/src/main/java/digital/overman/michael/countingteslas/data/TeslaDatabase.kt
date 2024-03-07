package digital.overman.michael.countingteslas.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TeslaEntity::class],
    version = 1,
//    autoMigrations = [
//        AutoMigration (from = 1, to = 2)
//    ]
)
abstract class TeslaDatabase : RoomDatabase() {
    abstract val dao: TeslaDao
    companion object {
        const val DATABASE_NAME = "teslas_db"
    }
}

@Database(
    entities = [ItemEntity::class],
    version = 1,
//    autoMigrations = [
//        AutoMigration (from = 1, to = 2)
//    ]
)
abstract class ItemDatabase : RoomDatabase() {
    abstract val dao: ItemDao
    companion object {
        const val DATABASE_NAME = "items.db"
    }
}