package com.udacity.asteroidradar.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.AsteroidApiFilter
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.domain.PictureOfDay
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.launch

enum class ApiStatus { LOADING, ERROR, DONE }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AsteroidsDatabase.getDatabase(application)
    private val repository = AsteroidsRepository(database)

    val asteroids = repository.allAsteroids as MutableLiveData<List<Asteroid>>

    private val _pictureOfDayStatus = MutableLiveData<ApiStatus>()
    val pictureOfDayStatus: LiveData<ApiStatus> get() = _pictureOfDayStatus

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay> get() = _pictureOfDay

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid?>()
    val navigateToSelectedAsteroid: LiveData<Asteroid?>
        get() = _navigateToSelectedAsteroid

    init {
        getAsteroids()
        getPictureOfDay()
    }

    private fun getAsteroids() {
        viewModelScope.launch {
            try {
                repository.refreshList(AsteroidApiFilter.SHOW_TODAY, AsteroidApiFilter.SHOW_WEEK)
            } catch (e: Exception) {
                Log.e("MainViewModel", e.toString())
            }
        }
    }

    fun showSavedAsteroids() {
        viewModelScope.launch {
            asteroids.value = database.asteroidDao
                .getSavedAsteroids().asDomainModel()
        }
    }

    fun showTodayAsteroids() {
        viewModelScope.launch {
            asteroids.value = database.asteroidDao
                .getSelectedDateAsteroids(AsteroidApiFilter.SHOW_TODAY.value,
                    AsteroidApiFilter.SHOW_TODAY.value).asDomainModel()
            Log.d("asteroids size:", "${asteroids.value?.size}")
        }
    }

    fun showWeekAsteroids() {
        viewModelScope.launch {
            asteroids.value = database.asteroidDao
                .getSelectedDateAsteroids(AsteroidApiFilter.SHOW_TODAY.value,
                    AsteroidApiFilter.SHOW_WEEK.value).asDomainModel()
        }
    }

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }

    private fun getPictureOfDay() {
        viewModelScope.launch {
            try {
                val pictureOfDay = Network.retrofitService.getImageOfTheDay()
                _pictureOfDayStatus.value = ApiStatus.LOADING

                if (pictureOfDay.mediaType.isNotEmpty()) {
                    if (pictureOfDay.mediaType == "image") {
                        _pictureOfDay.value = pictureOfDay
                    } else
                        _pictureOfDay.value = PictureOfDay(
                            "image",
                            Constants.DEFAULT_IMAGE_TITLE,
                            Constants.DEFAULT_IMAGE_URL
                        )
                    _pictureOfDayStatus.value = ApiStatus.DONE
                }
            } catch (e: Exception) {
                _pictureOfDayStatus.value = ApiStatus.ERROR
                Log.e("MainViewModel", e.toString())
            }
        }
    }
}