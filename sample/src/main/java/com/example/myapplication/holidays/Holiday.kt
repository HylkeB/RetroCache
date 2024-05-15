package com.example.myapplication.holidays

import kotlinx.serialization.Serializable

@Serializable
data class Holiday(
    val date: String,
    val name: String,
    val localName: String
)