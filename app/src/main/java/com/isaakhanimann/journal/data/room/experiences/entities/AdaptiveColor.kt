package com.isaakhanimann.journal.data.room.experiences.entities

import androidx.compose.ui.graphics.Color

enum class AdaptiveColor {
    RED {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 69, blue = 58)
            } else {
                Color(red = 255, green = 59, blue = 48)
            }
        }

        override val isPreferred = true
    },
    ORANGE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 159, blue = 10)
            } else {
                Color(red = 255, green = 149, blue = 0)
            }
        }

        override val isPreferred = true
    },
    YELLOW {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 214, blue = 10)
            } else {
                Color(red = 255, green = 204, blue = 0)
            }
        }

        override val isPreferred = true
    },
    GREEN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 48, green = 209, blue = 88)
            } else {
                Color(red = 52, green = 199, blue = 89)
            }
        }

        override val isPreferred = true
    },
    MINT {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 102, green = 212, blue = 207)
            } else {
                Color(red = 0, green = 199, blue = 190)
            }
        }

        override val isPreferred = true
    },
    TEAL {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 64, green = 200, blue = 224)
            } else {
                Color(red = 48, green = 176, blue = 199)
            }
        }

        override val isPreferred = true
    },
    CYAN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 100, green = 210, blue = 255)
            } else {
                Color(red = 50, green = 173, blue = 230)
            }
        }

        override val isPreferred = true
    },
    BLUE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 10, green = 132, blue = 255)
            } else {
                Color(red = 0, green = 122, blue = 255)
            }
        }

        override val isPreferred = true
    },
    INDIGO {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 94, green = 92, blue = 230)
            } else {
                Color(red = 88, green = 86, blue = 214)
            }
        }

        override val isPreferred = true
    },
    PURPLE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 191, green = 90, blue = 242)
            } else {
                Color(red = 175, green = 82, blue = 222)
            }
        }

        override val isPreferred = true
    },
    PINK {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 55, blue = 95)
            } else {
                Color(red = 255, green = 45, blue = 85)
            }
        }

        override val isPreferred = true
    },
    BROWN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 172, green = 142, blue = 104)
            } else {
                Color(red = 162, green = 132, blue = 94)
            }
        }

        override val isPreferred = true
    },
    FIRE_ENGINE_RED {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 237, green = 43, blue = 42)
            } else {
                Color(red = 237, green = 14, blue = 6)
            }
        }

        override val isPreferred = false
    },
    CORAL {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 131, blue = 121)
            } else {
                Color(red = 180, green = 92, blue = 85)
            }
        }

        override val isPreferred = false
    },
    TOMATO {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 99, blue = 71)
            } else {
                Color(red = 180, green = 69, blue = 50)
            }
        }

        override val isPreferred = false
    },
    CINNABAR {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 227, green = 36, blue = 0)
            } else {
                Color(red = 227, green = 36, blue = 0)
            }
        }

        override val isPreferred = false
    },
    RUST {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 199, green = 81, blue = 58)
            } else {
                Color(red = 199, green = 81, blue = 58)
            }
        }

        override val isPreferred = false
    },
    ORANGE_RED {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 69, blue = 0)
            } else {
                Color(red = 205, green = 55, blue = 0)
            }
        }

        override val isPreferred = false
    },
    AUBURN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 217, green = 80, blue = 0)
            } else {
                Color(red = 173, green = 62, blue = 0)
            }
        }

        override val isPreferred = false
    },
    SADDLE_BROWN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 191, green = 95, blue = 25)
            } else {
                Color(red = 139, green = 69, blue = 19)
            }
        }

        override val isPreferred = false
    },
    DARK_ORANGE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 140, blue = 0)
            } else {
                Color(red = 155, green = 84, blue = 0)
            }
        }

        override val isPreferred = false
    },
    DARK_GOLD {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 169, green = 104, blue = 0)
            } else {
                Color(red = 169, green = 104, blue = 0)
            }
        }

        override val isPreferred = false
    },
    KHAKI {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 203, green = 183, blue = 137)
            } else {
                Color(red = 128, green = 114, blue = 86)
            }
        }

        override val isPreferred = false
    },
    BRONZE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 167, green = 123, blue = 0)
            } else {
                Color(red = 120, green = 87, blue = 0)
            }
        }

        override val isPreferred = false
    },
    GOLD {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 215, blue = 0)
            } else {
                Color(red = 130, green = 109, blue = 0)
            }
        }

        override val isPreferred = false
    },
    OLIVE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 141, green = 134, blue = 0)
            } else {
                Color(red = 102, green = 97, blue = 0)
            }
        }

        override val isPreferred = false
    },
    OLIVE_DRAB {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 154, green = 166, blue = 14)
            } else {
                Color(red = 111, green = 118, blue = 8)
            }
        }

        override val isPreferred = false
    },
    DARK_OLIVE_GREEN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 105, green = 133, blue = 58)
            } else {
                Color(red = 85, green = 107, blue = 47)
            }
        }

        override val isPreferred = false
    },
    MOSS_GREEN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 102, green = 156, blue = 53)
            } else {
                Color(red = 79, green = 122, blue = 40)
            }
        }

        override val isPreferred = false
    },
    LIME_GREEN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 0, green = 255, blue = 0)
            } else {
                Color(red = 0, green = 130, blue = 0)
            }
        }

        override val isPreferred = false
    },
    LIME {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 50, green = 205, blue = 50)
            } else {
                Color(red = 32, green = 130, blue = 32)
            }
        }

        override val isPreferred = false
    },
    FOREST_GREEN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 34, green = 139, blue = 34)
            } else {
                Color(red = 28, green = 114, blue = 28)
            }
        }

        override val isPreferred = false
    },
    SEA_GREEN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 46, green = 139, blue = 87)
            } else {
                Color(red = 38, green = 114, blue = 71)
            }
        }

        override val isPreferred = false
    },
    JUNGLE_GREEN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 3, green = 136, blue = 88)
            } else {
                Color(red = 3, green = 136, blue = 88)
            }
        }

        override val isPreferred = false
    },
    LIGHT_SEA_GREEN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 32, green = 178, blue = 170)
            } else {
                Color(red = 22, green = 128, blue = 122)
            }
        }

        override val isPreferred = false
    },
    DARK_TURQUOISE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 0, green = 206, blue = 209)
            } else {
                Color(red = 0, green = 131, blue = 134)
            }
        }

        override val isPreferred = false
    },
    DODGER_BLUE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 30, green = 144, blue = 255)
            } else {
                Color(red = 24, green = 116, blue = 205)
            }
        }

        override val isPreferred = false
    },
    ROYAL_BLUE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 72, green = 117, blue = 251)
            } else {
                Color(red = 65, green = 105, blue = 225)
            }
        }

        override val isPreferred = false
    },
    DEEP_LAVENDER {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 135, green = 78, blue = 254)
            } else {
                Color(red = 135, green = 78, blue = 254)
            }
        }

        override val isPreferred = false
    },
    BLUE_VIOLET {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 166, green = 73, blue = 252)
            } else {
                Color(red = 138, green = 43, blue = 226)
            }
        }

        override val isPreferred = false
    },
    DARK_VIOLET {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 162, green = 76, blue = 210)
            } else {
                Color(red = 148, green = 0, blue = 211)
            }
        }

        override val isPreferred = false
    },
    HELIOTROPE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 151, green = 93, blue = 175)
            } else {
                Color(red = 151, green = 93, blue = 175)
            }
        }

        override val isPreferred = false
    },
    BYZANTIUM {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 190, green = 56, blue = 243)
            } else {
                Color(red = 153, green = 41, blue = 189)
            }
        }

        override val isPreferred = false
    },
    MAGENTA {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 0, blue = 255)
            } else {
                Color(red = 205, green = 0, blue = 205)
            }
        }

        override val isPreferred = false
    },
    DARK_MAGENTA {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 217, green = 0, blue = 217)
            } else {
                Color(red = 139, green = 0, blue = 139)
            }
        }

        override val isPreferred = false
    },
    FUCHSIA {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 214, green = 68, blue = 146)
            } else {
                Color(red = 189, green = 60, blue = 129)
            }
        }

        override val isPreferred = false
    },
    DEEP_PINK {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 20, blue = 147)
            } else {
                Color(red = 205, green = 16, blue = 117)
            }
        }

        override val isPreferred = false
    },
    GRAYISH_MAGENTA {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 161, green = 96, blue = 128)
            } else {
                Color(red = 161, green = 96, blue = 128)
            }
        }

        override val isPreferred = false
    },
    HOT_PINK {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 105, blue = 180)
            } else {
                Color(red = 180, green = 74, blue = 126)
            }
        }

        override val isPreferred = false
    },
    JAZZBERRY_JAM {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 230, green = 59, blue = 122)
            } else {
                Color(red = 185, green = 45, blue = 93)
            }
        }

        override val isPreferred = false
    },
    MAROON {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 187, green = 82, blue = 99)
            } else {
                Color(red = 190, green = 49, blue = 68)
            }
        }

        override val isPreferred = false
    },
    GARNET_NOIR {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 145, green = 71, blue = 71)
            } else {
                Color(red = 102, green = 0, blue = 0)
            }
        }

        override val isPreferred = false
    },
    SMOKED_ROSEWOOD {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 120, green = 108, blue = 108)
            } else {
                Color(red = 68, green = 51, blue = 51)
            }
        }

        override val isPreferred = false
    },
    ROSE_QUARTZ {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 243, green = 194, blue = 194)
            } else {
                Color(red = 189, green = 135, blue = 135)
            }
        }

        override val isPreferred = false
    },
    SUNSET_APRICOT {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 194, blue = 145)
            } else {
                Color(red = 200, green = 134, blue = 80)
            }
        }

        override val isPreferred = false
    },
    HONEY_SAFFRON {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 231, blue = 157)
            } else {
                Color(red = 170, green = 147, blue = 80)
            }
        }

        override val isPreferred = false
    },
    MIDNIGHT_OLIVE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 108, green = 108, blue = 71)
            } else {
                Color(red = 51, green = 51, blue = 0)
            }
        }

        override val isPreferred = false
    },
    NEON_LEMON {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 243, green = 255, blue = 145)
            } else {
                Color(red = 145, green = 155, blue = 63)
            }
        }

        override val isPreferred = false
    },
    ELECTRIC_CHARTREUSE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 218, green = 255, blue = 71)
            } else {
                Color(red = 129, green = 160, blue = 0)
            }
        }

        override val isPreferred = false
    },
    MOSS_APPLE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 157, green = 206, blue = 71)
            } else {
                Color(red = 105, green = 165, blue = 0)
            }
        }

        override val isPreferred = false
    },
    SPRING_BUD {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 194, green = 255, blue = 145)
            } else {
                Color(red = 109, green = 163, blue = 66)
            }
        }

        override val isPreferred = false
    },
    PALE_PISTACHIO {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 218, green = 255, blue = 194)
            } else {
                Color(red = 127, green = 158, blue = 105)
            }
        }

        override val isPreferred = false
    },
    SILVER_SAGE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 169, green = 194, blue = 169)
            } else {
                Color(red = 125, green = 157, blue = 125)
            }
        }

        override val isPreferred = false
    },
    FROSTED_MINT {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 243, green = 255, blue = 243)
            } else {
                Color(red = 141, green = 151, blue = 141)
            }
        }

        override val isPreferred = false
    },
    BLACK_PINE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 71, green = 96, blue = 84)
            } else {
                Color(red = 0, green = 34, blue = 17)
            }
        }

        override val isPreferred = false
    },
    SEAFOAM_JADE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 157, green = 231, blue = 194)
            } else {
                Color(red = 88, green = 163, blue = 126)
            }
        }

        override val isPreferred = false
    },
    TROPICAL_AQUA {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 71, green = 255, blue = 206)
            } else {
                Color(red = 0, green = 169, blue = 125)
            }
        }

        override val isPreferred = false
    },
    DEEP_HARBOR_BLUE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 71, green = 108, blue = 157)
            } else {
                Color(red = 0, green = 51, blue = 119)
            }
        }

        override val isPreferred = false
    },
    STORM_SLATE_BLUE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 120, green = 133, blue = 157)
            } else {
                Color(red = 68, green = 85, blue = 119)
            }
        }

        override val isPreferred = false
    },
    ABYSSAL_NAVY {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 87, green = 87, blue = 117)
            } else {
                Color(red = 0, green = 0, blue = 34)
            }
        }

        override val isPreferred = false
    },
    MIDNIGHT_COBALT {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 82, green = 82, blue = 153)
            } else {
                Color(red = 0, green = 0, blue = 85)
            }
        }

        override val isPreferred = false
    },
    SATURATED_COBALT {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 74, green = 74, blue = 189)
            } else {
                Color(red = 0, green = 0, blue = 153)
            }
        }

        override val isPreferred = false
    },
    LASER_BLUE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 71, green = 71, blue = 255)
            } else {
                Color(red = 0, green = 0, blue = 255)
            }
        }

        override val isPreferred = false
    },
    DUSTY_PERIWINKLE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 182, green = 182, blue = 194)
            } else {
                Color(red = 147, green = 147, blue = 164)
            }
        }

        override val isPreferred = false
    },
    SOFT_PERIWINKLE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 194, green = 194, blue = 243)
            } else {
                Color(red = 143, green = 143, blue = 201)
            }
        }

        override val isPreferred = false
    },
    COTTON_CANDY_MAGENTA {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 194, blue = 255)
            } else {
                Color(red = 191, green = 126, blue = 191)
            }
        }

        override val isPreferred = false
    },
    LILAC_MIST {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 231, blue = 255)
            } else {
                Color(red = 164, green = 142, blue = 164)
            }
        }

        override val isPreferred = false
    },
    ELECTRIC_ORCHID {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 157, blue = 243)
            } else {
                Color(red = 222, green = 104, blue = 208)
            }
        }

        override val isPreferred = false
    },
    DEEP_PLUM_WINE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 133, green = 71, blue = 120)
            } else {
                Color(red = 85, green = 0, blue = 68)
            }
        }

        override val isPreferred = false
    },
    RADIANT_MULBERRY {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 231, green = 84, blue = 194)
            } else {
                Color(red = 221, green = 17, blue = 170)
            }
        }

        override val isPreferred = false
    },
    BLACK_CHERRY {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 126, green = 74, blue = 88)
            } else {
                Color(red = 68, green = 0, blue = 17)
            }
        }

        override val isPreferred = false
    },
    ACID_LIME {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 139, green = 184, blue = 76)
            } else {
                Color(red = 94, green = 156, blue = 7)
            }
        }

        override val isPreferred = false
    },
    WASABI_ZING {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 156, green = 225, blue = 71)
            } else {
                Color(red = 92, green = 167, blue = 0)
            }
        }

        override val isPreferred = false
    },
    CITRUS_LEAF {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 161, green = 205, blue = 107)
            } else {
                Color(red = 110, green = 163, blue = 43)
            }
        }

        override val isPreferred = false
    },
    VERDANT_CHARTREUSE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 157, green = 222, blue = 119)
            } else {
                Color(red = 95, green = 166, blue = 53)
            }
        }

        override val isPreferred = false
    },
    KIWI_PUNCH {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 111, green = 178, blue = 75)
            } else {
                Color(red = 55, green = 148, blue = 5)
            }
        }

        override val isPreferred = false
    },
    PERIDOT_FLARE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 126, green = 231, blue = 74)
            } else {
                Color(red = 59, green = 170, blue = 2)
            }
        }

        override val isPreferred = false
    },
    APPLE_ZEST {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 128, green = 195, blue = 102)
            } else {
                Color(red = 77, green = 168, blue = 41)
            }
        }

        override val isPreferred = false
    },
    LIME_SPARK {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 101, green = 198, blue = 84)
            } else {
                Color(red = 40, green = 172, blue = 17)
            }
        }

        override val isPreferred = false
    },
    HERBAL_NEON {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 102, green = 231, blue = 107)
            } else {
                Color(red = 33, green = 172, blue = 38)
            }
        }

        override val isPreferred = false
    },
    CLOVER_GLOW {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 84, green = 183, blue = 91)
            } else {
                Color(red = 18, green = 155, blue = 27)
            }
        }

        override val isPreferred = false
    },
    AUREATE_GOLD {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 195, blue = 31)
            } else {
                Color(red = 153, green = 117, blue = 18)
            }
        }

        override val isPreferred = false
    },
    SAFFRON_BLAZE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 231, blue = 10)
            } else {
                Color(red = 166, green = 150, blue = 7)
            }
        }

        override val isPreferred = false
    },
    AMBER_BURST {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 255, green = 247, blue = 25)
            } else {
                Color(red = 115, green = 111, blue = 11)
            }
        }

        override val isPreferred = false
    },
    MARIGOLD_FLARE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 247, green = 255, blue = 5)
            } else {
                Color(red = 143, green = 148, blue = 3)
            }
        }

        override val isPreferred = false
    },
    SUNLIT_OCHRE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 233, green = 255, blue = 31)
            } else {
                Color(red = 116, green = 128, blue = 15)
            }
        }

        override val isPreferred = false
    },
    BRONZED_HONEY {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 208, green = 255, blue = 0)
            } else {
                Color(red = 94, green = 115, blue = 0)
            }
        }

        override val isPreferred = false
    },
    CELESTIAL_AZURE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 43, green = 167, blue = 255)
            } else {
                Color(red = 39, green = 152, blue = 232)
            }
        }

        override val isPreferred = false
    },
    COBALT_SURGE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 89, green = 183, blue = 255)
            } else {
                Color(red = 69, green = 141, blue = 196)
            }
        }

        override val isPreferred = false
    },
    DEEP_SKY_CERULEAN {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 13, green = 106, blue = 255)
            } else {
                Color(red = 4, green = 37, blue = 89)
            }
        }

        override val isPreferred = false
    },
    ELECTRIC_SAPPHIRE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 3, green = 91, blue = 255)
            } else {
                Color(red = 1, green = 52, blue = 145)
            }
        }

        override val isPreferred = false
    },
    MIDNIGHT_AZURE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 103, green = 89, blue = 255)
            } else {
                Color(red = 98, green = 85, blue = 242)
            }
        }

        override val isPreferred = false
    },
    ULTRAMARINE_GLOW {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 79, green = 43, blue = 255)
            } else {
                Color(red = 40, green = 22, blue = 130)
            }
        }

        override val isPreferred = false
    },
    AZURE_TIDE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 20, green = 185, blue = 255)
            } else {
                Color(red = 9, green = 78, blue = 107)
            }
        }

        override val isPreferred = false
    },
    CERULEAN_SURGE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 10, green = 173, blue = 255)
            } else {
                Color(red = 5, green = 88, blue = 130)
            }
        }

        override val isPreferred = false
    },
    COBALT_CURRENT {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 5, green = 155, blue = 255)
            } else {
                Color(red = 2, green = 65, blue = 107)
            }
        }

        override val isPreferred = false
    },
    SAPPHIRE_WAVE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 5, green = 138, blue = 255)
            } else {
                Color(red = 2, green = 62, blue = 115)
            }
        }

        override val isPreferred = false
    },
    DEEP_AZURE_EDGE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 0, green = 115, blue = 255)
            } else {
                Color(red = 0, green = 48, blue = 107)
            }
        }

        override val isPreferred = false
    },
    ROYAL_COBALT {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 0, green = 98, blue = 255)
            } else {
                Color(red = 0, green = 47, blue = 122)
            }
        }

        override val isPreferred = false
    },
    ELECTRIC_COBALT {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 5, green = 80, blue = 255)
            } else {
                Color(red = 3, green = 49, blue = 156)
            }
        }

        override val isPreferred = false
    },
    NIGHT_AZURE {
        override fun getComposeColor(isDarkTheme: Boolean): Color {
            return if (isDarkTheme) {
                Color(red = 5, green = 59, blue = 255)
            } else {
                Color(red = 2, green = 25, blue = 107)
            }
        }

        override val isPreferred = false
    };

    abstract fun getComposeColor(isDarkTheme: Boolean): Color
    abstract val isPreferred: Boolean
}
