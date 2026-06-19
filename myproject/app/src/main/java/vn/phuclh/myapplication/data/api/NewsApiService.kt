package vn.phuclh.myapplication.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import vn.phuclh.myapplication.data.model.NewsResponseDto
import vn.phuclh.myapplication.util.ApiConfig

interface NewsApiService {
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("pageSize") pageSize: Int = 30,
        @Query("apiKey") apiKey: String = ApiConfig.NEWS_API_KEY
    ): NewsResponseDto
}
