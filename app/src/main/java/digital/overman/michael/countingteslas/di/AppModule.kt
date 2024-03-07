package digital.overman.michael.countingteslas.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import digital.overman.michael.countingteslas.data.ItemDatabase
import digital.overman.michael.countingteslas.data.ItemRepository
import digital.overman.michael.countingteslas.data.ItemRepositoryImpl
import digital.overman.michael.countingteslas.data.TeslaDatabase
import digital.overman.michael.countingteslas.data.TeslaRepository
import digital.overman.michael.countingteslas.data.TeslaRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTeslaDatabase(app: Application): TeslaDatabase {
        return Room.databaseBuilder(
            app,
            TeslaDatabase::class.java,
            TeslaDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideTeslaRepository(db: TeslaDatabase): TeslaRepository {
        return TeslaRepositoryImpl(db.dao)
    }

    @Provides
    @Singleton
    fun provideItemDatabase(app: Application): ItemDatabase {
        return Room.databaseBuilder(
            app,
            ItemDatabase::class.java,
            ItemDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideItemRepository(db: ItemDatabase): ItemRepository {
        return ItemRepositoryImpl(db.dao)
    }
}