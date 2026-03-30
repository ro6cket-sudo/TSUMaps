package com.example.tsumaps.points

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tsumaps.points.FoodPlaceEntity
import com.example.tsumaps.points.FoodDao

@Database(entities = [FoodPlaceEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract  fun foodDao(): FoodDao
}