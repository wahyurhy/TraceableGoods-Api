package com.example.route

import com.example.models.Admin
import com.example.mysql.DbConnection
import com.example.mysql.entity.EntityAdmin
import com.example.utils.AdminResponse
import com.example.utils.GenericResponse
import com.example.utils.LoginResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.mindrot.jbcrypt.BCrypt

fun Application.routeAdmin() {
    val db: Database = DbConnection.getDatabaseInstance()
    routing {
        get("/") {
            call.respondText("Selamat datang di Traceable Goods Server")
        }

        post("/register") {
            val parameters = call.receiveParameters()
            val nama = parameters["nama"]
            val email = parameters["email"]
            val password = parameters["password"]

            if (nama.isNullOrEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    GenericResponse(
                        isSuccess = false,
                        message = "Nama harus diisi"
                    )
                )
                return@post
            }

            val admin = Admin(nama = nama, email = email, password = password)

            if (!admin.isValidCredentials()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    GenericResponse(
                        isSuccess = false,
                        message = "Format email belum tepat atau password harus dari 8 karakter atau lebih"
                    )
                )
                return@post
            }

            val emailLowercased = admin.email?.lowercase().toString()
            val passwordHashed = admin.hashedPassword()

            // Check if email already exists
            val adminEntity = db.from(EntityAdmin)
                .select()
                .where { EntityAdmin.email eq emailLowercased }
                .map { it[EntityAdmin.email] }
                .firstOrNull()

            if (adminEntity != null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    GenericResponse(isSuccess = false, message = "Email sudah pernah terdaftar")
                )
                return@post
            }

            val noOfRowsAffected = db.insert(EntityAdmin) {
                set(it.nama, admin.nama)
                set(it.email, emailLowercased)
                set(it.password, passwordHashed)
            }

            if (noOfRowsAffected > 0) {
                // success
                call.respond(
                    HttpStatusCode.Created,
                    GenericResponse(isSuccess = true, message = "$noOfRowsAffected data berhasil ditambahkan")
                )
            } else {
                // failed
                call.respond(
                    HttpStatusCode.BadRequest,
                    GenericResponse(isSuccess = false, message = "Terjadi error untuk mendaftarkan admin")
                )
            }
        }

        post("/login") {
            val parameters = call.receiveParameters()
            val email = parameters["email"]
            val password = parameters["password"]

            val admin = Admin(email = email, password = password)

            val emailLowercasedAdmin = admin.email?.lowercase().toString()
            val passwordAdmin = admin.password

            // cek bila email kosong
            if (emailLowercasedAdmin.isEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    GenericResponse(
                        isSuccess = false,
                        message = "Email tidak boleh kosong"
                    )
                )
                return@post
            }

            // cek bila email dan password tidak sesuai
            if (!admin.isValidCredentials()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    GenericResponse(
                        isSuccess = false,
                        message = "Email tidak ditemukan atau password salah"
                    )
                )
                return@post
            }

            // cek apakah email terdaftar
            val adminEntity = db.from(EntityAdmin)
                .select()
                .where { EntityAdmin.email eq emailLowercasedAdmin }
                .map {
                    val id = it[EntityAdmin.id]
                    val nama = it[EntityAdmin.nama]
                    val email = it[EntityAdmin.email]
                    val password = it[EntityAdmin.password]
                    Admin(id, nama, email, password)
                }.firstOrNull()

            // bila email tidak ditemukan
            if (adminEntity == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    GenericResponse(isSuccess = false, message = "Email dan password tidak ditemukan")
                )
                return@post
            }

            val doesPasswordMatch = BCrypt.checkpw(passwordAdmin, adminEntity.password)
            if (!doesPasswordMatch) {
                call.respond(HttpStatusCode.BadRequest, GenericResponse(isSuccess = false, message = "Password salah"))
                return@post
            }

            val adminResponse = AdminResponse(adminEntity.id!!.toInt(), adminEntity.nama.toString(), adminEntity.email.toString())

            call.respond(HttpStatusCode.OK, LoginResponse(code = 200, message = "Berhasil login", data = adminResponse))
        }
    }
}