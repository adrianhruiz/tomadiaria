package com.adrianhruiz.tomadiaria.data.repository

interface AccountRepository {

    /**
     * Comprueba la contraseña del usuario con sesión iniciada. Firebase exige un inicio de sesión
     * reciente para borrar la cuenta, y así además se confirma que quien borra es el dueño.
     *
     * @throws InvalidPasswordException si la contraseña no es correcta.
     */
    suspend fun reauthenticate(password: String)

    /** Borra todos los datos del usuario en Firestore y después su cuenta de Authentication. */
    suspend fun deleteAccountAndData()
}

class InvalidPasswordException(cause: Throwable? = null) : Exception("Contraseña incorrecta", cause)
