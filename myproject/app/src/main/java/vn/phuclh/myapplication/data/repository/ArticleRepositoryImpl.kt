package vn.phuclh.myapplication.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.phuclh.myapplication.data.api.NewsApiService
import vn.phuclh.myapplication.data.local.ArticleDao
import vn.phuclh.myapplication.data.local.toDomain
import vn.phuclh.myapplication.data.model.toEntity
import vn.phuclh.myapplication.domain.model.Article
import vn.phuclh.myapplication.domain.repository.ArticleRepository
import vn.phuclh.myapplication.util.ApiConfig
import javax.inject.Inject

class ArticleRepositoryImpl @Inject constructor(
    private val apiService: NewsApiService,
    private val dao: ArticleDao
) : ArticleRepository {

    override fun observeArticles(): Flow<List<Article>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeBookmarks(): Flow<List<Article>> =
        dao.observeBookmarks().map { list -> list.map { it.toDomain() } }

    override suspend fun refreshArticles(): Result<Unit> = runCatching {
        val response = apiService.getTopHeadlines()
        val entities = response.articles.mapNotNull { it.toEntity() }
        dao.insertAll(entities)
    }

    override suspend fun toggleBookmark(url: String, isBookmarked: Boolean) {
        dao.updateBookmark(url, isBookmarked)
    }

    override suspend fun markAllSeen() = dao.markAllSeen()

    override suspend fun getUnseenCount(): Int = dao.getUnseenCount()

    override suspend fun getArticleByUrl(url: String): Article? =
        dao.getByUrl(url)?.toDomain()
}
