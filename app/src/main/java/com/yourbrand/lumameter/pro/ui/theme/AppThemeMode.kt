package com.yourbrand.lumameter.pro.ui.theme

enum class AppThemeMode(
    val storageValue: String,
) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    fun resolveDarkTheme(isSystemDark: Boolean): Boolean {
        return when (this) {
            SYSTEM -> isSystemDark
            LIGHT -> false
            DARK -> true
        }
    }

    companion object {
        fun fromStorageValue(value: String?): AppThemeMode {
            return entries.firstOrNull { it.storageValue == value } ?: SYSTEM
        }
    }
}
