package com.gw4app.webhook

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object WebhookSender {
    /** Envía la petición y devuelve el código HTTP, o falla con una excepción. */
    suspend fun send(hook: Webhook): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL(hook.url.trim()).openConnection() as HttpURLConnection
            try {
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                conn.requestMethod = hook.method
                if (hook.method == "POST") {
                    conn.doOutput = true
                    conn.setFixedLengthStreamingMode(0)
                }
                val code = conn.responseCode
                // Consumir el cuerpo para liberar la conexión
                (if (code >= 400) conn.errorStream else conn.inputStream)?.use { it.readBytes() }
                code
            } finally {
                conn.disconnect()
            }
        }
    }
}
