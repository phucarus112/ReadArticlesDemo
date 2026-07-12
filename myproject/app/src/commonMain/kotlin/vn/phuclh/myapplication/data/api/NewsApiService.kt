package vn.phuclh.myapplication.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import vn.phuclh.myapplication.data.model.NewsResponseDto
import vn.phuclh.myapplication.util.ApiConfig

class NewsApiService(
    private val client: HttpClient,
) {
    suspend fun getTopHeadlines(
        country: String = "us",
        pageSize: Int = 30,
    ): NewsResponseDto =
        client
            .get("v2/top-headlines") {
                parameter("country", country)
                parameter("pageSize", pageSize)
                parameter("apiKey", ApiConfig.NEWS_API_KEY)
            }.body()
}
