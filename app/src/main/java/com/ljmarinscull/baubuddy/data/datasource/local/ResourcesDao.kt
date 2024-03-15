package com.ljmarinscull.baubuddy.data.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ljmarinscull.baubuddy.data.models.ResourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourcesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(values: List<ResourceEntity>)

    @Query("SELECT * FROM resources")
    fun findAll(): Flow<List<ResourceEntity>>

    @Query("""
        SELECT * FROM resources 
        WHERE task LIKE '%' || :query || '%' 
        OR title LIKE '%' || :query || '%' 
        OR sort LIKE '%' || :query || '%' 
        OR wageType LIKE '%' || :query || '%'
        OR colorCode LIKE '%' || :query || '%'
        OR businessUnit LIKE '%' || :query || '%'
        OR workingTime LIKE '%' || :query || '%'
        OR parentTaskID LIKE '%' || :query || '%'
        OR businessUnitKey LIKE '%' || :query || '%' 
        OR prePlanningBoardQuickSelect LIKE '%' || :query || '%'
        OR isAvailableInTimeTrackingKioskMode = :isAvailable
        """)
    fun findAll(query: String, isAvailable: Boolean): Flow<List<ResourceEntity>>

    @Query("""
        SELECT * FROM resources 
        WHERE task LIKE '%' || :query || '%' 
        OR title LIKE '%' || :query || '%' 
        OR sort LIKE '%' || :query || '%' 
        OR wageType LIKE '%' || :query || '%'
        OR colorCode LIKE '%' || :query || '%'
        OR businessUnit LIKE '%' || :query || '%'
        OR workingTime LIKE '%' || :query || '%'
        OR parentTaskID LIKE '%' || :query || '%'
        OR businessUnitKey LIKE '%' || :query || '%' 
        OR prePlanningBoardQuickSelect LIKE '%' || :query || '%'
        """)
    fun findAll(query: String): Flow<List<ResourceEntity>>

    @Query("""
        SELECT * FROM resources 
        WHERE isAvailableInTimeTrackingKioskMode = :isAvailable
        """)
    fun findAll(isAvailable: Boolean): Flow<List<ResourceEntity>>
}
