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
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import vn.phuclh.myapplication.data.api.NewsApiService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NewsApiServiceTest {
    private val successJson =
        """
        {
          "status": "ok",
          "totalResults": 2,
          "articles": [
            {
              "source": { "id": "bbc", "name": "BBC News" },
              "title": "Article 1",
              "description": "Desc 1",
              "url": "https://bbc.com/1",
              "urlToImage": null,
              "publishedAt": "2024-01-01T00:00:00Z",
              "content": null
            },
            {
              "source": { "id": null, "name": "CNN" },
              "title": "Article 2",
              "description": null,
              "url": "https://cnn.com/2",
              "urlToImage": "https://img.cnn.com/2.jpg",
              "publishedAt": "2024-01-02T00:00:00Z",
              "content": "Content 2"
            }
          ]
        }
        """.trimIndent()

    private fun buildClient(mockEngine: MockEngine): HttpClient =
        HttpClient(mockEngine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

    @Test
    fun `getTopHeadlines returns parsed articles on success`() =
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(
                        content = successJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val service = NewsApiService(buildClient(engine))
            val response = service.getTopHeadlines()

            assertEquals("ok", response.status)
            assertEquals(2, response.articles.size)
            assertEquals("Article 1", response.articles[0].title)
            assertEquals("BBC News", response.articles[0].source.name)
            assertEquals("https://cnn.com/2", response.articles[1].url)
        }

    @Test
    fun `getTopHeadlines returns empty list when articles is empty`() =
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(
                        content = """{"status":"ok","totalResults":0,"articles":[]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val service = NewsApiService(buildClient(engine))
            val response = service.getTopHeadlines()

            assertEquals(0, response.articles.size)
        }

    @Test
    fun `getTopHeadlines throws on server error`() =
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(
                        content = """{"message":"server error"}""",
                        status = HttpStatusCode.InternalServerError,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }

            val service = NewsApiService(buildClient(engine))
            assertFailsWith<Exception> {
                service.getTopHeadlines()
            }
        }
}
