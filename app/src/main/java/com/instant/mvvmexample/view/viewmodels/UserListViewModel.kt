package com.instant.mvvmexample.view.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instant.mvvmexample.model.User
import com.instant.mvvmexample.repository.UserRepository
import com.instant.mvvmexample.repository.UserRepositoryImpl
import com.instant.mvvmexample.view.uistates.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class UserListViewModel : ViewModel() {

    private val repository: UserRepository = UserRepositoryImpl()

    // User input fields and validation errors
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _nameError = MutableStateFlow(false)
    val nameError: StateFlow<Boolean> = _nameError

    private val _emailError = MutableStateFlow(false)
    val emailError: StateFlow<Boolean> = _emailError

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers: StateFlow<List<User>> = _filteredUsers

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private var recentlyDeletedUser: User? = null

    init {
        observeSearchQuery()
        fetchUsers()
    }

    // Fetch users from the repository
    fun fetchUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val users = repository.getUsers()
                _uiState.value = UiState.Success(users, if (users.isEmpty()) "No users available" else "Users fetched successfully!")
                _filteredUsers.value = users
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    filterUsers(query)
                }
        }
    }

    private fun filterUsers(query: String) {
        val users = (uiState.value as? UiState.Success)?.users ?: emptyList()
        _filteredUsers.value = if (query.isEmpty()) users else users.filter {
            it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateName(input: String) {
        _name.value = input
        _nameError.value = input.isBlank()
    }

    fun updateEmail(input: String) {
        _email.value = input
        _emailError.value = input.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }

    fun addUser() {
        if (_name.value.isNotBlank() && _email.value.isNotBlank() && !_emailError.value) {
            viewModelScope.launch {
                _uiState.value = UiState.Loading
                try {
                    val newUser = User(id = (0..1000).random(), name = _name.value, email = _email.value)
                    val users = repository.addUser(newUser)
                    _uiState.value = UiState.Success(users, "User added successfully!")
                    _filteredUsers.value = users
                    _name.value = ""
                    _email.value = ""
                } catch (e: Exception) {
                    _uiState.value = UiState.Error("Error adding user: ${e.message}")
                }
            }
        } else {
            _nameError.value = _name.value.isBlank()
            _emailError.value = _email.value.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            recentlyDeletedUser = user
            _uiState.value = UiState.Loading
            try {
                val users = repository.deleteUser(user)
                _uiState.value = UiState.Success(users, "User deleted successfully!")
                _filteredUsers.value = users
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error deleting user: ${e.message}")
            }
        }
    }

    fun undoDelete() {
        recentlyDeletedUser?.let {
            updateName(it.name)
            updateEmail(it.email)
            addUser()
        }
        recentlyDeletedUser = null
    }

    fun clearUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val users = repository.clearUsers()
                _uiState.value = UiState.Success(users, "All users cleared!")
                _filteredUsers.value = users
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error clearing users: ${e.message}")
            }
        }
    }
}