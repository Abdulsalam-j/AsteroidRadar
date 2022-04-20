package com.udacity.asteroidradar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.api.AsteroidApiFilter
import com.udacity.asteroidradar.database.AsteroidsDatabase.Companion.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import retrofit2.HttpException

class RefreshDataWorker (context: Context, params: WorkerParameters):
        CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "RefreshDataWorker"
    }

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = AsteroidsRepository(database)
        return try {
            repository.refreshList()
            Result.success()
        }catch (e: HttpException) {
            Result.retry()
        }
    }
}