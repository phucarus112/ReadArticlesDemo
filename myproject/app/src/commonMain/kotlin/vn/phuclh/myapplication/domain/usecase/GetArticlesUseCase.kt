package vn.phuclh.myapplication.domain.usecase

import kotlinx.coroutines.flow.Flow
import vn.phuclh.myapplication.domain.model.Article
import vn.phuclh.myapplication.domain.repository.ArticleRepository

class GetArticlesUseCase(
    private val repository: ArticleRepository,
) {
    operator fun invoke(): Flow<List<Article>> = repository.observeArticles()
}
