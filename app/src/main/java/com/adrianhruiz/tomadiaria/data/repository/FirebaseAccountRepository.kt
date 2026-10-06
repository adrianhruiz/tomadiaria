package com.adrianhruiz.tomadiaria.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Subcolecciones que cuelgan de `users/{uid}`. Firestore no borra las subcolecciones al borrar
 * un documento, así que **si se añade una colección nueva bajo el usuario, hay que añadirla
 * aquí**; si no, sus datos sobrevivirían al borrado de la cuenta.
 */
private val USER_COLLECTIONS = listOf("reasons", "takenMedications", HistorySummary.COLLECTION)
private const val SCHEDULES_COLLECTION = "schedules"
private const val DOSES_COLLECTION = "doses"
private const val FIRESTORE_BATCH_LIMIT = 500

class FirebaseAccountRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AccountRepository {

    override suspend fun reauthenticate(password: String) {
        val user = auth.currentUser ?: error("No hay sesión iniciada")
        val email = user.email ?: error("La cuenta no tiene email")
        try {
            user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw InvalidPasswordException(e)
        }
    }

    /**
     * Aquí sí se leen las colecciones enteras: el objetivo es justo encontrar todo lo del usuario
     * para borrarlo. Primero los datos y después la cuenta, porque sin cuenta las reglas de
     * Firestore ya no dejarían borrar nada.
     */
    override suspend fun deleteAccountAndData() {
        val user = auth.currentUser ?: error("No hay sesión iniciada")
        val userDocument = firestore.collection("users").document(user.uid)

        val references = buildList<DocumentReference> {
            USER_COLLECTIONS.forEach { name ->
                addAll(userDocument.collection(name).get().await().documents.map { it.reference })
            }
            userDocument.collection(SCHEDULES_COLLECTION).get().await().documents.forEach { schedule ->
                addAll(schedule.reference.collection(DOSES_COLLECTION).get().await().documents.map { it.reference })
                add(schedule.reference)
            }
            add(userDocument)
        }

        references.chunked(FIRESTORE_BATCH_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it) }
            batch.commit().await()
        }

        user.delete().await()
    }
}
