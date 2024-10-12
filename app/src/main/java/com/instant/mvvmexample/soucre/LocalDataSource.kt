package com.instant.mvvmexample.soucre

import com.instant.mvvmexample.model.User
import kotlinx.coroutines.delay

class LocalDataSource {

    private var users = mutableListOf<User>()

     suspend fun getUsers(): List<User> {
        delay(1000)
        return users
    }

     suspend fun addUser(user: User): List<User> {
        delay(500)
        users.add(user)
        return users
    }

     suspend fun deleteUser(user: User): List<User> {
        delay(500)
        users.remove(user)
        return users
    }

     suspend fun clearUsers(): List<User> {
        delay(500)
        users.clear()
        return users
    }
}