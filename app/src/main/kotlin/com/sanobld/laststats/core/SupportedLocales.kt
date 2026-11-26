package com.sanobld.laststats.core

data class SupportedLocale(
    val code: String,
    val flag: String,
    val nativeName: String,
    val englishName: String,
)

/** Display order = order in every language picker. */
val SupportedLocales: List<SupportedLocale> = listOf(
    SupportedLocale("fr", "🇫🇷", "Français", "French"),
    SupportedLocale("en", "🇬🇧", "English", "English"),
    SupportedLocale("es", "🇪🇸", "Español", "Spanish"),
    SupportedLocale("zh", "🇨🇳", "中文", "Chinese"),
    SupportedLocale("pt", "🇧🇷", "Português", "Portuguese"),
    SupportedLocale("de", "🇩🇪", "Deutsch", "German"),
    SupportedLocale("it", "🇮🇹", "Italiano", "Italian"),
    SupportedLocale("ja", "🇯🇵", "日本語", "Japanese"),
    SupportedLocale("ru", "🇷🇺", "Русский", "Russian"),
    SupportedLocale("ar", "🇸🇦", "العربية", "Arabic"),
)

/** Falls back to French when the code is unknown (for example an old backup). */
fun supportedLocaleFor(code: String): SupportedLocale =
    SupportedLocales.firstOrNull { it.code == code } ?: SupportedLocales.first()
