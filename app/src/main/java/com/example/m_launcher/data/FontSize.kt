package com.example.m_launcher.data

import kotlinx.serialization.Serializable

@Serializable
enum class FontSize(val spValue: Float, val displayName: String) {
    SMALL(14f, "Small"),
    MEDIUM(18f, "Medium"),
    LARGE(22f, "Large"),
    EXTRA_LARGE(26f, "Extra Large")
}


