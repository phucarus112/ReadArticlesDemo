package vn.phuclh.myapplication.data.api

import io.ktor.client.plugins.api.createClientPlugin

val AuthInterceptor =
    createClientPlugin("AuthInterceptor") {
        onRequest { request, _ ->
            // TODO: gắn token vào header nếu cần
        }
    }
