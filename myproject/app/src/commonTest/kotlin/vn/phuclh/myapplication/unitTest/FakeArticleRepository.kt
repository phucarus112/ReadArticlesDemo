package vn.phuclh.myapplication.unitTest

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import vn.phuclh.myapplication.domain.model.Article
import vn.phuclh.myapplication.domain.repository.ArticleRepository

/**
 * Fake thay cho mock framework: implement thẳng interface repository.
 * Vừa đóng vai Stub (trả dữ liệu định sẵn) vừa đóng vai Mock (ghi lại lời gọi để verify).
 * Không cần bytecode magic nên chạy được trên KMP (JVM/iOS/…).
 */
class FakeArticleRepository : ArticleRepository {
    val articlesFlow = MutableStateFlow<List<Article>>(emptyList())

    // Điều khiển kết quả refresh từ phía test
    var refreshResult: Result<Unit> = Result.success(Unit)

    // Ghi lại lời gọi để verify hành vi
    var refreshCallCount = 0
    var markAllSeenCallCount = 0
    val toggledBookmarks = mutableListOf<Pair<String, Boolean>>()

    override fun observeArticles(): Flow<List<Article>> = articlesFlow

    override fun observeBookmarks(): Flow<List<Article>> = articlesFlow

    override suspend fun refreshArticles(): Result<Unit> {
        refreshCallCount++
        return refreshResult
    }

    override suspend fun toggleBookmark(
        url: String,
        isBookmarked: Boolean,
    ) {
        toggledBookmarks += url to isBookmarked
    }

    override suspend fun markAllSeen() {
        markAllSeenCallCount++
    }

    override suspend fun getUnseenCount(): Int = 0

    override suspend fun getArticleByUrl(url: String): Article? = articlesFlow.value.find { it.url == url }
}

fun article(
    url: String,
    title: String = "Title $url",
    isBookmarked: Boolean = false,
) = Article(
    url = url,
    title = title,
    description = null,
    imageUrl = null,
    source = "Test Source",
    publishedAt = "2026-01-01T00:00:00Z",
    content = null,
    isBookmarked = isBookmarked,
)
