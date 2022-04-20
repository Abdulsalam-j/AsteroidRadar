package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.api.AsteroidApiFilter
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AsteroidsRepository(private val database: AsteroidsDatabase) {

    val allAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAllAsteroids()) {
            it.asDomainModel()
        }

    suspend fun refreshList(startDate: AsteroidApiFilter, endDate: AsteroidApiFilter) {
        withContext(Dispatchers.IO) {
            val asteroidList: ArrayList<Asteroid>
            val asteroidResponseBody = Network.retrofitService.getAsteroids(startDate.value, endDate.value)
            val responseBodyString = asteroidResponseBody.string()
            asteroidList = parseAsteroidsJsonResult(JSONObject(responseBodyString))
            database.asteroidDao.insertAll(*asteroidList.asDatabaseModel())
        }
    }

    suspend fun deleteLastDayAsteroid(todayDate: String) {
        withContext(Dispatchers.IO) {
            database.asteroidDao.deletePreviousAsteroid(todayDate)
        }
    }
}