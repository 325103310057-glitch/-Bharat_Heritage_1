package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val itemId: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val phoneNumber: String,
    val displayName: String,
    val bio: String = "Indian Heritage Explorer",
    val joinedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey
    val id: Int = 1,
    val phoneNumber: String,
    val sessionToken: String,
    val isLoggedIn: Boolean,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)
