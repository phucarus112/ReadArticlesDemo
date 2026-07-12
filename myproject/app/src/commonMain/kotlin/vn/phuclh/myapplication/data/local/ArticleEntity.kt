package vn.phuclh.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import vn.phuclh.myapplication.domain.model.Article

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val source: String,
    val publishedAt: String,
    val content: String?,
    val isBookmarked: Boolean = false,
    val isSeen: Boolean = false,
    val fetchedAt: Long = 0L,
)

fun ArticleEntity.toDomain() =
    Article(
        url = url,
        title = title,
        description = description,
        imageUrl = urlToImage,
        source = source,
        publishedAt = publishedAt,
        content = content,
        isBookmarked = isBookmarked,
        isSeen = isSeen,
    )
