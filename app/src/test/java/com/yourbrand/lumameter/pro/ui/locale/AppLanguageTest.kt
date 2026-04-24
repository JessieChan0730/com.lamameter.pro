package com.yourbrand.lumameter.pro.ui.locale

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `language display labels keep chinese and english fixed`() {
        assertEquals("Follow system", AppLanguage.SYSTEM.displayLabel("Follow system"))
        assertEquals("\u4E2D\u6587", AppLanguage.CHINESE.displayLabel("Follow system"))
        assertEquals("English", AppLanguage.ENGLISH.displayLabel("Follow system"))
    }
}
