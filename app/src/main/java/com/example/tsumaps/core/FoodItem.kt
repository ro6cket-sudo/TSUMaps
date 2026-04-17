package com.example.tsumaps.core

enum class FoodItem(val bit: Int, val label: String) {
    // --- НАПИТКИ ---
    COFFEE(1 shl 0, "Кофе"),
    TEA(1 shl 1, "Чай"),
    COLD_DRINKS(1 shl 2, "Холодные напитки"),

    // --- ФАСТФУД ---
    SHAWARMA(1 shl 3, "Шаурма"),
    BURGERS(1 shl 4, "Бургеры"),
    PIZZA(1 shl 5, "Пицца"),
    FRIED_CHICKEN(1 shl 6, "Жареная курица"),
    PANCAKES(1 shl 7, "Блины"),
    HOT_DOGS(1 shl 8, "Хот-доги"),

    // --- АЗИЯ ---
    SUSHI_ROLLS(1 shl 9, "Суши / Роллы"),
    WOK_NOODLES(1 shl 10, "Вок / Лапша"),
    ASIAN_SOUPS(1 shl 11, "Азиатские супы"),
    DUMPLINGS(1 shl 12, "Пельмени / Дим-сам"),
    SEAFOOD(1 shl 13, "Морепродукты"),

    // --- СЛАДКОЕ ---
    DESSERTS(1 shl 14, "Десерты"),
    BAKERY(1 shl 15, "Выпечка"),
    ICE_CREAM(1 shl 16, "Мороженое"),
    WAFFLES(1 shl 17, "Вафли"),

    // --- ОСНОВНАЯ ЕДА ---
    SOUPS(1 shl 18, "Супы"),
    SALADS(1 shl 19, "Салаты"),
    HOT_MEALS(1 shl 20, "Горячие блюда"),
    MEAT_GRILL(1 shl 21, "Мясо / Гриль"),
    POTATO_DISHES(1 shl 22, "Блюда из картофеля"),
    BREAKFASTS(1 shl 23, "Завтраки"),
    BUSINESS_LUNCH(1 shl 24, "Бизнес-ланч"),

    // --- АЛКОГОЛЬ ---
    BEER(1 shl 25, "Пиво"),
    STRONG_ALCOHOL(1 shl 26, "Крепкий алкоголь"),
    WINE(1 shl 27, "Вино"),
    COCKTAILS(1 shl 28, "Коктейли"),
    BAR_SNACKS(1 shl 29, "Закуски к бару"),

    // --- МАГАЗИНЫ ---
    GROCERIES(1 shl 30, "Продукты"),
    READY_TO_EAT(1 shl 31, "Готовая еда")
}