package vn.phuclh.myapplication.presentation.bookmarks

import vn.phuclh.myapplication.domain.model.Article

data class BookmarkUiState(
    val bookmarks: List<Article> = emptyList(),
)
