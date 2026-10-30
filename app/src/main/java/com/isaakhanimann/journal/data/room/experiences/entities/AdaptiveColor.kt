package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.compose.ui.graphics.Color

enum class AdaptiveColor(
    private val lightColor: Color,
    private val darkColor: Color,
    val isPreferred: Boolean = false,
) {
    RED(
        lightColor = Color(red = 255, green = 59, blue = 48),
        darkColor = Color(red = 255, green = 69, blue = 58),
        isPreferred = true,
    ),
    ORANGE(
        lightColor = Color(red = 255, green = 149, blue = 0),
        darkColor = Color(red = 255, green = 159, blue = 10),
        isPreferred = true,
    ),
    YELLOW(
        lightColor = Color(red = 255, green = 204, blue = 0),
        darkColor = Color(red = 255, green = 214, blue = 10),
        isPreferred = true,
    ),
    GREEN(
        lightColor = Color(red = 52, green = 199, blue = 89),
        darkColor = Color(red = 48, green = 209, blue = 88),
        isPreferred = true,
    ),
    MINT(
        lightColor = Color(red = 0, green = 199, blue = 190),
        darkColor = Color(red = 102, green = 212, blue = 207),
        isPreferred = true,
    ),
    TEAL(
        lightColor = Color(red = 48, green = 176, blue = 199),
        darkColor = Color(red = 64, green = 200, blue = 224),
        isPreferred = true,
    ),
    CYAN(
        lightColor = Color(red = 50, green = 173, blue = 230),
        darkColor = Color(red = 100, green = 210, blue = 255),
        isPreferred = true,
    ),
    BLUE(
        lightColor = Color(red = 0, green = 122, blue = 255),
        darkColor = Color(red = 10, green = 132, blue = 255),
        isPreferred = true,
    ),
    INDIGO(
        lightColor = Color(red = 88, green = 86, blue = 214),
        darkColor = Color(red = 94, green = 92, blue = 230),
        isPreferred = true,
    ),
    PURPLE(
        lightColor = Color(red = 175, green = 82, blue = 222),
        darkColor = Color(red = 191, green = 90, blue = 242),
        isPreferred = true,
    ),
    PINK(
        lightColor = Color(red = 255, green = 45, blue = 85),
        darkColor = Color(red = 255, green = 55, blue = 95),
        isPreferred = true,
    ),
    BROWN(
        lightColor = Color(red = 162, green = 132, blue = 94),
        darkColor = Color(red = 172, green = 142, blue = 104),
        isPreferred = true,
    ),
    FIRE_ENGINE_RED(
        lightColor = Color(red = 237, green = 14, blue = 6),
        darkColor = Color(red = 237, green = 43, blue = 42),
    ),
    CORAL(
        lightColor = Color(red = 180, green = 92, blue = 85),
        darkColor = Color(red = 255, green = 131, blue = 121),
    ),
    TOMATO(
        lightColor = Color(red = 180, green = 69, blue = 50),
        darkColor = Color(red = 255, green = 99, blue = 71),
    ),
    CINNABAR(
        lightColor = Color(red = 227, green = 36, blue = 0),
        darkColor = Color(red = 227, green = 36, blue = 0),
    ),
    RUST(
        lightColor = Color(red = 199, green = 81, blue = 58),
        darkColor = Color(red = 199, green = 81, blue = 58),
    ),
    ORANGE_RED(
        lightColor = Color(red = 205, green = 55, blue = 0),
        darkColor = Color(red = 255, green = 69, blue = 0),
    ),
    AUBURN(
        lightColor = Color(red = 173, green = 62, blue = 0),
        darkColor = Color(red = 217, green = 80, blue = 0),
    ),
    SADDLE_BROWN(
        lightColor = Color(red = 139, green = 69, blue = 19),
        darkColor = Color(red = 191, green = 95, blue = 25),
    ),
    DARK_ORANGE(
        lightColor = Color(red = 155, green = 84, blue = 0),
        darkColor = Color(red = 255, green = 140, blue = 0),
    ),
    DARK_GOLD(
        lightColor = Color(red = 169, green = 104, blue = 0),
        darkColor = Color(red = 169, green = 104, blue = 0),
    ),
    KHAKI(
        lightColor = Color(red = 128, green = 114, blue = 86),
        darkColor = Color(red = 203, green = 183, blue = 137),
    ),
    BRONZE(
        lightColor = Color(red = 120, green = 87, blue = 0),
        darkColor = Color(red = 167, green = 123, blue = 0),
    ),
    GOLD(
        lightColor = Color(red = 130, green = 109, blue = 0),
        darkColor = Color(red = 255, green = 215, blue = 0),
    ),
    OLIVE(
        lightColor = Color(red = 102, green = 97, blue = 0),
        darkColor = Color(red = 141, green = 134, blue = 0),
    ),
    OLIVE_DRAB(
        lightColor = Color(red = 111, green = 118, blue = 8),
        darkColor = Color(red = 154, green = 166, blue = 14),
    ),
    DARK_OLIVE_GREEN(
        lightColor = Color(red = 85, green = 107, blue = 47),
        darkColor = Color(red = 105, green = 133, blue = 58),
    ),
    MOSS_GREEN(
        lightColor = Color(red = 79, green = 122, blue = 40),
        darkColor = Color(red = 102, green = 156, blue = 53),
    ),
    LIME_GREEN(
        lightColor = Color(red = 0, green = 130, blue = 0),
        darkColor = Color(red = 0, green = 255, blue = 0),
    ),
    LIME(
        lightColor = Color(red = 32, green = 130, blue = 32),
        darkColor = Color(red = 50, green = 205, blue = 50),
    ),
    FOREST_GREEN(
        lightColor = Color(red = 28, green = 114, blue = 28),
        darkColor = Color(red = 34, green = 139, blue = 34),
    ),
    SEA_GREEN(
        lightColor = Color(red = 38, green = 114, blue = 71),
        darkColor = Color(red = 46, green = 139, blue = 87),
    ),
    JUNGLE_GREEN(
        lightColor = Color(red = 3, green = 136, blue = 88),
        darkColor = Color(red = 3, green = 136, blue = 88),
    ),
    LIGHT_SEA_GREEN(
        lightColor = Color(red = 22, green = 128, blue = 122),
        darkColor = Color(red = 32, green = 178, blue = 170),
    ),
    DARK_TURQUOISE(
        lightColor = Color(red = 0, green = 131, blue = 134),
        darkColor = Color(red = 0, green = 206, blue = 209),
    ),
    DODGER_BLUE(
        lightColor = Color(red = 24, green = 116, blue = 205),
        darkColor = Color(red = 30, green = 144, blue = 255),
    ),
    ROYAL_BLUE(
        lightColor = Color(red = 65, green = 105, blue = 225),
        darkColor = Color(red = 72, green = 117, blue = 251),
    ),
    DEEP_LAVENDER(
        lightColor = Color(red = 135, green = 78, blue = 254),
        darkColor = Color(red = 135, green = 78, blue = 254),
    ),
    BLUE_VIOLET(
        lightColor = Color(red = 138, green = 43, blue = 226),
        darkColor = Color(red = 166, green = 73, blue = 252),
    ),
    DARK_VIOLET(
        lightColor = Color(red = 148, green = 0, blue = 211),
        darkColor = Color(red = 162, green = 76, blue = 210),
    ),
    HELIOTROPE(
        lightColor = Color(red = 151, green = 93, blue = 175),
        darkColor = Color(red = 151, green = 93, blue = 175),
    ),
    BYZANTIUM(
        lightColor = Color(red = 153, green = 41, blue = 189),
        darkColor = Color(red = 190, green = 56, blue = 243),
    ),
    MAGENTA(
        lightColor = Color(red = 205, green = 0, blue = 205),
        darkColor = Color(red = 255, green = 0, blue = 255),
    ),
    DARK_MAGENTA(
        lightColor = Color(red = 139, green = 0, blue = 139),
        darkColor = Color(red = 217, green = 0, blue = 217),
    ),
    FUCHSIA(
        lightColor = Color(red = 189, green = 60, blue = 129),
        darkColor = Color(red = 214, green = 68, blue = 146),
    ),
    DEEP_PINK(
        lightColor = Color(red = 205, green = 16, blue = 117),
        darkColor = Color(red = 255, green = 20, blue = 147),
    ),
    GRAYISH_MAGENTA(
        lightColor = Color(red = 161, green = 96, blue = 128),
        darkColor = Color(red = 161, green = 96, blue = 128),
    ),
    HOT_PINK(
        lightColor = Color(red = 180, green = 74, blue = 126),
        darkColor = Color(red = 255, green = 105, blue = 180),
    ),
    JAZZBERRY_JAM(
        lightColor = Color(red = 185, green = 45, blue = 93),
        darkColor = Color(red = 230, green = 59, blue = 122),
    ),
    MAROON(
        lightColor = Color(red = 190, green = 49, blue = 68),
        darkColor = Color(red = 187, green = 82, blue = 99),
    ),
    GARNET_NOIR(
        lightColor = Color(red = 102, green = 0, blue = 0),
        darkColor = Color(red = 145, green = 71, blue = 71),
    ),
    SMOKED_ROSEWOOD(
        lightColor = Color(red = 68, green = 51, blue = 51),
        darkColor = Color(red = 120, green = 108, blue = 108),
    ),
    ROSE_QUARTZ(
        lightColor = Color(red = 189, green = 135, blue = 135),
        darkColor = Color(red = 243, green = 194, blue = 194),
    ),
    SUNSET_APRICOT(
        lightColor = Color(red = 200, green = 134, blue = 80),
        darkColor = Color(red = 255, green = 194, blue = 145),
    ),
    HONEY_SAFFRON(
        lightColor = Color(red = 170, green = 147, blue = 80),
        darkColor = Color(red = 255, green = 231, blue = 157),
    ),
    MIDNIGHT_OLIVE(
        lightColor = Color(red = 51, green = 51, blue = 0),
        darkColor = Color(red = 108, green = 108, blue = 71),
    ),
    NEON_LEMON(
        lightColor = Color(red = 145, green = 155, blue = 63),
        darkColor = Color(red = 243, green = 255, blue = 145),
    ),
    ELECTRIC_CHARTREUSE(
        lightColor = Color(red = 129, green = 160, blue = 0),
        darkColor = Color(red = 218, green = 255, blue = 71),
    ),
    MOSS_APPLE(
        lightColor = Color(red = 105, green = 165, blue = 0),
        darkColor = Color(red = 157, green = 206, blue = 71),
    ),
    SPRING_BUD(
        lightColor = Color(red = 109, green = 163, blue = 66),
        darkColor = Color(red = 194, green = 255, blue = 145),
    ),
    PALE_PISTACHIO(
        lightColor = Color(red = 127, green = 158, blue = 105),
        darkColor = Color(red = 218, green = 255, blue = 194),
    ),
    SILVER_SAGE(
        lightColor = Color(red = 125, green = 157, blue = 125),
        darkColor = Color(red = 169, green = 194, blue = 169),
    ),
    FROSTED_MINT(
        lightColor = Color(red = 141, green = 151, blue = 141),
        darkColor = Color(red = 243, green = 255, blue = 243),
    ),
    BLACK_PINE(
        lightColor = Color(red = 0, green = 34, blue = 17),
        darkColor = Color(red = 71, green = 96, blue = 84),
    ),
    SEAFOAM_JADE(
        lightColor = Color(red = 88, green = 163, blue = 126),
        darkColor = Color(red = 157, green = 231, blue = 194),
    ),
    TROPICAL_AQUA(
        lightColor = Color(red = 0, green = 169, blue = 125),
        darkColor = Color(red = 71, green = 255, blue = 206),
    ),
    DEEP_HARBOR_BLUE(
        lightColor = Color(red = 0, green = 51, blue = 119),
        darkColor = Color(red = 71, green = 108, blue = 157),
    ),
    STORM_SLATE_BLUE(
        lightColor = Color(red = 68, green = 85, blue = 119),
        darkColor = Color(red = 120, green = 133, blue = 157),
    ),
    ABYSSAL_NAVY(
        lightColor = Color(red = 0, green = 0, blue = 34),
        darkColor = Color(red = 87, green = 87, blue = 117),
    ),
    MIDNIGHT_COBALT(
        lightColor = Color(red = 0, green = 0, blue = 85),
        darkColor = Color(red = 82, green = 82, blue = 153),
    ),
    SATURATED_COBALT(
        lightColor = Color(red = 0, green = 0, blue = 153),
        darkColor = Color(red = 74, green = 74, blue = 189),
    ),
    LASER_BLUE(
        lightColor = Color(red = 0, green = 0, blue = 255),
        darkColor = Color(red = 71, green = 71, blue = 255),
    ),
    DUSTY_PERIWINKLE(
        lightColor = Color(red = 147, green = 147, blue = 164),
        darkColor = Color(red = 182, green = 182, blue = 194),
    ),
    SOFT_PERIWINKLE(
        lightColor = Color(red = 143, green = 143, blue = 201),
        darkColor = Color(red = 194, green = 194, blue = 243),
    ),
    COTTON_CANDY_MAGENTA(
        lightColor = Color(red = 191, green = 126, blue = 191),
        darkColor = Color(red = 255, green = 194, blue = 255),
    ),
    LILAC_MIST(
        lightColor = Color(red = 164, green = 142, blue = 164),
        darkColor = Color(red = 255, green = 231, blue = 255),
    ),
    ELECTRIC_ORCHID(
        lightColor = Color(red = 222, green = 104, blue = 208),
        darkColor = Color(red = 255, green = 157, blue = 243),
    ),
    DEEP_PLUM_WINE(
        lightColor = Color(red = 85, green = 0, blue = 68),
        darkColor = Color(red = 133, green = 71, blue = 120),
    ),
    RADIANT_MULBERRY(
        lightColor = Color(red = 221, green = 17, blue = 170),
        darkColor = Color(red = 231, green = 84, blue = 194),
    ),
    BLACK_CHERRY(
        lightColor = Color(red = 68, green = 0, blue = 17),
        darkColor = Color(red = 126, green = 74, blue = 88),
    ),
    ACID_LIME(
        lightColor = Color(red = 94, green = 156, blue = 7),
        darkColor = Color(red = 139, green = 184, blue = 76),
    ),
    WASABI_ZING(
        lightColor = Color(red = 92, green = 167, blue = 0),
        darkColor = Color(red = 156, green = 225, blue = 71),
    ),
    CITRUS_LEAF(
        lightColor = Color(red = 110, green = 163, blue = 43),
        darkColor = Color(red = 161, green = 205, blue = 107),
    ),
    VERDANT_CHARTREUSE(
        lightColor = Color(red = 95, green = 166, blue = 53),
        darkColor = Color(red = 157, green = 222, blue = 119),
    ),
    KIWI_PUNCH(
        lightColor = Color(red = 55, green = 148, blue = 5),
        darkColor = Color(red = 111, green = 178, blue = 75),
    ),
    PERIDOT_FLARE(
        lightColor = Color(red = 59, green = 170, blue = 2),
        darkColor = Color(red = 126, green = 231, blue = 74),
    ),
    APPLE_ZEST(
        lightColor = Color(red = 77, green = 168, blue = 41),
        darkColor = Color(red = 128, green = 195, blue = 102),
    ),
    LIME_SPARK(
        lightColor = Color(red = 40, green = 172, blue = 17),
        darkColor = Color(red = 101, green = 198, blue = 84),
    ),
    HERBAL_NEON(
        lightColor = Color(red = 33, green = 172, blue = 38),
        darkColor = Color(red = 102, green = 231, blue = 107),
    ),
    CLOVER_GLOW(
        lightColor = Color(red = 18, green = 155, blue = 27),
        darkColor = Color(red = 84, green = 183, blue = 91),
    ),
    AUREATE_GOLD(
        lightColor = Color(red = 153, green = 117, blue = 18),
        darkColor = Color(red = 255, green = 195, blue = 31),
    ),
    SAFFRON_BLAZE(
        lightColor = Color(red = 166, green = 150, blue = 7),
        darkColor = Color(red = 255, green = 231, blue = 10),
    ),
    AMBER_BURST(
        lightColor = Color(red = 115, green = 111, blue = 11),
        darkColor = Color(red = 255, green = 247, blue = 25),
    ),
    MARIGOLD_FLARE(
        lightColor = Color(red = 143, green = 148, blue = 3),
        darkColor = Color(red = 247, green = 255, blue = 5),
    ),
    SUNLIT_OCHRE(
        lightColor = Color(red = 116, green = 128, blue = 15),
        darkColor = Color(red = 233, green = 255, blue = 31),
    ),
    BRONZED_HONEY(
        lightColor = Color(red = 94, green = 115, blue = 0),
        darkColor = Color(red = 208, green = 255, blue = 0),
    ),
    CELESTIAL_AZURE(
        lightColor = Color(red = 39, green = 152, blue = 232),
        darkColor = Color(red = 43, green = 167, blue = 255),
    ),
    COBALT_SURGE(
        lightColor = Color(red = 69, green = 141, blue = 196),
        darkColor = Color(red = 89, green = 183, blue = 255),
    ),
    DEEP_SKY_CERULEAN(
        lightColor = Color(red = 4, green = 37, blue = 89),
        darkColor = Color(red = 13, green = 106, blue = 255),
    ),
    ELECTRIC_SAPPHIRE(
        lightColor = Color(red = 1, green = 52, blue = 145),
        darkColor = Color(red = 3, green = 91, blue = 255),
    ),
    MIDNIGHT_AZURE(
        lightColor = Color(red = 98, green = 85, blue = 242),
        darkColor = Color(red = 103, green = 89, blue = 255),
    ),
    ULTRAMARINE_GLOW(
        lightColor = Color(red = 40, green = 22, blue = 130),
        darkColor = Color(red = 79, green = 43, blue = 255),
    ),
    AZURE_TIDE(
        lightColor = Color(red = 9, green = 78, blue = 107),
        darkColor = Color(red = 20, green = 185, blue = 255),
    ),
    CERULEAN_SURGE(
        lightColor = Color(red = 5, green = 88, blue = 130),
        darkColor = Color(red = 10, green = 173, blue = 255),
    ),
    COBALT_CURRENT(
        lightColor = Color(red = 2, green = 65, blue = 107),
        darkColor = Color(red = 5, green = 155, blue = 255),
    ),
    SAPPHIRE_WAVE(
        lightColor = Color(red = 2, green = 62, blue = 115),
        darkColor = Color(red = 5, green = 138, blue = 255),
    ),
    DEEP_AZURE_EDGE(
        lightColor = Color(red = 0, green = 48, blue = 107),
        darkColor = Color(red = 0, green = 115, blue = 255),
    ),
    ROYAL_COBALT(
        lightColor = Color(red = 0, green = 47, blue = 122),
        darkColor = Color(red = 0, green = 98, blue = 255),
    ),
    ELECTRIC_COBALT(
        lightColor = Color(red = 3, green = 49, blue = 156),
        darkColor = Color(red = 5, green = 80, blue = 255),
    ),
    NIGHT_AZURE(
        lightColor = Color(red = 2, green = 25, blue = 107),
        darkColor = Color(red = 5, green = 59, blue = 255),
    );

    fun getComposeColor(isDarkTheme: Boolean): Color =
        if (isDarkTheme) darkColor else lightColor
}
