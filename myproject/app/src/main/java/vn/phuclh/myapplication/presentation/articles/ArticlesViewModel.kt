package vn.phuclh.myapplication.presentation.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.phuclh.myapplication.domain.usecase.GetArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.RefreshArticlesUseCase
import vn.phuclh.myapplication.domain.usecase.ToggleBookmarkUseCase
import vn.phuclh.myapplication.domain.repository.ArticleRepository
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase,
    private val refreshArticlesUseCase: RefreshArticlesUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val repository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticlesUiState(isLoading = true))
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    init {
        observeArticles()
        handleIntent(ArticlesIntent.FetchData)
    }

    fun handleIntent(intent: ArticlesIntent) {
        when (intent) {
            is ArticlesIntent.FetchData -> refresh(isInitial = true)
            is ArticlesIntent.RefreshData -> refresh(isInitial = false)
            is ArticlesIntent.ToggleBookmark -> toggleBookmark(intent.url, intent.isBookmarked)
            is ArticlesIntent.MarkAllArticlesSeen -> markAllSeen()
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
                .onSuccess {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isRefreshing = false) }
                }
        }
    }

    private fun toggleBookmark(url: String, isBookmarked: Boolean) {
        viewModelScope.launch { toggleBookmarkUseCase(url, isBookmarked) }
    }

    private fun markAllSeen() {
        viewModelScope.launch { repository.markAllSeen() }
    }
}
