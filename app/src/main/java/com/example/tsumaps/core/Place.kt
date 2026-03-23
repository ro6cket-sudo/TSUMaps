package com.example.tsumaps.core

import kotlin.uuid.Uuid

data class Place(
    val id: Int,
    val name: String,
    val description: String,
    val x: Int,
    val y: Int,
    val type: PlaceType
)
