package com.ljmarinscull.baubuddy

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import app.cash.turbine.test
import com.ljmarinscull.baubuddy.data.datasource.local.AppDatabase
import com.ljmarinscull.baubuddy.data.datasource.local.LocalDataSource
import com.ljmarinscull.baubuddy.data.datasource.remote.RemoteDataSource
import com.ljmarinscull.baubuddy.domain.repository.Repository
import com.ljmarinscull.baubuddy.ui.home.FilterType
import com.ljmarinscull.baubuddy.ui.home.FilterType.Companion.SEPARATOR
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
        repository.loadResources(FilterType.Query()).test {
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
        repository.loadResources(FilterType.Query()).test {
            val emission = awaitItem()
            assertEquals(emission.size,0)

            val result = repository.requestResources("")
            assertEquals(result.isFailure,true)
        }
    }

    @Test
    fun returnsDataEmissionWithValuesUpdatedOnSuccessRequest() = runBlocking {
        repository.loadResources(FilterType.Query()).test {
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

    @Test
    fun returnsAvailableResourceThatAlsoContainsTheLeftSideQuery() = runBlocking {

        val query = handleQuery("Task 1+able=true")
        repository.loadResources(query).test {
            val emission = awaitItem()
            assertEquals(emission.size,0)

            localDataSource.save(newResources())

            val emission2 = awaitItem()
            assertEquals(emission2.size,1)
        }
    }

    @Test
    fun returnsNotAvailableResourceThatAlsoContainsTheLeftSideQuery() = runBlocking {

        val query = handleQuery("33+able=false")
        repository.loadResources(query).test {
            val emission = awaitItem()
            assertEquals(emission.size,0)

            localDataSource.save(newResources())

            val emission2 = awaitItem()
            assertEquals(emission2.size,2)
        }
    }

    @Test
    fun returnsResourceThatContainsTheLeftSideQueryIgnoringWrongFalseBooleanQuery() = runBlocking {

        val query = handleQuery("33+able=falses")
        repository.loadResources(query).test {
            val emission = awaitItem()
            assertEquals(emission.size,0)

            localDataSource.save(newResources())

            val emission2 = awaitItem()
            assertEquals(emission2.size,0)
        }
    }

    @Test
    fun returnsResourceThatContainsTheLeftSideQueryIgnoringWrongTrueBooleanQuery() = runBlocking {

        val query = handleQuery("33+able=trues")
        repository.loadResources(query).test {
            val emission = awaitItem()
            assertEquals(emission.size,0)

            localDataSource.save(newResources())

            val emission2 = awaitItem()
            assertEquals(emission2.size,0)
        }
    }

    @Test
    fun returnsAvailableResourceOnAvailabilityQuery() = runBlocking {

        val query = handleQuery("able=true")
        repository.loadResources(query).test {
            val emission = awaitItem()
            assertEquals(emission.size,0)

            localDataSource.save(newResources())

            val emission2 = awaitItem()
            assertEquals(emission2.size,1)
        }
    }

    @Test
    fun returnsNotAvailableResourceOnAvailabilityQuery() = runBlocking {

        val query = handleQuery("able=false")
        repository.loadResources(query).test {
            val emission = awaitItem()
            assertEquals(emission.size,0)

            localDataSource.save(newResources())

            val emission2 = awaitItem()
            assertEquals(emission2.size,2)
        }
    }

    private fun handleQuery(query: String?): FilterType {
        if (query.isNullOrEmpty())
            return FilterType.Query()

        val filterType = FilterType.filterAvailabilityFrom(query)
        if (filterType != null)
            return filterType

        if (query.contains(SEPARATOR)) {
            val splitQuery = query.split(SEPARATOR, limit = 2)
            if (splitQuery.size == 2) {
                val type = FilterType.filterAvailabilityFrom(splitQuery[1])
                if(type != null)
                    return FilterType.CompoundQuery(splitQuery[0], type.isAvailable)
                else
                    FilterType.Query(splitQuery[0])
            }
        }

        return FilterType.Query(query)
    }
}