package com.yourbrand.lumameter.pro.ui.locale

import java.util.Locale

enum class AppLanguage(
    val storageValue: String,
    val locale: Locale?,
) {
    SYSTEM("system", null),
    CHINESE("zh", Locale.SIMPLIFIED_CHINESE),
    ENGLISH("en", Locale.ENGLISH),
    ;

    companion object {
        fun fromStorageValue(value: String?): AppLanguage {
            return entries.firstOrNull { it.storageValue == value } ?: SYSTEM
        }
    }
}
