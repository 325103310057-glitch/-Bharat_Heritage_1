package com.example.data.repository

import com.example.data.datasource.HeritageCatalog
import com.example.data.local.dao.HeritageDao
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.UserSessionEntity
import com.example.data.model.HeritageCategory
import com.example.data.model.HeritageItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HeritageRepository(private val dao: HeritageDao) {

    fun getAllHeritageItems(): List<HeritageItem> = HeritageCatalog.items

    fun getHeritageItemById(id: String): HeritageItem? = HeritageCatalog.findById(id)

    fun searchHeritage(query: String, category: HeritageCategory = HeritageCategory.ALL): List<HeritageItem> {
        return HeritageCatalog.search(query, category)
    }

    fun getFavoriteIds(): Flow<Set<String>> {
        return dao.getAllFavorites().map { list -> list.map { it.itemId }.toSet() }
    }

    fun isFavorite(itemId: String): Flow<Boolean> {
        return dao.isFavorite(itemId)
    }

    suspend fun toggleFavorite(itemId: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            dao.deleteFavorite(itemId)
        } else {
            dao.insertFavorite(FavoriteEntity(itemId = itemId))
        }
    }

    fun getUserSession(): Flow<UserSessionEntity?> = dao.getSession()

    suspend fun saveSession(phoneNumber: String, token: String) {
        dao.saveSession(
            UserSessionEntity(
                id = 1,
                phoneNumber = phoneNumber,
                sessionToken = token,
                isLoggedIn = true,
                lastActiveTimestamp = System.currentTimeMillis()
            )
        )
        // Ensure default profile exists
        dao.saveUserProfile(
            UserProfileEntity(
                phoneNumber = phoneNumber,
                displayName = "Heritage Explorer",
                bio = "Preserving and celebrating India's eternal heritage."
            )
        )
    }

    suspend fun clearSession() {
        dao.clearSession()
    }

    fun getUserProfile(phoneNumber: String): Flow<UserProfileEntity?> = dao.getUserProfile(phoneNumber)

    suspend fun updateUserProfile(profile: UserProfileEntity) {
        dao.saveUserProfile(profile)
    }
}
