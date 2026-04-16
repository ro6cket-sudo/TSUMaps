package com.example.tsumaps.core

data class Place(
    val id: Int,
    val name: String,
    val description: String,
    val lat: Double,
    val lon: Double,
    val type: PlaceType,
    val openTime: String,
    val closeTime: String
    )
