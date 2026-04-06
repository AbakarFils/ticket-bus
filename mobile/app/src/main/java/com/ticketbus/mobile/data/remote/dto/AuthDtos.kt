package com.ticketbus.mobile.data.remote.dto

data class LoginRequest(val username: String, val password: String)
data class AuthResponse(val token: String, val username: String, val role: String)
