package com.gw4app.webhook

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Webhook(
    val name: String,
    val url: String,
    val method: String = "GET",
    val id: String = UUID.randomUUID().toString(),
)

/** Guarda la lista de webhooks en SharedPreferences como JSON. */
class WebhookStore(context: Context) {
    private val prefs = context.getSharedPreferences("webhooks", Context.MODE_PRIVATE)

    fun load(): List<Webhook> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                Webhook(
                    name = o.getString("name"),
                    url = o.getString("url"),
                    method = o.optString("method", "GET"),
                    id = o.getString("id"),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(list: List<Webhook>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("url", it.url)
                    .put("method", it.method)
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private companion object {
        const val KEY = "list"
    }
}
