package vn.phuclh.myapplication.data.model

import com.google.gson.annotations.SerializedName
import vn.phuclh.myapplication.data.local.ArticleEntity

data class NewsResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("totalResults") val totalResults: Int = 0,
    @SerializedName("articles") val articles: List<ArticleDto> = emptyList()
)

data class ArticleDto(
    @SerializedName("source") val source: SourceDto,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("urlToImage") val urlToImage: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("content") val content: String?
)

data class SourceDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?
)

fun ArticleDto.toEntity(): ArticleEntity? {
    val safeUrl = url?.takeIf { it.isNotBlank() } ?: ""
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: ""
    return ArticleEntity(
        url = safeUrl,
        title = safeTitle,
        description = description,
        urlToImage = urlToImage,
        source = source.name ?: "Unknown",
        publishedAt = publishedAt ?: "",
        content = content
    )
}
