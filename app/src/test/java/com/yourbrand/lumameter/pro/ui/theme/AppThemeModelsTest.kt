package com.yourbrand.lumameter.pro.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemeModelsTest {

    @Test
    fun `unknown stored theme mode falls back to system`() {
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromStorageValue("unknown"))
    }

    @Test
    fun `unknown stored color theme falls back to classic amber`() {
        assertEquals(AppColorTheme.CLASSIC_AMBER, AppColorTheme.fromStorageValue("unknown"))
    }

    @Test
    fun `paper light color theme restores from storage value`() {
        assertEquals(AppColorTheme.PAPER_LIGHT, AppColorTheme.fromStorageValue("paper_light"))
    }

    @Test
    fun `theme mode resolves dark state consistently`() {
        assertEquals(true, AppThemeMode.DARK.resolveDarkTheme(isSystemDark = false))
        assertEquals(false, AppThemeMode.LIGHT.resolveDarkTheme(isSystemDark = true))
        assertEquals(true, AppThemeMode.SYSTEM.resolveDarkTheme(isSystemDark = true))
        assertEquals(false, AppThemeMode.SYSTEM.resolveDarkTheme(isSystemDark = false))
    }
}
