package vn.phuclh.myapplication.domain.usecase

import vn.phuclh.myapplication.domain.repository.ArticleRepository

class RefreshArticlesUseCase(
    private val repository: ArticleRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshArticles()
}
