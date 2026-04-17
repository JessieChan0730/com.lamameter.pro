package com.yourbrand.lumameter.pro.data.calibration

import com.yourbrand.lumameter.pro.domain.exposure.CalibrationPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalibrationBackupCodecTest {
    @Test
    fun `round trips presets and active id`() {
        val presets = listOf(
            CalibrationPreset(id = "a", name = "Pixel 8", offsetEv = 0.4, notes = "daylight"),
            CalibrationPreset(id = "b", name = "iPhone", offsetEv = -0.3, notes = ""),
        )
        val json = CalibrationBackupCodec.serializeToJson(presets, activeId = "b")
        val decoded = CalibrationBackupCodec.parseFromJson(json).getOrThrow()
        assertEquals(presets, decoded.presets)
        assertEquals("b", decoded.activeId)
    }

    @Test
    fun `drops entries with missing fields`() {
        val raw = """
            {
              "version": 1,
              "presets": [
                { "id": "ok", "name": "Good", "offsetEv": 0.2, "notes": "" },
                { "id": "", "name": "NoId", "offsetEv": 0.1 },
                { "id": "nooffset", "name": "NoOffset" },
                { "id": "noname", "offsetEv": 0.5 }
              ]
            }
        """.trimIndent()
        val decoded = CalibrationBackupCodec.parseFromJson(raw).getOrThrow()
        assertEquals(1, decoded.presets.size)
        assertEquals("ok", decoded.presets[0].id)
    }

    @Test
    fun `drops active id when preset missing`() {
        val raw = """
            {
              "version": 1,
              "activeId": "ghost",
              "presets": [
                { "id": "a", "name": "A", "offsetEv": 0.0, "notes": "" }
              ]
            }
        """.trimIndent()
        val decoded = CalibrationBackupCodec.parseFromJson(raw).getOrThrow()
        assertNull(decoded.activeId)
    }

    @Test
    fun `fails on malformed json`() {
        val result = CalibrationBackupCodec.parseFromJson("not-json")
        assertTrue(result.isFailure)
    }

    @Test
    fun `fails on future version`() {
        val raw = """{"version":999,"presets":[]}"""
        val result = CalibrationBackupCodec.parseFromJson(raw)
        assertTrue(result.isFailure)
    }

    @Test
    fun `clamps out of range offset`() {
        val raw = """
            {"version":1,"presets":[{"id":"a","name":"A","offsetEv":42.0,"notes":""}]}
        """.trimIndent()
        val decoded = CalibrationBackupCodec.parseFromJson(raw).getOrThrow()
        assertEquals(5.0, decoded.presets[0].offsetEv, 0.0001)
    }
}
