package vn.phuclh.myapplication.data.di

import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import vn.phuclh.myapplication.data.api.AuthInterceptor
import vn.phuclh.myapplication.data.local.AppDatabase
import vn.phuclh.myapplication.util.ApiConfig

// androidModule: CHỈ chứa những gì buộc phải phụ thuộc nền tảng Android —
// HttpClient engine (OkHttp), Room database builder (cần Context), DAO.
// Repository, use case, view model đã nằm ở commonModule (dùng chung đa nền tảng).
val androidModule =
    module {

        single {
            HttpClient(OkHttp) {
                engine {
                    // Chucker attaches at the OkHttp layer; no-op variant in release builds
                    addInterceptor(ChuckerInterceptor.Builder(androidContext()).build())
                }
                defaultRequest { url(ApiConfig.BASE_URL) }
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                install(Logging) { level = LogLevel.BODY }
                install(AuthInterceptor)
            }
        }

        single {
            Room
                .databaseBuilder(androidContext(), AppDatabase::class.java, "news_digest.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }

        single { get<AppDatabase>().articleDao() }
    }
