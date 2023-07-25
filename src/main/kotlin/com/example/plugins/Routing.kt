package com.example.plugins

import com.example.route.routeAdmin
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting() {
    routeAdmin()
}
