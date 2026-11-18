package com.gacorvalan.ratti.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.gacorvalan.ratti.BuildConfig
import com.gacorvalan.ratti.R
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/** Ver planillas, sin poder editarlas. */
internal const val PLANILLAS_SOLO_LECTURA =
    "https://www.googleapis.com/auth/spreadsheets.readonly"

internal data class SesionGoogle(
    val email: String?,
    val error: String?,
    val ocupado: Boolean,
    val entrar: () -> Unit,
    val salir: () -> Unit,
)

@Composable
internal fun rememberSesionGoogle(): SesionGoogle {
    val context = LocalContext.current
    val alcance = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var mailPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var ocupado by remember { mutableStateOf(false) }
    val enCurso = remember { AtomicBoolean(false) }

    val permiso = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { resultado ->
        enCurso.set(false)
        ocupado = false
        val cuenta = mailPendiente
        mailPendiente = null
        when (val lectura = leerResultadoPermiso(context, resultado)) {
            PermisoPlanillas.Concedido -> {
                if (cuenta.isNullOrBlank()) {
                    email = null
                    error = context.getString(R.string.error_permiso_denegado)
                } else {
                    email = cuenta
                    error = null
                }
            }
            is PermisoPlanillas.Fallo -> {
                email = null
                error = context.getString(lectura.mensaje)
            }
        }
    }

    return SesionGoogle(
        email = email,
        error = error,
        ocupado = ocupado,
        entrar = entrar@{
            if (ocupado || !enCurso.compareAndSet(false, true)) return@entrar
            val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
            if (clientId.isBlank()) {
                enCurso.set(false)
                email = null
                error = context.getString(R.string.error_falta_client_id)
                return@entrar
            }
            val actividad = context.buscarActividad()
            if (actividad == null) {
                enCurso.set(false)
                email = null
                error = context.getString(R.string.error_sesion)
                return@entrar
            }
            email = null
            error = null
            ocupado = true
            alcance.launch {
                val cuenta = try {
                    entrarConGoogle(actividad, clientId)
                } catch (e: CancellationException) {
                    enCurso.set(false)
                    throw e
                } catch (e: Exception) {
                    enCurso.set(false)
                    ocupado = false
                    email = null
                    error = actividad.getString(mensajeInicioSesion(e))
                    return@launch
                }
                mailPendiente = cuenta
                try {
                    val autorizacion = Identity.getAuthorizationClient(actividad)
                        .authorize(solicitudPlanillas())
                        .awaitEnActividad(actividad)
                    when {
                        autorizacion.hasResolution() -> {
                            val pendiente = autorizacion.pendingIntent
                            if (pendiente == null) {
                                enCurso.set(false)
                                mailPendiente = null
                                ocupado = false
                                email = null
                                error = actividad.getString(R.string.error_permiso)
                            } else {
                                try {
                                    permiso.launch(
                                        IntentSenderRequest.Builder(pendiente.intentSender).build(),
                                    )
                                } catch (_: Exception) {
                                    enCurso.set(false)
                                    mailPendiente = null
                                    ocupado = false
                                    email = null
                                    error = actividad.getString(R.string.error_permiso)
                                }
                            }
                        }
                        autorizacion.puedeLeerPlanillas() -> {
                            enCurso.set(false)
                            mailPendiente = null
                            ocupado = false
                            email = cuenta
                            error = null
                        }
                        else -> {
                            enCurso.set(false)
                            mailPendiente = null
                            ocupado = false
                            email = null
                            error = actividad.getString(R.string.error_permiso_denegado)
                        }
                    }
                } catch (e: CancellationException) {
                    enCurso.set(false)
                    throw e
                } catch (e: Exception) {
                    enCurso.set(false)
                    mailPendiente = null
                    ocupado = false
                    email = null
                    error = actividad.getString(mensajePermiso(e))
                }
            }
        },
        salir = {
            alcance.launch {
                try {
                    cerrarSesionGoogle(context.applicationContext)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // La pantalla vuelve a "Entrar" aunque Google no haya limpiado su sesión.
                }
                enCurso.set(false)
                mailPendiente = null
                ocupado = false
                email = null
                error = null
            }
        },
    )
}

