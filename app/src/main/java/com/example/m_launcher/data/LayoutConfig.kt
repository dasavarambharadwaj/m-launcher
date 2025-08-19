package com.example.m_launcher.data

import kotlinx.serialization.Serializable

@Serializable
enum class HorizontalPosition { LEFT, CENTER, RIGHT }

@Serializable
enum class VerticalPosition { TOP, CENTER, BOTTOM }

@Serializable
data class LayoutConfig(
    val horizontalPosition: HorizontalPosition = HorizontalPosition.CENTER,
    val verticalPosition: VerticalPosition = VerticalPosition.CENTER
)


