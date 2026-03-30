package com.example.tsumaps.points

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tsumaps.points.FoodPlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_organizations ORDER BY name ASC")
    fun getAllFoodPlaces(): Flow<List<FoodPlaceEntity>>

    @Query("SELECT * FROM food_organizations WHERE category = :category")
    fun getFoodPlacesByCategory(category: String): Flow<List<FoodPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodPlace(foodPlace: FoodPlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foodPlaces: List<FoodPlaceEntity>)

    @Update
    suspend fun updateFoodPlace(foodPlace: FoodPlaceEntity)

    @Delete
    suspend fun deleteFoodPlace(foodPlace: FoodPlaceEntity)
}