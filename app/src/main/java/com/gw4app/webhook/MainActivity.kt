package com.gw4app.webhook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = WebhookStore(this)
        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxWidth().background(Color.Black)) {
                    WebhookApp(store)
                }
            }
        }
    }
}

private enum class Screen { LIST, ADD }

@Composable
private fun WebhookApp(store: WebhookStore) {
    val hooks = remember { mutableStateListOf<Webhook>().apply { addAll(store.load()) } }
    val status = remember { mutableStateMapOf<String, String>() }
    var screen by remember { mutableStateOf(Screen.LIST) }
    var deleteMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    when (screen) {
        Screen.LIST -> ScalingLazyColumn(Modifier.fillMaxWidth()) {
            item { Text(if (deleteMode) "Toca para borrar" else "Webhooks") }
            if (hooks.isEmpty()) {
                item {
                    Text(
                        "Añade tu enlace de MacroDroid con «+ Añadir».",
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
            items(hooks.size) { i ->
                val hook = hooks[i]
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(hook.name) },
                    secondaryLabel = status[hook.id]?.let { { Text(it) } },
                    colors = if (deleteMode) ChipDefaults.chipColors(backgroundColor = Color(0xFF7A1F1F))
                    else ChipDefaults.primaryChipColors(),
                    onClick = {
                        if (deleteMode) {
                            hooks.remove(hook)
                            store.save(hooks.toList())
                        } else {
                            status[hook.id] = "Enviando…"
                            scope.launch {
                                val r = WebhookSender.send(hook)
                                status[hook.id] = r.fold(
                                    onSuccess = { if (it in 200..299) "✓ $it" else "✗ HTTP $it" },
                                    onFailure = { "✗ sin conexión" },
                                )
                            }
                        }
                    },
                )
            }
            item {
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("+ Añadir") },
                    colors = ChipDefaults.secondaryChipColors(),
                    onClick = { deleteMode = false; screen = Screen.ADD },
                )
            }
            if (hooks.isNotEmpty()) {
                item {
                    Chip(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (deleteMode) "Listo" else "Borrar") },
                        colors = ChipDefaults.secondaryChipColors(),
                        onClick = { deleteMode = !deleteMode },
                    )
                }
            }
        }

        Screen.ADD -> AddScreen(
            onSave = { hook ->
                hooks.add(hook)
                store.save(hooks.toList())
                screen = Screen.LIST
            },
            onCancel = { screen = Screen.LIST },
        )
    }
}

@Composable
private fun AddScreen(onSave: (Webhook) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://trigger.macrodroid.com/") }
    var post by remember { mutableStateOf(false) }
    val valid = name.isNotBlank() && url.trim().let { it.startsWith("http://") || it.startsWith("https://") }

    ScalingLazyColumn(Modifier.fillMaxWidth()) {
        item { Text("Nuevo webhook") }
        item { Field(name, "Nombre") { name = it } }
        item { Field(url, "URL del webhook") { url = it } }
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Método: ${if (post) "POST" else "GET"}") },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = { post = !post },
            )
        }
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Guardar") },
                enabled = valid,
                onClick = { onSave(Webhook(name.trim(), url.trim(), if (post) "POST" else "GET")) },
            )
        }
        item {
            Chip(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cancelar") },
                colors = ChipDefaults.secondaryChipColors(),
                onClick = onCancel,
            )
        }
    }
}

@Composable
private fun Field(value: String, hint: String, onChange: (String) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (value.isEmpty()) Text(hint, color = Color.Gray)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = TextStyle(color = Color.White),
            cursorBrush = SolidColor(Color.White),
        )
    }
}
