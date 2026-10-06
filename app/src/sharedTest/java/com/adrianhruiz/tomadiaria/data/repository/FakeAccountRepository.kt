package com.adrianhruiz.tomadiaria.data.repository

class FakeAccountRepository(
    private val correctPassword: String = "secret1",
) : AccountRepository {

    var shouldFailDeletion: Boolean = false
    var deleted: Boolean = false
        private set

    override suspend fun reauthenticate(password: String) {
        if (password != correctPassword) throw InvalidPasswordException()
    }

    override suspend fun deleteAccountAndData() {
        if (shouldFailDeletion) error("Simulated deletion failure")
        deleted = true
    }
}
