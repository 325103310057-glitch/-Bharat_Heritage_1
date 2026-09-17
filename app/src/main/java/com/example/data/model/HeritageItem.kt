package com.example.data.model

enum class HeritageCategory(val title: String, val iconName: String) {
    ALL("All Heritage", "Landmark"),
    MONUMENTS("Monuments", "AccountBalance"),
    FORTS("Forts & Palaces", "Castle"),
    TEMPLES("Sacred Temples", "TempleHindu"),
    CAVES("Ancient Caves", "Terrain"),
    TRADITIONS("Culture & Arts", "TheaterComedy"),
    CITIES("Heritage Cities", "LocationCity")
}

data class HeritageItem(
    val id: String,
    val name: String,
    val hindiName: String,
    val category: HeritageCategory,
    val state: String,
    val city: String,
    val era: String,
    val constructedBy: String,
    val yearBuilt: String,
    val shortDescription: String,
    val fullHistory: String,
    val culturalSignificance: String,
    val architectureStyle: String,
    val quickFacts: List<String>,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double,
    val unescoWorldHeritage: Boolean = true,
    val isFavorite: Boolean = false
)
