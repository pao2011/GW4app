@file:OptIn(ExperimentalFoundationApi::class)

package com.gw4app.webhook

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Text
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = WebhookStore(this)
        setContent { App(store) }
    }
}

@Composable
private fun App(store: WebhookStore) {
    val hooks = remember { mutableStateListOf<Webhook>().apply { addAll(store.load()) } }
    val status = remember { mutableStateMapOf<String, String>() }
    var editing by remember { mutableStateOf<Webhook?>(null) } // id vacío = nuevo
    var confirmDelete by remember { mutableStateOf<Webhook?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun persist() = store.save(hooks.toList())

    editing?.let { current ->
        Editor(
            initial = current,
            onSave = { saved ->
                val i = hooks.indexOfFirst { it.id == saved.id }
                if (i >= 0) hooks[i] = saved else hooks.add(saved)
                persist()
                editing = null
            },
            onCancel = { editing = null },
            onDelete = { hooks.removeAll { it.id == current.id }; persist(); editing = null },
        )
        return
    }

    confirmDelete?.let { h ->
        ScalingLazyColumn(Modifier.fillMaxWidth()) {
            item { Text("¿Borrar «${h.name}»?") }
            item {
                Text("Sí", Modifier.clickable2 { hooks.remove(h); persist(); confirmDelete = null }.padding(8.dp))
            }
            item { Text("No", Modifier.clickable2 { confirmDelete = null }.padding(8.dp)) }
        }
        return
    }

    ScalingLazyColumn(Modifier.fillMaxWidth()) {
        items(hooks.size) { i ->
            val h = hooks[i]
            Text(
                text = h.name + (status[h.id]?.let { "  $it" } ?: ""),
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            status[h.id] = "…"
                            scope.launch {
                                val r = WebhookSender.send(h)
                                val ok = r.getOrNull()?.let { it in 200..299 } == true
                                status[h.id] = if (ok) "✓" else "✗"
                                vibrate(context, ok)
                            }
                        },
                        onLongClick = { editing = h },
                        onLongClickLabel = "Editar",
                    )
                    .padding(8.dp),
            )
        }
        item { Text("+", Modifier.clickable2 { editing = Webhook("", "https://trigger.macrodroid.com/") }.padding(8.dp)) }
    }
}

@Composable
private fun Editor(initial: Webhook, onSave: (Webhook) -> Unit, onCancel: () -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var url by remember { mutableStateOf(initial.url) }
    var post by remember { mutableStateOf(initial.method == "POST") }
    var askDelete by remember { mutableStateOf(false) }
    val valid = name.isNotBlank() && url.trim().startsWith("http")
    val isNew = initial.name.isEmpty()

    ScalingLazyColumn(Modifier.fillMaxWidth()) {
        item { Field(name, "Nombre") { name = it } }
        item { Field(url, "URL") { url = it } }
        item { Text(if (post) "POST" else "GET", Modifier.clickable2 { post = !post }.padding(8.dp)) }
        item {
            Text(
                "Guardar",
                Modifier.clickable2(valid) {
                    onSave(initial.copy(name = name.trim(), url = url.trim(), method = if (post) "POST" else "GET"))
                }.padding(8.dp),
                color = if (valid) Color.Unspecified else Color.Gray,
            )
        }
        item { Text("Cancelar", Modifier.clickable2 { onCancel() }.padding(8.dp)) }
        if (!isNew) item {
            Text(
                if (askDelete) "¿Seguro? Toca otra vez" else "Borrar",
                Modifier.clickable2 { if (askDelete) onDelete() else askDelete = true }.padding(8.dp),
            )
        }
    }
}

@Composable
private fun Field(value: String, hint: String, onChange: (String) -> Unit) {
    if (value.isEmpty()) Text(hint, color = Color.Gray)
    BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = TextStyle(color = Color.White),
        cursorBrush = SolidColor(Color.White),
        modifier = Modifier.fillMaxWidth().padding(8.dp),
    )
}

private fun Modifier.clickable2(enabled: Boolean = true, onClick: () -> Unit) =
    this.then(Modifier.combinedClickable(enabled = enabled, onClick = onClick))

private fun vibrate(context: Context, ok: Boolean) {
    val v = context.getSystemService(Vibrator::class.java) ?: return
    val effect = if (ok) VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE)
    else VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 80), -1)
    v.vibrate(effect)
}
