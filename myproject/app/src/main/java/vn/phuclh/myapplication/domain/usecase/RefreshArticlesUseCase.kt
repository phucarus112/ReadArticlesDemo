package vn.phuclh.myapplication.domain.usecase

import vn.phuclh.myapplication.domain.repository.ArticleRepository
import javax.inject.Inject

class RefreshArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshArticles()
}
