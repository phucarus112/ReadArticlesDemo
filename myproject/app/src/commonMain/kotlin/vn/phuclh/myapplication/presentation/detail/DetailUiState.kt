package vn.phuclh.myapplication.presentation.detail

import vn.phuclh.myapplication.domain.model.Article

sealed interface DetailUiState {
    data object Loading : DetailUiState

    data class Success(
        val article: Article,
    ) : DetailUiState

    data class Error(
        val message: String,
    ) : DetailUiState
}
