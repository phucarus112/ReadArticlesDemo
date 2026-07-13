package vn.phuclh.myapplication.domain.usecase

import vn.phuclh.myapplication.domain.repository.ArticleRepository

class MarkArticlesSeenUseCase(
    private val repository: ArticleRepository,
) {
    suspend operator fun invoke() = repository.markAllSeen()
}
