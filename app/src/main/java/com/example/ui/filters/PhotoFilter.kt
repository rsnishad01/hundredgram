package com.example.ui.filters

import androidx.compose.ui.graphics.ColorMatrix
import java.io.Serializable

enum class FilterPresetType {
    NORMAL, VINTAGE, SEPIA, CYBERPUNK, MONOCHROME, SUNSET, GLOW, HIGH_CONTRAST
}

data class PhotoFilter(
    val id: String,
    val name: String,
    val type: FilterPresetType,
    val colorMatrix: ColorMatrix
) : Serializable {
    companion object {
        fun getAllFilters(): List<PhotoFilter> = listOf(
            PhotoFilter(
                "f_normal", "Normal", FilterPresetType.NORMAL,
                ColorMatrix()
            ),
            PhotoFilter(
                "f_vintage", "Vintage", FilterPresetType.VINTAGE,
                ColorMatrix(
                    floatArrayOf(
                        0.9f, 0.2f, 0.1f, 0f, 10f,
                        0.1f, 0.8f, 0.1f, 0f, 10f,
                        0.1f, 0.1f, 0.7f, 0f, 20f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            ),
            PhotoFilter(
                "f_sepia", "Sepia", FilterPresetType.SEPIA,
                ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            ),
            PhotoFilter(
                "f_cyber", "Cyber", FilterPresetType.CYBERPUNK,
                ColorMatrix(
                    floatArrayOf(
                        1.2f, 0f, 0.5f, 0f, 20f,
                        0f, 1.1f, 0.8f, 0f, 0f,
                        0.3f, 0.5f, 1.4f, 0f, 30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            ),
            PhotoFilter(
                "f_mono", "B&W", FilterPresetType.MONOCHROME,
                ColorMatrix().apply { setToSaturation(0f) }
            ),
            PhotoFilter(
                "f_sunset", "Sunset", FilterPresetType.SUNSET,
                ColorMatrix(
                    floatArrayOf(
                        1.3f, 0.1f, 0f, 0f, 25f,
                        0.1f, 0.9f, 0f, 0f, 10f,
                        0f, 0.1f, 0.6f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
        )
    }
}
