package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.api.*
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject

class AsteroidsRepository(private val database: AsteroidsDatabase) {

    val allAsteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidDao.getAllAsteroids()) {
            it.asDomainModel()
        }

    suspend fun queryAllAsteroids(): List<Asteroid> {
        return database.asteroidDao
            .getSavedAsteroids().asDomainModel()
    }

    suspend fun queryWeekAsteroids(): List<Asteroid> {
        return database.asteroidDao
            .getSelectedDateAsteroids(
                AsteroidApiFilter.TODAY_DATE.value,
                AsteroidApiFilter.NEXT_WEEK_DATE.value
            ).asDomainModel()
    }

    suspend fun queryTodayAsteroids(): List<Asteroid> {
        return database.asteroidDao
            .getSelectedDateAsteroids(AsteroidApiFilter.TODAY_DATE.value,
                AsteroidApiFilter.TODAY_DATE.value).asDomainModel()
    }

    suspend fun refreshList(startDate: AsteroidApiFilter = AsteroidApiFilter.TODAY_DATE, endDate: AsteroidApiFilter = AsteroidApiFilter.NEXT_WEEK_DATE) {
        withContext(Dispatchers.IO) {
            val asteroidList: ArrayList<Asteroid>
            val asteroidResponseBody = Network.retrofitService.getAsteroids(startDate.value, endDate.value)
            val responseBodyString = getStringFromResponse(asteroidResponseBody)
            asteroidList = parseAsteroidsJsonResult(JSONObject(responseBodyString))
            database.asteroidDao.insertAll(*asteroidList.asDatabaseModel())
        }
    }

    suspend fun deleteLastDayAsteroid(todayDate: String) {
        withContext(Dispatchers.IO) {
            database.asteroidDao.deletePreviousAsteroid(todayDate)
        }
    }

    private fun getStringFromResponse(responseBody: ResponseBody): String {
        return responseBody.string()
    }
}