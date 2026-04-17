package com.example.tsumaps.core

enum class FoodItem(val bit: Int) {
    // --- НАПИТКИ ---
    COFFEE(1 shl 0),
    TEA(1 shl 1),
    COLD_DRINKS(1 shl 2),

    // --- ФАСТФУД ---
    SHAWARMA(1 shl 3),
    BURGERS(1 shl 4),
    PIZZA(1 shl 5),
    FRIED_CHICKEN(1 shl 6),
    PANCAKES(1 shl 7),
    HOT_DOGS(1 shl 8),

    // --- АЗИЯ ---
    SUSHI_ROLLS(1 shl 9),
    WOK_NOODLES(1 shl 10),
    ASIAN_SOUPS(1 shl 11),
    DUMPLINGS(1 shl 12),
    SEAFOOD(1 shl 13),

    // --- СЛАДКОЕ ---
    DESSERTS(1 shl 14),
    BAKERY(1 shl 15),
    ICE_CREAM(1 shl 16),
    WAFFLES(1 shl 17),

    // --- ОСНОВНАЯ ЕДА ---
    SOUPS(1 shl 18),
    SALADS(1 shl 19),
    HOT_MEALS(1 shl 20),
    MEAT_GRILL(1 shl 21),
    POTATO_DISHES(1 shl 22),
    BREAKFASTS(1 shl 23),
    BUSINESS_LUNCH(1 shl 24),

    // --- АЛКОГОЛЬ ---
    BEER(1 shl 25),
    STRONG_ALCOHOL(1 shl 26),
    WINE(1 shl 27),
    COCKTAILS(1 shl 28),
    BAR_SNACKS(1 shl 29),

    // --- МАГАЗИНЫ ---
    GROCERIES(1 shl 30),
    READY_TO_EAT(1 shl 31)
}