package com.latinbarber.app.model

data class ShopConfig(
    val openTime: String = "09:00",
    val closeTime: String = "20:00",
    val workDays: List<String> = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
)