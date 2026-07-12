package vn.phuclh.myapplication.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.phuclh.myapplication.domain.repository.ArticleRepository
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase

class DetailViewModel(
    private val repository: ArticleRepository,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val articleUrl: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            val article = repository.getArticleByUrl(articleUrl)
            _uiState.value =
                if (article != null) {
                    DetailUiState.Success(article)
                } else {
                    DetailUiState.Error("Article not found")
                }
        }
    }

    fun toggleBookmark() {
        val state = _uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch {
            toggleBookmarkUseCase(state.article.url, !state.article.isBookmarked)
            loadArticle()
        }
    }
}
