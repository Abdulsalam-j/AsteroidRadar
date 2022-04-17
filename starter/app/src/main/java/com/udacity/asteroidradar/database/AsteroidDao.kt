package com.udacity.asteroidradar.database

import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.Query

interface AsteroidDao {

    @Query("SELECT * FROM asteroids_table")
    fun getAsteroids(): LiveData<List<AsteroidRoomDatabase>>

    @Insert
    fun insertAll(vararg asteroids: AsteroidRoomDatabase)

}