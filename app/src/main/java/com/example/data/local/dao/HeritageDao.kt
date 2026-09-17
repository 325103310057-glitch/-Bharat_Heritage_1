package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeritageDao {
    // Favorites
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itemId = :itemId)")
    fun isFavorite(itemId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE itemId = :itemId")
    suspend fun deleteFavorite(itemId: String)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE phoneNumber = :phoneNumber LIMIT 1")
    fun getUserProfile(phoneNumber: String): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // User Session
    @Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
    fun getSession(): Flow<UserSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: UserSessionEntity)

    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}
