package vn.phuclh.myapplication.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import vn.phuclh.myapplication.data.local.ArticleEntity

@Serializable
data class NewsResponseDto(
    @SerialName("status") val status: String,
    @SerialName("totalResults") val totalResults: Int = 0,
    @SerialName("articles") val articles: List<ArticleDto> = emptyList(),
)

@Serializable
data class ArticleDto(
    @SerialName("source") val source: SourceDto,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("urlToImage") val urlToImage: String? = null,
    @SerialName("publishedAt") val publishedAt: String? = null,
    @SerialName("content") val content: String? = null,
)

@Serializable
data class SourceDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
)

fun ArticleDto.toEntity(): ArticleEntity? {
    val safeUrl = url?.takeIf { it.isNotBlank() } ?: return null
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null
    return ArticleEntity(
        url = safeUrl,
        title = safeTitle,
        description = description,
        urlToImage = urlToImage,
        source = source.name ?: "Unknown",
        publishedAt = publishedAt ?: "",
        content = content,
    )
}
