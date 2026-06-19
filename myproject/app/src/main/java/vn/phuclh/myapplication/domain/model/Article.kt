package vn.phuclh.myapplication.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Article(
    val url: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val source: String,
    val publishedAt: String,
    val content: String?,
    val isBookmarked: Boolean = false,
    val isSeen: Boolean = false
)
