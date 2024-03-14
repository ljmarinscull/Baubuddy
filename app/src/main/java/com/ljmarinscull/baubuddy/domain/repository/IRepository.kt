package com.ljmarinscull.baubuddy.domain.repository

fun interface ILoginRepository{
    suspend fun login(username: String, password: String): Result<String>
}

interface IRepository: ILoginRepository
