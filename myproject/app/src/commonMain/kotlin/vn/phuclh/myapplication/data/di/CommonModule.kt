package vn.phuclh.myapplication.data.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import vn.phuclh.myapplication.data.api.NewsApiService
import vn.phuclh.myapplication.data.repository.ArticleRepositoryImpl
import vn.phuclh.myapplication.domain.repository.ArticleRepository
import vn.phuclh.myapplication.domain.usecase.GetArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.GetBookmarksUseCase
import vn.phuclh.myapplication.domain.usecase.MarkArticlesSeenUseCase
import vn.phuclh.myapplication.domain.usecase.RefreshArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase
import vn.phuclh.myapplication.presentation.articles.ArticlesViewModel
import vn.phuclh.myapplication.presentation.bookmarks.BookmarkViewModel
import vn.phuclh.myapplication.presentation.detail.DetailViewModel

// commonModule: mọi thứ thuần Kotlin, dùng lại được trên mọi target (Android/iOS/…).
// Phần phụ thuộc nền tảng (HttpClient engine, Room builder, Context) nằm ở
// module riêng của từng platform — xem androidModule ở androidMain.
val commonModule: Module =
    module {
        single { NewsApiService(get()) }

        single<ArticleRepository> { ArticleRepositoryImpl(get(), get()) }

        factory { GetArticlesUseCase(get()) }
        factory { GetBookmarksUseCase(get()) }
        factory { RefreshArticlesUseCase(get()) }
        factory { ToggleBookmarkUseCase(get()) }
        factory { MarkArticlesSeenUseCase(get()) }

        viewModelOf(::ArticlesViewModel)
        viewModelOf(::BookmarkViewModel)
        viewModel { params -> DetailViewModel(get(), get(), params.get()) }
    }
