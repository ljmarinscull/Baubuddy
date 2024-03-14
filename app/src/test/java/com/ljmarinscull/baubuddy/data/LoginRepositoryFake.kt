package com.ljmarinscull.baubuddy.data

import com.ljmarinscull.baubuddy.domain.repository.ILoginRepository

class LoginRepositoryFake: ILoginRepository {
    private var profileToReturn = "authorization"
    var errorToReturn: Exception? = null

    override suspend fun login(username: String, password: String): Result<String> {
        return if(errorToReturn != null) {
            Result.failure(errorToReturn!!)
        } else Result.success(profileToReturn)
    }
}