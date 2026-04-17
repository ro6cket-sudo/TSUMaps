package com.example.tsumaps.core.algorithms.genetic

enum class MutationType(val label: String) {
    SWAP("Перестановка"),
    INVERSION("Инверсия")
}

enum class CrossoverType(val label: String) {
    ORDER("Порядковый"),
    ONE_POINT("Одноточечный")
}

enum class SelectionType(val label: String) {
    TOURNAMENT("Турнирный"),
    PROB_TOURNAMENT("Вер. турнир"),
    LINEAR_RANK("Линейный ранг")
}
