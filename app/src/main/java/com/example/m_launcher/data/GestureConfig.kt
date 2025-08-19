package com.example.m_launcher.data

import kotlinx.serialization.Serializable

@Serializable
data class GestureConfig(
    val leftSwipePackage: String? = null,
    val leftSwipeName: String? = null,
    val rightSwipePackage: String? = null,
    val rightSwipeName: String? = null
)


