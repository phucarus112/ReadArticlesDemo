package vn.phuclh.myapplication.presentation.articles

import vn.phuclh.myapplication.domain.model.Article

data class ArticlesUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

sealed interface ArticlesIntent {
    data object FetchData : ArticlesIntent

    data object RefreshData : ArticlesIntent

    data object MarkAllArticlesSeen : ArticlesIntent

    data class ToggleBookmark(
        val url: String,
        val isBookmarked: Boolean,
    ) : ArticlesIntent
}
