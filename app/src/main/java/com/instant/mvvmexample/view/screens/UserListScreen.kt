package com.instant.mvvmexample.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instant.mvvmexample.model.User
import com.instant.mvvmexample.view.uistates.UiState
import com.instant.mvvmexample.view.viewmodels.UserListViewModel

@Composable
fun UserListScreen(
    viewModel: UserListViewModel,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredUsers by viewModel.filteredUsers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarActionLabel by remember { mutableStateOf<String?>(null) }
    var onUndoDelete by remember { mutableStateOf<(() -> Unit)?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Search Users") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .shadow(4.dp, RectangleShape),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    errorTextColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color.LightGray,
                    errorContainerColor = Color(0xFFFFCDD2),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    errorCursorColor = MaterialTheme.colorScheme.error,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    disabledIndicatorColor = Color.LightGray,
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    disabledLabelColor = Color.Gray,
                    errorLabelColor = MaterialTheme.colorScheme.error
                )
            )
        }

        when (uiState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6200EE))
                    }
                }
            }

            is UiState.Success -> {
                if (filteredUsers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "No users available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    items(filteredUsers) { user ->
                        UserItem(user = user, onDeleteUser = {
                            viewModel.deleteUser(user)
                            snackbarMessage = "User deleted: ${user.name}"
                            snackbarActionLabel = "Undo"
                            onUndoDelete = { viewModel.undoDelete() }
                        })
                    }
                }
            }

            is UiState.Error -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            UserInput(
                viewModel = viewModel,
                onClearUsers = {
                    viewModel.clearUsers()
                    snackbarMessage = "All users cleared"
                },
                onAddUser = {
                    viewModel.addUser()
                    snackbarMessage = "User added successfully"

                }
            )
        }
    }

    // Show snackbar whenever there's a new message
    snackbarMessage?.let { message ->
        LaunchedEffect(message) {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = snackbarActionLabel,
                withDismissAction = true
            )
            // Handle Snackbar result for "Undo"
            if (result == SnackbarResult.ActionPerformed && snackbarActionLabel == "Undo") {
                onUndoDelete?.invoke()
            }

            // Clear snackbar message and action after it's shown
            snackbarMessage = null
            snackbarActionLabel = null
            onUndoDelete = null
        }
    }
}

@Composable
fun UserItem(user: User, onDeleteUser: (User) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)) // Rounded corners
            .shadow(6.dp, RoundedCornerShape(16.dp)), // Elevation shadow
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Custom background
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Increased padding
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ) // Bolder font for name
                    )
                    Spacer(modifier = Modifier.height(4.dp)) // Small space between name and email
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) // Softer color for email
                    )
                }
                Button(
                    onClick = { onDeleteUser(user) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Delete", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun UserInput(viewModel: UserListViewModel, onClearUsers: () -> Unit, onAddUser : ()->Unit) {
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Name") },
            isError = nameError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            supportingText = {
                if (nameError) {
                    Text(
                        text = "Name cannot be empty",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email") },
            isError = emailError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            supportingText = {
                if (emailError) {
                    Text(
                        text = if (email.isEmpty()) "Email cannot be empty" else "Invalid email address",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { onAddUser() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add user")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add User", color = Color.White)
            }
            Button(
                onClick = onClearUsers,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Clear Users", color = Color.White)
            }
        }
    }
}