package com.sanobld.laststats.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MicExternalOn
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PeopleAlt
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import com.sanobld.laststats.ui.theme.ColorMath

/** 9 ranks, low to high. The last one stays maxed out beyond its threshold. */
enum class CardTier { None, Bronze, Silver, Gold, Platinum, Emerald, Sapphire, Diamond, Chrome, Iridescent }

enum class AchvCategory { Listening, Artists, Albums, Tracks, Loyalty, Pace, Streak, Marathon, Social, Comparisons }

/**
 * One milestone. [threshold] is a number of scrobbles, distinct items, years,
 * weekly scrobbles, days, friends or comparisons, depending on the category.
 */
data class AchievementDef(
    val id: String,
    val category: AchvCategory,
    val threshold: Int,
    val tier: CardTier,
    val icon: ImageVector,
)

data class AchievementProgress(
    val def: AchievementDef,
    val current: Int,
    val unlocked: Boolean,
)

/** Summary for a category card: value, highest tier reached and the next locked milestone. */
data class CategorySummary(
    val current: Int,
    val tier: CardTier,
    val next: AchievementProgress?,
)

/** Everything the achievements need, already counted by the caller. */
data class AchievementInput(
    val totalScrobbles: Int,
    val artistCount: Int,
    val albumCount: Int,
    val trackCount: Int,
    val yearsRegistered: Int,
    val weeklyAvg: Int,
    /** Own account only: 0 for friends (no raw history). */
    val bestStreakDays: Int = 0,
    val bestDayScrobbles: Int = 0,
    val friendsCount: Int = 0,
    val comparisonsCount: Int = 0,
)

/** Play count thresholds for the card border of a track, album or artist. */
val PlayTierThresholds = listOf(10, 25, 50, 100, 250, 500, 1000, 2500, 5000)

val PlayTierOrder = listOf(
    CardTier.Bronze, CardTier.Silver, CardTier.Gold, CardTier.Platinum, CardTier.Emerald,
    CardTier.Sapphire, CardTier.Diamond, CardTier.Chrome, CardTier.Iridescent,
)

/** The profile tier is the number of unlocked achievements on the same 9 rank scale. */
val ProfileTierThresholds = listOf(2, 4, 6, 8, 10, 12, 13, 15, 17)

private val tierBase: Map<CardTier, Color> = mapOf(
    CardTier.Bronze to Color(0xFFDBA67A),
    CardTier.Silver to Color(0xFFC6CBD4),
    CardTier.Gold to Color(0xFFEBC65E),
    CardTier.Platinum to Color(0xFFDCCFF0),
    CardTier.Emerald to Color(0xFF7ED3A2),
    CardTier.Sapphire to Color(0xFF8AAEF2),
    CardTier.Diamond to Color(0xFF93DDEE),
    CardTier.Chrome to Color(0xFF474C54),
)

private val iridescentTonal = listOf(
    Color(0xFFF2B8EE), Color(0xFFB9DAF7), Color(0xFFC6F0E0), Color(0xFFF5E2A8), Color(0xFFF2B8EE),
)

private fun shade(c: Color, dl: Float): Color {
    val hsl = FloatArray(3)
    ColorMath.colorToHSL(c.toArgb(), hsl)
    hsl[2] = (hsl[2] + dl).coerceIn(0f, 1f)
    return Color(ColorMath.HSLToColor(hsl))
}

/** Soft tonal gradient of a tier. Null for [CardTier.None]. */
fun tierGradient(t: CardTier): List<Color>? {
    if (t == CardTier.None) return null
    if (t == CardTier.Iridescent) return iridescentTonal
    val b = tierBase[t] ?: return null
    return listOf(shade(b, 0.05f), b, shade(b, 0.09f), b, shade(b, -0.06f))
}

/** Icon and text colour that reads on top of the tier colour. */
fun tierOnColor(t: CardTier): Color =
    if (t == CardTier.Chrome) Color(0xFFF1F2F4) else Color(0xFF2B2118)

/** Mid tone of the gradient, used as a soft tinted background. */
fun tierSoftColor(t: CardTier): Color? = tierGradient(t)?.first()

