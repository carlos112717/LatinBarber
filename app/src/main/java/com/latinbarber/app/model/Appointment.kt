package com.latinbarber.app.model

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val barberName: String = "",
    val serviceName: String = "",
    val price: Double = 0.0,
    val date: String = "",
    val time: String = "",
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)