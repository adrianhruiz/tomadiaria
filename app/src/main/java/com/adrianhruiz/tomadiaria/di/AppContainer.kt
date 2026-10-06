package com.adrianhruiz.tomadiaria.di

import android.content.Context
import com.adrianhruiz.tomadiaria.data.backup.BackupRepository
import com.adrianhruiz.tomadiaria.data.backup.FirestoreBackupRepository
import com.adrianhruiz.tomadiaria.data.remote.NetworkModule
import com.adrianhruiz.tomadiaria.data.remote.firebase.FirebaseModule
import com.adrianhruiz.tomadiaria.data.repository.AccountRepository
import com.adrianhruiz.tomadiaria.data.repository.AuthRepository
import com.adrianhruiz.tomadiaria.data.repository.FirebaseAccountRepository
import com.adrianhruiz.tomadiaria.data.repository.FirebaseAuthRepository
import com.adrianhruiz.tomadiaria.data.repository.FirestoreMedicationHistoryRepository
import com.adrianhruiz.tomadiaria.data.repository.FirestoreReasonRepository
import com.adrianhruiz.tomadiaria.data.repository.FirestoreScheduleRepository
import com.adrianhruiz.tomadiaria.data.repository.MedicationCatalogRepository
import com.adrianhruiz.tomadiaria.data.repository.MedicationHistoryRepository
import com.adrianhruiz.tomadiaria.data.repository.MedicationSearchRepository
import com.adrianhruiz.tomadiaria.data.repository.ReasonRepository
import com.adrianhruiz.tomadiaria.data.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient

class AppContainer(context: Context) {

    private val networkModule = NetworkModule(context)

    /** Sostiene los listeners que se comparten entre pantallas; vive lo que viva la app. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val okHttpClient: OkHttpClient get() = networkModule.okHttpClient

    val authRepository: AuthRepository =
        FirebaseAuthRepository(FirebaseModule.auth, FirebaseModule.firestore)

    val accountRepository: AccountRepository =
        FirebaseAccountRepository(FirebaseModule.auth, FirebaseModule.firestore)

    val medicationSearchRepository: MedicationSearchRepository =
        MedicationSearchRepository(networkModule.cimaApiService)

    val medicationCatalogRepository: MedicationCatalogRepository =
        MedicationCatalogRepository(networkModule.cimaApiService)

    val medicationHistoryRepository: MedicationHistoryRepository =
        FirestoreMedicationHistoryRepository(FirebaseModule.firestore, authRepository)

    val reasonRepository: ReasonRepository =
        FirestoreReasonRepository(FirebaseModule.firestore, authRepository, appScope)

    val scheduleRepository: ScheduleRepository =
        FirestoreScheduleRepository(FirebaseModule.firestore, authRepository)

    val backupRepository: BackupRepository =
        FirestoreBackupRepository(FirebaseModule.firestore, authRepository)
}
