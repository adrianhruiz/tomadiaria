package com.adrianhruiz.tomadiaria.data.repository

import com.adrianhruiz.tomadiaria.model.Reason
import kotlinx.coroutines.flow.Flow

interface ReasonRepository {

    fun getAll(): Flow<List<Reason>>

    suspend fun findOrCreate(nombre: String): Reason
}
