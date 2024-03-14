package com.ljmarinscull.baubuddy

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import app.cash.turbine.test
import com.ljmarinscull.baubuddy.data.datasource.local.LocalDataSource
import com.ljmarinscull.baubuddy.data.datasource.remote.RemoteDataSource
import com.ljmarinscull.baubuddy.domain.repository.Repository
import com.ljmarinscull.baubuddy.ui.home.CompoundQuery
import com.ljmarinscull.baubuddy.util.newResources
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@SmallTest
class RepositoryTest {

    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var localDataSource: LocalDataSource
    private lateinit var repository: Repository
    private lateinit var api: ApiServiceFake
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        api = ApiServiceFake()
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        remoteDataSource = RemoteDataSource(
            api
        )
        localDataSource = LocalDataSource(
            database.resourcesDao()
        )
        repository = Repository(remoteDataSource,localDataSource)
        database.clearAllTables()
    }

    @Test
    fun returnsListOfItemsOnSuccessRequest() = runBlocking {
        repository.loadResources(CompoundQuery()).test {
            val emission = awaitItem()
            assertEquals(emission.size,0)

            val result = repository.requestResources("")
            assertEquals(result.isSuccess,true)

            val emission1 = awaitItem()
            assertEquals(emission1.size,2)
        }
    }

    @Test
    fun returnsZeroDataEmissionOfItemsOnErrorRequest() = runBlocking {
        api.errorToReturn = IOException()
        repository.loadResources(CompoundQuery()).test {
            val emission = awaitItem()
            assertEquals(emission.size,0)

            val result = repository.requestResources("")
            assertEquals(result.isFailure,true)
        }
    }

    @Test
    fun returnsDataEmissionWithValuesUpdatedOnSuccessRequest() = runBlocking {
        repository.loadResources(CompoundQuery()).test {
            val emission = awaitItem()
            assertEquals(emission.size,0)

            val result = repository.requestResources("")
            assertEquals(result.isSuccess,true)

            val emission1 = awaitItem()
            assertEquals(emission1.size,2)

            localDataSource.save(newResources())

            val emission2 = awaitItem()
            assertEquals(emission2[0].description, newResources()[0].description)
            assertEquals(emission2[1].description, newResources()[1].description)
        }
    }
}