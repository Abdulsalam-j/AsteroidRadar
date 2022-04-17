package com.udacity.asteroidradar.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AsteroidRoomDatabase::class], version = 1)
abstract class AsteroidsDatabase: RoomDatabase() {

    abstract val asteroidDao: AsteroidDao

    companion object {
        @Volatile
        private lateinit var INSTANCE: AsteroidsDatabase

        fun getDatabase(context: Context): AsteroidsDatabase {
            synchronized(AsteroidsDatabase::class.java) {
                if (!::INSTANCE.isInitialized) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AsteroidsDatabase::class.java,
                        "AsteroidsDatabase"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}