package vn.phuclh.myapplication.unitTest

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import vn.phuclh.myapplication.data.api.NewsApiService
import vn.phuclh.myapplication.data.repository.ArticleRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArticleRepositoryImplTest {
    // ---- Helpers dựng NewsApiService với response giả qua MockEngine ----

    private fun apiReturning(
        json: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): NewsApiService {
        val engine =
            MockEngine {
                respond(
                    content = json,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        val client =
            HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
        return NewsApiService(client)
    }

    private fun jsonWith(articlesJson: String) =
        """
        { "status": "ok", "totalResults": 1, "articles": [ $articlesJson ] }
        """.trimIndent()

    private fun articleJson(
        url: String?,
        title: String?,
    ): String {
        val urlField = if (url == null) "null" else "\"$url\""
        val titleField = if (title == null) "null" else "\"$title\""
        return """
            {
              "source": { "id": null, "name": "BBC" },
              "title": $titleField,
              "url": $urlField,
              "urlToImage": "https://img/x.jpg",
              "publishedAt": "2024-01-01T00:00:00Z"
            }
            """.trimIndent()
    }

    // ---- observe* : mapping Entity -> domain Article ----

    @Test
    fun `observeArticles maps entities to domain articles`() =
        runTest {
            val dao = FakeArticleDao()
            dao.articles.value = listOf(entity("a", imageUrl = "https://img/a.jpg"))
            val repo =
                ArticleRepositoryImpl(
                    apiService = apiReturning(jsonWith(articleJson("a", "T"))),
                    dao = dao,
                )

            val result = repo.observeArticles().first()

            assertEquals(1, result.size)
            // urlToImage (entity) -> imageUrl (domain): kiểm tra mapping đúng field
            assertEquals("https://img/a.jpg", result.first().imageUrl)
        }

    @Test
    fun `observeBookmarks returns only bookmarked articles`() =
        runTest {
            val dao = FakeArticleDao()
            dao.articles.value =
                listOf(
                    entity("a", isBookmarked = true),
                    entity("b", isBookmarked = false),
                )
            val repo = ArticleRepositoryImpl(apiReturning(jsonWith(articleJson("a", "T"))), dao)
            val result = repo.observeBookmarks().first()

            assertEquals(listOf("a"), result.map { it.url })
        }

    // ---- refreshArticles : gọi API -> map -> lưu DAO ----

    @Test
    fun `refreshArticles inserts mapped articles on success`() =
        runTest {
            val dao = FakeArticleDao()
            val json =
                jsonWith(articleJson("https://x/1", "Title 1") + "," + articleJson("https://x/2", "Title 2"))
            val repo = ArticleRepositoryImpl(apiReturning(json), dao)

            val result = repo.refreshArticles()

            assertTrue(result.isSuccess)
            assertEquals(2, dao.articles.value.size)
        }

    @Test
    fun `refreshArticles drops articles with null url or title`() =
        runTest {
            val dao = FakeArticleDao()
            val json =
                jsonWith(
                    articleJson("https://x/valid", "Valid") + "," +
                        articleJson(null, "No URL") + "," +
                        articleJson("https://x/notitle", null),
                )
            val repo = ArticleRepositoryImpl(apiReturning(json), dao)

            repo.refreshArticles()

            // Chỉ bài hợp lệ được lưu; 2 bài thiếu url/title bị toEntity() loại
            assertEquals(listOf("https://x/valid"), dao.articles.value.map { it.url })
        }

    @Test
    fun `refreshArticles returns failure when api errors`() =
        runTest {
            val dao = FakeArticleDao()
            val repo = ArticleRepositoryImpl(apiReturning("{}", HttpStatusCode.InternalServerError), dao)
            val result = repo.refreshArticles()

            assertTrue(result.isFailure)
            assertTrue(dao.articles.value.isEmpty())
        }

    // ---- Các hàm delegate xuống DAO ----

    @Test
    fun `toggleBookmark updates dao`() =
        runTest {
            val dao = FakeArticleDao()
            dao.articles.value = listOf(entity("a", isBookmarked = false))
            val repo = ArticleRepositoryImpl(apiReturning(jsonWith(articleJson("a", "T"))), dao)

            repo.toggleBookmark("a", true)

            assertTrue(
                dao.articles.value
                    .first()
                    .isBookmarked,
            )
        }

    @Test
    fun `getArticleByUrl returns domain article or null`() =
        runTest {
            val dao = FakeArticleDao()
            dao.articles.value = listOf(entity("a"))
            val repo = ArticleRepositoryImpl(apiReturning(jsonWith(articleJson("a", "T"))), dao)

            assertEquals("a", repo.getArticleByUrl("a")?.url)
            assertNull(repo.getArticleByUrl("missing"))
        }
}
