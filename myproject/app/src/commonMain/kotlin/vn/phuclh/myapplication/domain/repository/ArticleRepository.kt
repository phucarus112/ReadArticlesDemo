package vn.phuclh.myapplication.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.phuclh.myapplication.domain.model.Article

interface ArticleRepository {
    fun observeArticles(): Flow<List<Article>>

    fun observeBookmarks(): Flow<List<Article>>

    suspend fun refreshArticles(): Result<Unit>

    suspend fun toggleBookmark(
        url: String,
        isBookmarked: Boolean,
    )

    suspend fun markAllSeen()

    suspend fun getUnseenCount(): Int

    suspend fun getArticleByUrl(url: String): Article?
}
