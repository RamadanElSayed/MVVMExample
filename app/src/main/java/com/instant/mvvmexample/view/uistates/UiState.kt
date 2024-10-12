package com.instant.mvvmexample.view.uistates

import com.instant.mvvmexample.model.User

sealed class UiState {
    data object Loading : UiState()
    data class Success(val users: List<User>, val message: String = "") : UiState()
    data class Error(val message: String) : UiState()
}