private suspend fun entrarConGoogle(context: Context, webClientId: String): String {
    val opcion = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
    val pedido = GetCredentialRequest.Builder()
        .addCredentialOption(opcion)
        .build()
    val resultado = CredentialManager.create(context).getCredential(
        context = context,
        request = pedido,
    )
    val credencial = resultado.credential
    if (credencial !is CustomCredential ||
        credencial.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        throw GoogleSignInException(R.string.error_respuesta)
    }
    val google = try {
        GoogleIdTokenCredential.createFrom(credencial.data)
    } catch (_: GoogleIdTokenParsingException) {
        throw GoogleSignInException(R.string.error_respuesta)
    }
    return google.email?.trim()?.takeIf { it.isNotEmpty() }
        ?: throw GoogleSignInException(R.string.error_sin_mail)
}

private suspend fun cerrarSesionGoogle(context: Context) {
    CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
}

private fun solicitudPlanillas(): AuthorizationRequest =
    AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(PLANILLAS_SOLO_LECTURA)))
        .build()

private sealed interface PermisoPlanillas {
    data object Concedido : PermisoPlanillas
    data class Fallo(@StringRes val mensaje: Int) : PermisoPlanillas
}

private fun leerResultadoPermiso(context: Context, resultado: ActivityResult): PermisoPlanillas {
    val data = resultado.data
    if (resultado.resultCode != Activity.RESULT_OK || data == null) {
        return PermisoPlanillas.Fallo(R.string.error_cancelo_permiso)
    }
    return try {
        val autorizacion = Identity.getAuthorizationClient(context)
            .getAuthorizationResultFromIntent(data)
        if (autorizacion.puedeLeerPlanillas()) {
            PermisoPlanillas.Concedido
        } else {
            PermisoPlanillas.Fallo(R.string.error_permiso_denegado)
        }
    } catch (e: ApiException) {
        PermisoPlanillas.Fallo(mensajePermiso(e))
    }
}

private fun AuthorizationResult.puedeLeerPlanillas(): Boolean {
    if (accessToken.isNullOrBlank()) return false
    if (grantedScopes.isEmpty()) return true
    return grantedScopes.any { it == PLANILLAS_SOLO_LECTURA }
}

@StringRes
private fun mensajeInicioSesion(error: Throwable): Int {
    return when (error) {
        is GoogleSignInException -> error.mensaje
        is GetCredentialCancellationException -> R.string.error_cancelo_sesion
        is NoCredentialException -> R.string.error_sin_cuenta
        is GetCredentialInterruptedException -> R.string.error_interrumpido
        is GetCredentialProviderConfigurationException -> R.string.error_config
        is GetCredentialException -> when (error.codigoApi()) {
            CODIGO_DESARROLLADOR -> R.string.error_config
            CODIGO_RED -> R.string.error_red
            CODIGO_CANCELADO, CODIGO_CANCELADO_SESION -> R.string.error_cancelo_sesion
            else -> R.string.error_sesion
        }
        else -> when (error.codigoApi()) {
            CODIGO_DESARROLLADOR -> R.string.error_config
            CODIGO_RED -> R.string.error_red
            else -> R.string.error_sesion
        }
    }
}

@StringRes
private fun mensajePermiso(error: Throwable): Int = when (error.codigoApi()) {
    CODIGO_DESARROLLADOR -> R.string.error_config
    CODIGO_RED -> R.string.error_red
    CODIGO_CANCELADO, CODIGO_CANCELADO_SESION -> R.string.error_cancelo_permiso
    else -> R.string.error_permiso
}

private fun Throwable.codigoApi(): Int? {
    var actual: Throwable? = this
    while (actual != null) {
        if (actual is ApiException) return actual.statusCode
        actual = actual.cause
    }
    return null
}

private fun Context.buscarActividad(): Activity? {
    var actual: Context = this
    while (actual is ContextWrapper) {
        if (actual is Activity) return actual
        actual = actual.baseContext
    }
    return null
}

private suspend fun <T> Task<T>.awaitEnActividad(actividad: Activity): T =
    suspendCancellableCoroutine { continuacion ->
        addOnCompleteListener(actividad) { tarea ->
            if (!continuacion.isActive) return@addOnCompleteListener
            val fallo = tarea.exception
            when {
                tarea.isSuccessful -> continuacion.resume(tarea.result)
                fallo != null -> continuacion.resumeWithException(fallo)
                else -> continuacion.cancel()
            }
        }
    }

private class GoogleSignInException(@StringRes val mensaje: Int) : Exception()

private const val CODIGO_RED = 7
private const val CODIGO_DESARROLLADOR = 10
private const val CODIGO_CANCELADO = 16
private const val CODIGO_CANCELADO_SESION = 12501
