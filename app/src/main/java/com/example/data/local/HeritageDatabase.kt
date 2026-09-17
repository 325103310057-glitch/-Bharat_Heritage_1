package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.HeritageDao
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.UserSessionEntity

@Database(
    entities = [
        FavoriteEntity::class,
        UserProfileEntity::class,
        UserSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HeritageDatabase : RoomDatabase() {
    abstract fun heritageDao(): HeritageDao

    companion object {
        @Volatile
        private var INSTANCE: HeritageDatabase? = null

        fun getInstance(context: Context): HeritageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HeritageDatabase::class.java,
                    "bharat_heritage.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
