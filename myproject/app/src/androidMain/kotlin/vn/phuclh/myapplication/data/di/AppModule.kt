package vn.phuclh.myapplication.data.di

import androidx.room.Room
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import vn.phuclh.myapplication.data.api.AuthInterceptor
import vn.phuclh.myapplication.data.api.NewsApiService
import vn.phuclh.myapplication.data.local.AppDatabase
import vn.phuclh.myapplication.data.repository.ArticleRepositoryImpl
import vn.phuclh.myapplication.domain.repository.ArticleRepository
import vn.phuclh.myapplication.domain.usecase.GetArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.GetBookmarksUseCase
import vn.phuclh.myapplication.domain.usecase.RefreshArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase
import vn.phuclh.myapplication.presentation.articles.ArticlesViewModel
import vn.phuclh.myapplication.presentation.bookmarks.BookmarkViewModel
import vn.phuclh.myapplication.presentation.detail.DetailViewModel
import vn.phuclh.myapplication.util.ApiConfig

val appModule =
    module {

        single {
            HttpClient(Android) {
                defaultRequest { url(ApiConfig.BASE_URL) }
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                install(Logging) { level = LogLevel.BODY }
                install(AuthInterceptor)
            }
        }

        single { NewsApiService(get()) }

        single {
            Room
                .databaseBuilder(androidContext(), AppDatabase::class.java, "news_digest.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }

        single { get<AppDatabase>().articleDao() }

        single<ArticleRepository> { ArticleRepositoryImpl(get(), get()) }

        factory { GetArticlesUseCase(get()) }
        factory { GetBookmarksUseCase(get()) }
        factory { RefreshArticlesUseCase(get()) }
        factory { ToggleBookmarkUseCase(get()) }

        viewModelOf(::ArticlesViewModel)
        viewModelOf(::BookmarkViewModel)
        viewModel { params -> DetailViewModel(get(), get(), params.get()) }
    }
