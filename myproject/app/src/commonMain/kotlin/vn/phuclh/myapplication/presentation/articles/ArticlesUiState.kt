package vn.phuclh.myapplication.presentation.articles

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import vn.phuclh.myapplication.domain.model.Article

@Stable
data class ArticlesUiState(
    val articles: ImmutableList<Article> = persistentListOf(),
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
