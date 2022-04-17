package com.udacity.asteroidradar.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.AsteroidApiFilter
import com.udacity.asteroidradar.api.Network
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.domain.PictureOfDay
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class ApiStatus { LOADING, ERROR, DONE }

class MainViewModel : ViewModel() {

    private val _asteroidStatus = MutableLiveData<ApiStatus>()
    val asteroidStatus: LiveData<ApiStatus> get() = _asteroidStatus

    private val _asteroids = MutableLiveData<List<Asteroid>>()
    val asteroids: LiveData<List<Asteroid>> get() = _asteroids

    private val _pictureOfDayStatus = MutableLiveData<ApiStatus>()
    val pictureOfDayStatus: LiveData<ApiStatus> get() = _pictureOfDayStatus

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay> get() = _pictureOfDay

    private val _navigateToSelectedAsteroid = MutableLiveData<Asteroid?>()
    val navigateToSelectedAsteroid : LiveData<Asteroid?>
        get() = _navigateToSelectedAsteroid

    init {
        getAsteroids(AsteroidApiFilter.SHOW_TODAY)
        Log.d("MainViewModel","Today value is: ${AsteroidApiFilter.SHOW_TODAY.value}" +
                " Coming week value is: ${AsteroidApiFilter.SHOW_WEEK.value}")
        getPictureOfDay()
    }

    private fun getAsteroids(startDate: AsteroidApiFilter) {
        viewModelScope.launch {
            try {
                val responseBody = Network.retrofitService.getAsteroids(startDate.value)
                val responseBodyString = responseBody.string()
                _asteroidStatus.value = ApiStatus.LOADING

                if (responseBodyString.isNotEmpty()) {
                    _asteroids.value = parseAsteroidsJsonResult(JSONObject(responseBodyString))
                    _asteroidStatus.value = ApiStatus.DONE
                }
            } catch (e: Exception) {
                _asteroidStatus.value = ApiStatus.ERROR
                _asteroids.value = ArrayList()
                Log.e("MainViewModel", e.toString())
            }
        }
    }

    fun updateFilter(startDate: AsteroidApiFilter, endDate: AsteroidApiFilter) {

    }

    private fun getPictureOfDay() {
        viewModelScope.launch {
            try {
                val pictureOfDay = Network.retrofitService.getImageOfTheDay()
                _pictureOfDayStatus.value = ApiStatus.LOADING

                if (pictureOfDay.mediaType.isNotEmpty()) {
                    if (pictureOfDay.mediaType == "image") {
                        _pictureOfDay.value = pictureOfDay
                    }
                    else
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

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToSelectedAsteroid.value = asteroid
    }

    fun displayAsteroidDetailsComplete() {
        _navigateToSelectedAsteroid.value = null
    }
}