package vn.phuclh.myapplication.domain.usecase

import vn.phuclh.myapplication.domain.repository.ArticleRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(url: String, isBookmarked: Boolean) =
        repository.toggleBookmark(url, isBookmarked)
}
