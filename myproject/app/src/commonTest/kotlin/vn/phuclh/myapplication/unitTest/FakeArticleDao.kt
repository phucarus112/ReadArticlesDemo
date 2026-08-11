package vn.phuclh.myapplication.unitTest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import vn.phuclh.myapplication.data.local.ArticleDao
import vn.phuclh.myapplication.data.local.ArticleEntity

/**
 * Fake DAO trong bộ nhớ — mô phỏng hành vi Room mà không cần DB thật.
 * Cho phép unit test ArticleRepositoryImpl trên mọi target (không cần emulator).
 */
class FakeArticleDao : ArticleDao {
    val articles = MutableStateFlow<List<ArticleEntity>>(emptyList())

    override fun observeAll(): Flow<List<ArticleEntity>> = articles

    override fun observeBookmarks(): Flow<List<ArticleEntity>> =
        articles.map { list -> list.filter { it.isBookmarked } }

    override suspend fun getByUrl(url: String): ArticleEntity? = articles.value.find { it.url == url }

    override suspend fun insertAll(articles: List<ArticleEntity>) {
        // Mô phỏng OnConflictStrategy.IGNORE: bỏ qua url đã tồn tại
        val existingUrls =
            this.articles.value
                .map { it.url }
                .toSet()
        this.articles.value += articles.filter { it.url !in existingUrls }
    }

    override suspend fun updateBookmark(
        url: String,
        isBookmarked: Boolean,
    ) {
        articles.value = articles.value.map { if (it.url == url) it.copy(isBookmarked = isBookmarked) else it }
    }

    override suspend fun markAllSeen() {
        articles.value = articles.value.map { it.copy(isSeen = true) }
    }

    override suspend fun getUnseenCount(): Int = articles.value.count { !it.isSeen }
}

fun entity(
    url: String,
    title: String = "Title $url",
    imageUrl: String? = null,
    isBookmarked: Boolean = false,
    isSeen: Boolean = false,
) = ArticleEntity(
    url = url,
    title = title,
    description = null,
    urlToImage = imageUrl,
    source = "Test Source",
    publishedAt = "2026-01-01T00:00:00Z",
    content = null,
    isBookmarked = isBookmarked,
    isSeen = isSeen,
)
