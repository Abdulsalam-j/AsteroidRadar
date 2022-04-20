package com.udacity.asteroidradar.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AsteroidDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg asteroids: AsteroidRoomDatabase)

    @Query("SELECT * FROM asteroids_table ORDER BY close_approach_date ASC")
    fun getAllAsteroids(): LiveData<List<AsteroidRoomDatabase>>

    @Query(
        "SELECT * FROM asteroids_table " +
                "WHERE close_approach_date >= :startDate " +
                "AND close_approach_date <= :closeDate " +
                "ORDER BY close_approach_date ASC"
    )
    suspend fun getSelectedDateAsteroids(startDate: String ,closeDate: String): List<AsteroidRoomDatabase>

    @Query("SELECT * FROM asteroids_table ORDER BY close_approach_date ASC")
    suspend fun getSavedAsteroids(): List<AsteroidRoomDatabase>

    @Query("DELETE FROM asteroids_table WHERE close_approach_date < :todayDate")
    suspend fun deletePreviousAsteroid(todayDate: String)

}
