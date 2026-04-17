package com.yourbrand.lumameter.pro.data.calibration

import com.yourbrand.lumameter.pro.domain.exposure.CalibrationPreset
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

data class CalibrationBackupPayload(
    val presets: List<CalibrationPreset>,
    val activeId: String?,
)

object CalibrationBackupCodec {
    private const val CURRENT_VERSION = 1
    private const val KEY_VERSION = "version"
    private const val KEY_PRESETS = "presets"
    private const val KEY_ACTIVE_ID = "activeId"
    private const val KEY_ID = "id"
    private const val KEY_NAME = "name"
    private const val KEY_OFFSET_EV = "offsetEv"
    private const val KEY_NOTES = "notes"

    fun serializeToJson(
        presets: List<CalibrationPreset>,
        activeId: String?,
    ): String {
        val array = JSONArray()
        presets.forEach { preset ->
            val item = JSONObject()
                .put(KEY_ID, preset.id)
                .put(KEY_NAME, preset.name)
                .put(KEY_OFFSET_EV, preset.offsetEv)
                .put(KEY_NOTES, preset.notes)
            array.put(item)
        }
        val root = JSONObject()
            .put(KEY_VERSION, CURRENT_VERSION)
            .put(KEY_PRESETS, array)
        if (activeId != null) {
            root.put(KEY_ACTIVE_ID, activeId)
        }
        return root.toString(2)
    }

    fun parseFromJson(raw: String): Result<CalibrationBackupPayload> {
        return runCatching {
            val root = JSONObject(raw)
            val version = root.optInt(KEY_VERSION, CURRENT_VERSION)
            if (version > CURRENT_VERSION) {
                throw JSONException("Unsupported backup version $version")
            }
            val array = root.optJSONArray(KEY_PRESETS) ?: JSONArray()
            val presets = buildList {
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val id = item.optString(KEY_ID).takeIf { it.isNotBlank() } ?: continue
                    val name = item.optString(KEY_NAME).takeIf { it.isNotBlank() } ?: continue
                    if (!item.has(KEY_OFFSET_EV)) continue
                    val offset = item.optDouble(KEY_OFFSET_EV, Double.NaN)
                    if (offset.isNaN()) continue
                    val notes = item.optString(KEY_NOTES, "")
                    add(
                        CalibrationPreset(
                            id = id,
                            name = name,
                            offsetEv = offset.coerceIn(-5.0, 5.0),
                            notes = notes,
                        ),
                    )
                }
            }
            val activeId = root.optString(KEY_ACTIVE_ID).takeIf {
                it.isNotBlank() && presets.any { preset -> preset.id == it }
            }
            CalibrationBackupPayload(presets = presets, activeId = activeId)
        }
    }
}
