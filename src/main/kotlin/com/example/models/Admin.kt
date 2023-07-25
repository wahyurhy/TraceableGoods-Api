package com.example.models

import com.google.gson.annotations.SerializedName
import org.mindrot.jbcrypt.BCrypt

data class Admin(
	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("nama")
	val nama: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("password")
	val password: String? = null

) {
	fun hashedPassword(): String {
		return BCrypt.hashpw(password, BCrypt.gensalt())
	}

	fun isValidCredentials(): Boolean {
		val emailRegex = Regex(pattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
		return email?.matches(emailRegex) == true && password?.length!! >= 8
	}
}
