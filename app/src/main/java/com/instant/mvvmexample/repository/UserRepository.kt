package com.instant.mvvmexample.repository

import com.instant.mvvmexample.model.User

interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun addUser(user: User): List<User>
    suspend fun deleteUser(user: User): List<User>
    suspend fun clearUsers(): List<User>
}