package vn.phuclh.myapplication.domain.usecase

import vn.phuclh.myapplication.domain.repository.ArticleRepository

class ToggleBookmarkUseCase(
    private val repository: ArticleRepository,
) {
    suspend operator fun invoke(
        url: String,
        isBookmarked: Boolean,
    ) = repository.toggleBookmark(url, isBookmarked)
}
