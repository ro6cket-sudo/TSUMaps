package com.example.tsumaps.core

enum class FoodItem(val bit: Int) {
    /*

    ДОБАВЛЯТЬ НОВЫЕ КАТЕГОРИИ ПО АНАЛОГИИ СО СЛЕДУЮЩЕЙ СТРОЧКОЙ:

    NAME(1 shl X) , где X - это номер item в zero-based indexing, NAME - название

     */

    COFFEE(1 shl 0),
    PANCAKES(1 shl 1)
}