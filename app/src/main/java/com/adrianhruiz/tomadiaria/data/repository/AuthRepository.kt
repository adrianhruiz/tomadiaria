package com.adrianhruiz.tomadiaria.data.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentUserId: String?

    val authStateUserId: Flow<String?>

    /**
     * Crea la cuenta y guarda junto a ella qué versión de la política de privacidad aceptó el
     * usuario y cuándo: el consentimiento para tratar datos de salud tiene que poder demostrarse.
     */
    suspend fun signUp(email: String, password: String, privacyPolicyVersion: String)

    suspend fun signIn(email: String, password: String)

    fun signOut()
}
