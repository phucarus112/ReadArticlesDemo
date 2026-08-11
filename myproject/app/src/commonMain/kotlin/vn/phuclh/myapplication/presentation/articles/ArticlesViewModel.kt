package vn.phuclh.myapplication.presentation.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.phuclh.myapplication.domain.usecase.GetArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.MarkArticlesSeenUseCase
import vn.phuclh.myapplication.domain.usecase.RefreshArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase

class ArticlesViewModel(
    private val getArticlesUseCase: GetArticlesUseCase,
    private val refreshArticlesUseCase: RefreshArticlesUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val markArticlesSeenUseCase: MarkArticlesSeenUseCase,
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
            is ArticlesIntent.ToggleBookmark ->
                viewModelScope.launch {
                    toggleBookmarkUseCase(intent.url, intent.isBookmarked)
                }
            is ArticlesIntent.MarkAllArticlesSeen ->
                viewModelScope.launch {
                    markArticlesSeenUseCase()
                }
        }
    }

    private fun observeArticles() {
        viewModelScope.launch {
            getArticlesUseCase().collect { articles ->
                _uiState.update { it.copy(articles = articles.toImmutableList(), isLoading = false) }
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