/** The 90 milestones, 9 per category, from bronze to iridescent. */
val Achievements: List<AchievementDef> = listOf(
        // Listening
        AchievementDef("l1", AchvCategory.Listening, 100, CardTier.Bronze, Icons.Rounded.Headphones),
        AchievementDef("l2", AchvCategory.Listening, 500, CardTier.Silver, Icons.Rounded.Headphones),
        AchievementDef("l3", AchvCategory.Listening, 1000, CardTier.Gold, Icons.Rounded.Headphones),
        AchievementDef("l4", AchvCategory.Listening, 2500, CardTier.Platinum, Icons.Rounded.Headphones),
        AchievementDef("l5", AchvCategory.Listening, 5000, CardTier.Emerald, Icons.Rounded.Headphones),
        AchievementDef("l6", AchvCategory.Listening, 10000, CardTier.Sapphire, Icons.Rounded.Headphones),
        AchievementDef("l7", AchvCategory.Listening, 25000, CardTier.Diamond, Icons.Rounded.Headphones),
        AchievementDef("l8", AchvCategory.Listening, 50000, CardTier.Chrome, Icons.Rounded.Headphones),
        AchievementDef("l9", AchvCategory.Listening, 500000, CardTier.Iridescent, Icons.Rounded.Headphones),
        // Artists
        AchievementDef("a1", AchvCategory.Artists, 25, CardTier.Bronze, Icons.Rounded.MicExternalOn),
        AchievementDef("a2", AchvCategory.Artists, 50, CardTier.Silver, Icons.Rounded.MicExternalOn),
        AchievementDef("a3", AchvCategory.Artists, 100, CardTier.Gold, Icons.Rounded.MicExternalOn),
        AchievementDef("a4", AchvCategory.Artists, 150, CardTier.Platinum, Icons.Rounded.MicExternalOn),
        AchievementDef("a5", AchvCategory.Artists, 250, CardTier.Emerald, Icons.Rounded.MicExternalOn),
        AchievementDef("a6", AchvCategory.Artists, 300, CardTier.Sapphire, Icons.Rounded.MicExternalOn),
        AchievementDef("a7", AchvCategory.Artists, 400, CardTier.Diamond, Icons.Rounded.MicExternalOn),
        AchievementDef("a8", AchvCategory.Artists, 500, CardTier.Chrome, Icons.Rounded.MicExternalOn),
        AchievementDef("a9", AchvCategory.Artists, 3000, CardTier.Iridescent, Icons.Rounded.MicExternalOn),
        // Albums
        AchievementDef("ab1", AchvCategory.Albums, 25, CardTier.Bronze, Icons.Rounded.Album),
        AchievementDef("ab2", AchvCategory.Albums, 50, CardTier.Silver, Icons.Rounded.Album),
        AchievementDef("ab3", AchvCategory.Albums, 100, CardTier.Gold, Icons.Rounded.Album),
        AchievementDef("ab4", AchvCategory.Albums, 150, CardTier.Platinum, Icons.Rounded.Album),
        AchievementDef("ab5", AchvCategory.Albums, 200, CardTier.Emerald, Icons.Rounded.Album),
        AchievementDef("ab6", AchvCategory.Albums, 300, CardTier.Sapphire, Icons.Rounded.Album),
        AchievementDef("ab7", AchvCategory.Albums, 400, CardTier.Diamond, Icons.Rounded.Album),
        AchievementDef("ab8", AchvCategory.Albums, 500, CardTier.Chrome, Icons.Rounded.Album),
        AchievementDef("ab9", AchvCategory.Albums, 3000, CardTier.Iridescent, Icons.Rounded.Album),
        // Tracks
        AchievementDef("t1", AchvCategory.Tracks, 50, CardTier.Bronze, Icons.Rounded.MusicNote),
        AchievementDef("t2", AchvCategory.Tracks, 100, CardTier.Silver, Icons.Rounded.MusicNote),
        AchievementDef("t3", AchvCategory.Tracks, 200, CardTier.Gold, Icons.Rounded.MusicNote),
        AchievementDef("t4", AchvCategory.Tracks, 300, CardTier.Platinum, Icons.Rounded.MusicNote),
        AchievementDef("t5", AchvCategory.Tracks, 500, CardTier.Emerald, Icons.Rounded.MusicNote),
        AchievementDef("t6", AchvCategory.Tracks, 600, CardTier.Sapphire, Icons.Rounded.MusicNote),
        AchievementDef("t7", AchvCategory.Tracks, 800, CardTier.Diamond, Icons.Rounded.MusicNote),
        AchievementDef("t8", AchvCategory.Tracks, 1000, CardTier.Chrome, Icons.Rounded.MusicNote),
        AchievementDef("t9", AchvCategory.Tracks, 6000, CardTier.Iridescent, Icons.Rounded.MusicNote),
        // Loyalty
        AchievementDef("y1", AchvCategory.Loyalty, 1, CardTier.Bronze, Icons.Rounded.Cake),
        AchievementDef("y2", AchvCategory.Loyalty, 2, CardTier.Silver, Icons.Rounded.Cake),
        AchievementDef("y3", AchvCategory.Loyalty, 3, CardTier.Gold, Icons.Rounded.Cake),
        AchievementDef("y4", AchvCategory.Loyalty, 4, CardTier.Platinum, Icons.Rounded.Cake),
        AchievementDef("y5", AchvCategory.Loyalty, 5, CardTier.Emerald, Icons.Rounded.Cake),
        AchievementDef("y6", AchvCategory.Loyalty, 6, CardTier.Sapphire, Icons.Rounded.Cake),
        AchievementDef("y7", AchvCategory.Loyalty, 7, CardTier.Diamond, Icons.Rounded.Cake),
        AchievementDef("y8", AchvCategory.Loyalty, 8, CardTier.Chrome, Icons.Rounded.Cake),
        AchievementDef("y9", AchvCategory.Loyalty, 15, CardTier.Iridescent, Icons.Rounded.Cake),
        // Pace
        AchievementDef("p1", AchvCategory.Pace, 10, CardTier.Bronze, Icons.Rounded.Speed),
        AchievementDef("p2", AchvCategory.Pace, 20, CardTier.Silver, Icons.Rounded.Speed),
        AchievementDef("p3", AchvCategory.Pace, 35, CardTier.Gold, Icons.Rounded.Speed),
        AchievementDef("p4", AchvCategory.Pace, 50, CardTier.Platinum, Icons.Rounded.Speed),
        AchievementDef("p5", AchvCategory.Pace, 75, CardTier.Emerald, Icons.Rounded.Speed),
        AchievementDef("p6", AchvCategory.Pace, 100, CardTier.Sapphire, Icons.Rounded.Speed),
        AchievementDef("p7", AchvCategory.Pace, 150, CardTier.Diamond, Icons.Rounded.Speed),
        AchievementDef("p8", AchvCategory.Pace, 200, CardTier.Chrome, Icons.Rounded.Speed),
        AchievementDef("p9", AchvCategory.Pace, 750, CardTier.Iridescent, Icons.Rounded.Speed),
        // Streak
        AchievementDef("s1", AchvCategory.Streak, 3, CardTier.Bronze, Icons.Rounded.LocalFireDepartment),
        AchievementDef("s2", AchvCategory.Streak, 7, CardTier.Silver, Icons.Rounded.LocalFireDepartment),
        AchievementDef("s3", AchvCategory.Streak, 14, CardTier.Gold, Icons.Rounded.LocalFireDepartment),
        AchievementDef("s4", AchvCategory.Streak, 30, CardTier.Platinum, Icons.Rounded.LocalFireDepartment),
        AchievementDef("s5", AchvCategory.Streak, 60, CardTier.Emerald, Icons.Rounded.LocalFireDepartment),
        AchievementDef("s6", AchvCategory.Streak, 100, CardTier.Sapphire, Icons.Rounded.LocalFireDepartment),
        AchievementDef("s7", AchvCategory.Streak, 200, CardTier.Diamond, Icons.Rounded.LocalFireDepartment),
        AchievementDef("s8", AchvCategory.Streak, 365, CardTier.Chrome, Icons.Rounded.LocalFireDepartment),
        AchievementDef("s9", AchvCategory.Streak, 1000, CardTier.Iridescent, Icons.Rounded.LocalFireDepartment),
        // Marathon
        AchievementDef("m1", AchvCategory.Marathon, 20, CardTier.Bronze, Icons.Rounded.Bolt),
        AchievementDef("m2", AchvCategory.Marathon, 50, CardTier.Silver, Icons.Rounded.Bolt),
        AchievementDef("m3", AchvCategory.Marathon, 100, CardTier.Gold, Icons.Rounded.Bolt),
        AchievementDef("m4", AchvCategory.Marathon, 150, CardTier.Platinum, Icons.Rounded.Bolt),
        AchievementDef("m5", AchvCategory.Marathon, 200, CardTier.Emerald, Icons.Rounded.Bolt),
        AchievementDef("m6", AchvCategory.Marathon, 300, CardTier.Sapphire, Icons.Rounded.Bolt),
        AchievementDef("m7", AchvCategory.Marathon, 500, CardTier.Diamond, Icons.Rounded.Bolt),
        AchievementDef("m8", AchvCategory.Marathon, 750, CardTier.Chrome, Icons.Rounded.Bolt),
        AchievementDef("m9", AchvCategory.Marathon, 1000, CardTier.Iridescent, Icons.Rounded.Bolt),
        // Social
        AchievementDef("so1", AchvCategory.Social, 1, CardTier.Bronze, Icons.Rounded.PeopleAlt),
        AchievementDef("so2", AchvCategory.Social, 3, CardTier.Silver, Icons.Rounded.PeopleAlt),
        AchievementDef("so3", AchvCategory.Social, 5, CardTier.Gold, Icons.Rounded.PeopleAlt),
        AchievementDef("so4", AchvCategory.Social, 10, CardTier.Platinum, Icons.Rounded.PeopleAlt),
        AchievementDef("so5", AchvCategory.Social, 15, CardTier.Emerald, Icons.Rounded.PeopleAlt),
        AchievementDef("so6", AchvCategory.Social, 20, CardTier.Sapphire, Icons.Rounded.PeopleAlt),
        AchievementDef("so7", AchvCategory.Social, 30, CardTier.Diamond, Icons.Rounded.PeopleAlt),
        AchievementDef("so8", AchvCategory.Social, 50, CardTier.Chrome, Icons.Rounded.PeopleAlt),
        AchievementDef("so9", AchvCategory.Social, 100, CardTier.Iridescent, Icons.Rounded.PeopleAlt),
        // Comparisons
        AchievementDef("c1", AchvCategory.Comparisons, 1, CardTier.Bronze, Icons.AutoMirrored.Rounded.CompareArrows),
        AchievementDef("c2", AchvCategory.Comparisons, 5, CardTier.Silver, Icons.AutoMirrored.Rounded.CompareArrows),
        AchievementDef("c3", AchvCategory.Comparisons, 10, CardTier.Gold, Icons.AutoMirrored.Rounded.CompareArrows),
        AchievementDef("c4", AchvCategory.Comparisons, 25, CardTier.Platinum, Icons.AutoMirrored.Rounded.CompareArrows),
        AchievementDef("c5", AchvCategory.Comparisons, 50, CardTier.Emerald, Icons.AutoMirrored.Rounded.CompareArrows),
        AchievementDef("c6", AchvCategory.Comparisons, 75, CardTier.Sapphire, Icons.AutoMirrored.Rounded.CompareArrows),
        AchievementDef("c7", AchvCategory.Comparisons, 100, CardTier.Diamond, Icons.AutoMirrored.Rounded.CompareArrows),
        AchievementDef("c8", AchvCategory.Comparisons, 200, CardTier.Chrome, Icons.AutoMirrored.Rounded.CompareArrows),
        AchievementDef("c9", AchvCategory.Comparisons, 500, CardTier.Iridescent, Icons.AutoMirrored.Rounded.CompareArrows),
    )
