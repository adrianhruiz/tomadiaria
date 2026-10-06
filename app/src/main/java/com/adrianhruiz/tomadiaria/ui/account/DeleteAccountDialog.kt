package com.adrianhruiz.tomadiaria.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: DeleteAccountViewModel = viewModel(factory = DeleteAccountViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onDeleted()
    }

    AlertDialog(
        onDismissRequest = { if (!uiState.isDeleting) onDismiss() },
        title = { Text("Eliminar mi cuenta") },
        text = {
            Column {
                Text(
                    "Se borrarán tu cuenta, tus medicamentos, recordatorios e historial de tomas, " +
                        "y las copias automáticas de este móvil. No se puede deshacer.",
                )
                Text(
                    "Si quieres conservar tus datos, cancela y usa antes \"Exportar mis datos\".",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !uiState.isDeleting,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                )
                uiState.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::deleteAccount, enabled = !uiState.isDeleting) {
                if (uiState.isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uiState.isDeleting) { Text("Cancelar") }
        },
    )
}
