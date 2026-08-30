package com.example.ui.theme

import androidx.compose.ui.graphics.Color

data class CategoryItem(
    val name: String,
    val emoji: String,
    val defaultColor: String,
    val composeColor: Color
)

object CalendarCategories {
    val ALL = listOf(
        CategoryItem("Work", "🏢", "#386B01", Color(0xFF386B01)),
        CategoryItem("Personal", "🏠", "#2E6B5E", Color(0xFF2E6B5E)),
        CategoryItem("Study", "🎓", "#825500", Color(0xFF825500)),
        CategoryItem("Finance", "💰", "#5F5B00", Color(0xFF5F5B00)),
        CategoryItem("Birthday", "🎉", "#9C4146", Color(0xFF9C4146)),
        CategoryItem("Fitness", "🏃", "#BA1A1A", Color(0xFFBA1A1A)),
        CategoryItem("Health", "🩺", "#226A79", Color(0xFF226A79)),
        CategoryItem("Creative", "🎨", "#8B5000", Color(0xFF8B5000))
    )

    val PRESET_COLORS = listOf(
        "#386B01", // Moss Green
        "#2E6B5E", // Forest Teal
        "#825500", // Amber Ochre
        "#5F5B00", // Olive Gold
        "#9C4146", // Terracotta Rose
        "#BA1A1A", // Crimson Red
        "#226A79", // Slate Teal
        "#8B5000", // Rust Sienna
        "#4F6546", // Sage Gray
        "#605F40"  // Editorial Stone
    )

    fun getCategory(name: String?): CategoryItem {
        return ALL.find { it.name.equals(name, ignoreCase = true) } ?: ALL[0]
    }

    fun parseColor(hex: String?, fallback: Color = Color(0xFF386B01)): Color {
        if (hex.isNullOrBlank()) return fallback
        return try {
            val cleanHex = hex.replace("#", "")
            val colorInt = if (cleanHex.length == 6) {
                "FF$cleanHex".toLong(16)
            } else {
                cleanHex.toLong(16)
            }
            Color(colorInt)
        } catch (e: Exception) {
            fallback
        }
    }
}
