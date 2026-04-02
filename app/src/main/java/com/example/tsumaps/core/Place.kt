package com.example.tsumaps.core

import com.example.tsumaps.core.PlaceType
import kotlin.uuid.Uuid

data class Place(
    val id: Int,
    val name: String,
    val description: String,
    val point: Point,
    val type: PlaceType,
    val menu: Set<FoodItem>,
    val openTime: Int,
    val closeTime: Int
)
