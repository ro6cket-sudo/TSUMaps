package com.example.tsumaps.core

import com.example.tsumaps.core.PlaceType
import kotlin.uuid.Uuid

private fun String.toMinutes(): Int {
    val parts = this.split(":")
    if (parts.size != 2) return 0
    val hours = parts[0].toIntOrNull() ?: 0
    val minutes = parts[1].toIntOrNull() ?: 0
    return hours * 60 + minutes
}


data class Place(
    val id: Int,
    val name: String,
    val description: String,
    val lat: Double,
    val lon: Double,
    val point: Point,
    val type: PlaceType,
    val menu: Set<FoodItem>,
    val openTime: String,
    val closeTime: String,
) {
    val openTimeInt: Int = openTime.toMinutes()
    val closeTimeInt: Int = closeTime.toMinutes()
    fun minutesUntilClose(currentTime: Int): Int = closeTimeInt - currentTime

    fun isOpen(currentTime: Int): Boolean =
        currentTime in openTimeInt until closeTimeInt
}
