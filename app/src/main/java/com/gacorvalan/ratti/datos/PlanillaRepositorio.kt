package com.gacorvalan.ratti.datos

import com.gacorvalan.ratti.datos.modelo.ModeloFinanzas
import java.io.IOException
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import java.time.LocalDate
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Lee la planilla con values:batchGet. Solo GET: no escribe ni borra celdas.
 * Si el token venció, pide uno nuevo una sola vez y reintenta.
 */
internal class PlanillaRepositorio {
    suspend fun leer(pedirToken: suspend () -> String): Map<String, List<List<ValorCrudo?>>> {
        val primero = pedirToken()
        try {
            return leerCon(primero)
        } catch (_: TokenRechazado) {
            val segundo = pedirToken()
            return try {
                leerCon(segundo)
            } catch (_: TokenRechazado) {
                throw ErrorPlanilla.PermisoVencido
            }
        }
    }

    /** La planilla ya interpretada: ciclo, semáforo neto, tarjetas, movimientos, historial y ahorros. */
    suspend fun leerModelo(
        pedirToken: suspend () -> String,
        hoy: LocalDate = LocalDate.now(),
    ): ModeloFinanzas = interpretarPlanilla(leer(pedirToken), hoy)

    private suspend fun leerCon(token: String): Map<String, List<List<ValorCrudo?>>> {
        val json = withContext(Dispatchers.IO) { traerValores(token) }
        return interpretarValores(json)
    }
}

private class TokenRechazado : Exception()

private fun traerValores(token: String): String {
    val conexion = (URL(urlBatchGet()).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        instanceFollowRedirects = false
        connectTimeout = 15_000
        readTimeout = 20_000
        setRequestProperty("Authorization", "Bearer $token")
        setRequestProperty("Accept", "application/json")
    }
    try {
        val codigo = try {
            conexion.responseCode
        } catch (e: IOException) {
            throw falloDeRed(e)
        }
        val cuerpo = try {
            conexion.cuerpo()
        } catch (e: IOException) {
            throw falloDeRed(e)
        }
        when (codigo) {
            200 -> return cuerpo
            401 -> throw TokenRechazado()
            403 -> throw ErrorPlanilla.Prohibido
            404 -> throw ErrorPlanilla.NoEncontrada
            400 -> {
                val pestana = pestanaEnError(cuerpo)
                if (pestana != null || cuerpo.contains("Unable to parse range", ignoreCase = true)) {
                    throw ErrorPlanilla.PestanaFaltante(pestana)
                }
                throw ErrorPlanilla.Http(codigo)
            }
            else -> throw ErrorPlanilla.Http(codigo)
        }
    } finally {
        conexion.disconnect()
    }
}

private fun HttpURLConnection.cuerpo(): String {
    val stream = if (responseCode in 200..299) inputStream else errorStream
    return stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
}

private fun falloDeRed(error: IOException): Exception {
    if (Thread.currentThread().isInterrupted) {
        throw CancellationException("Se canceló la lectura de la planilla", error)
    }
    return if (esRed(error)) ErrorPlanilla.SinConexion else ErrorPlanilla.Lectura
}

private fun esRed(error: Throwable): Boolean {
    var actual: Throwable? = error
    while (actual != null) {
        if (actual is UnknownHostException || actual is SocketTimeoutException || actual is SocketException) {
            return true
        }
        actual = actual.cause
    }
    return false
}

private fun urlBatchGet(): String {
    val base = "https://sheets.googleapis.com/v4/spreadsheets/$ID_PLANILLA_FINNI/values:batchGet"
    val rangos = PESTANAS_FINNI.joinToString("&") { nombre ->
        val rango = "'${nombre.replace("'", "''")}'"
        "ranges=" + URLEncoder.encode(rango, Charsets.UTF_8.name())
    }
    return "$base?$rangos&valueRenderOption=UNFORMATTED_VALUE&dateTimeRenderOption=FORMATTED_STRING"
}

private val RANGO_INVALIDO = Regex(
    """Unable to parse range:\s*(?:'((?:[^']|'')*)'|([^'!]+))""",
    RegexOption.IGNORE_CASE,
)

private fun pestanaEnError(cuerpo: String): String? {
    val mensaje = try {
        JSONObject(cuerpo).optJSONObject("error")?.optString("message")
    } catch (_: JSONException) {
        null
    }.orEmpty().ifBlank { cuerpo }
    val match = RANGO_INVALIDO.find(mensaje) ?: return null
    val crudo = match.groupValues[1].ifBlank { match.groupValues[2] }.trim()
    return crudo.replace("''", "'").ifBlank { null }
}

internal fun interpretarValores(json: String): Map<String, List<List<ValorCrudo?>>> {
    val raiz = try {
        JSONObject(json)
    } catch (_: JSONException) {
        throw ErrorPlanilla.Ilegible
    }
    val rangos = raiz.optJSONArray("valueRanges") ?: throw ErrorPlanilla.Ilegible
    val porNombre = LinkedHashMap<String, List<List<ValorCrudo?>>>()
    for (i in 0 until rangos.length()) {
        val item = rangos.optJSONObject(i) ?: continue
        val nombre = nombreDeRango(item.optString("range")) ?: PESTANAS_FINNI.getOrNull(i) ?: continue
        val values = item.optJSONArray("values")
        porNombre[nombre] = if (values == null) emptyList() else filas(values)
    }
    val faltante = PESTANAS_FINNI.firstOrNull { it !in porNombre }
    if (faltante != null) throw ErrorPlanilla.PestanaFaltante(faltante)
    return PESTANAS_FINNI.associateWith { porNombre.getValue(it) }
}

private fun nombreDeRango(rango: String): String? {
    if (rango.isBlank()) return null
    val hoja = rango.substringBefore('!').trim()
    val sinComillas = if (hoja.length >= 2 && hoja.first() == '\'' && hoja.last() == '\'') {
        hoja.substring(1, hoja.lastIndex).replace("''", "'")
    } else {
        hoja
    }
    return PESTANAS_FINNI.firstOrNull { it == sinComillas }
}

private fun filas(values: JSONArray): List<List<ValorCrudo?>> {
    return try {
        List(values.length()) { i ->
            val fila = values.optJSONArray(i) ?: return@List emptyList()
            List(fila.length()) { j -> celda(fila, j) }
        }
    } catch (_: JSONException) {
        throw ErrorPlanilla.Ilegible
    }
}

private fun celda(fila: JSONArray, indice: Int): ValorCrudo? {
    if (fila.isNull(indice)) return null
    return when (val valor = fila.get(indice)) {
        is String -> ValorCrudo.Texto(valor)
        is Number -> ValorCrudo.Numero(valor.toDouble())
        is Boolean -> ValorCrudo.Texto(valor.toString())
        else -> ValorCrudo.Texto(valor.toString())
    }
}

internal fun numeroEnTexto(valor: Double): String {
    if (!valor.isFinite()) return valor.toString()
    return BigDecimal(valor.toString()).stripTrailingZeros().toPlainString()
}
