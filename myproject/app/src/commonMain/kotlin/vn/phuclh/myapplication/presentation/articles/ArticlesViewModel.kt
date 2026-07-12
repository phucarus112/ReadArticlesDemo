package vn.phuclh.myapplication.presentation.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.phuclh.myapplication.domain.usecase.GetArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.RefreshArticlesUseCase

class ArticlesViewModel(
    private val getArticlesUseCase: GetArticlesUseCase,
    private val refreshArticlesUseCase: RefreshArticlesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArticlesUiState(isLoading = true))
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    init {
        observeArticles()
        refresh(isInitial = true)
    }

    fun handleIntent(intent: ArticlesIntent) {
        when (intent) {
            is ArticlesIntent.FetchData -> refresh(isInitial = true)
            is ArticlesIntent.RefreshData -> refresh(isInitial = false)
            is ArticlesIntent.ToggleBookmark -> Unit
            is ArticlesIntent.MarkAllArticlesSeen -> Unit
        }
    }

    private fun observeArticles() {
        viewModelScope.launch {
            getArticlesUseCase().collect { articles ->
                _uiState.update { it.copy(articles = articles, isLoading = false) }
            }
        }
    }

    private fun refresh(isInitial: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = !isInitial, error = null) }
            refreshArticlesUseCase()
                .onSuccess { _uiState.update { it.copy(isRefreshing = false) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message, isRefreshing = false) } }
        }
    }
}
