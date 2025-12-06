package com.latinbarber.app.model

data class Service(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val durationMinutes: Int = 30
)