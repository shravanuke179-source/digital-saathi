package com.example.ui.components

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Keeps existing forms open on failure and acknowledges only completed cloud writes. */
class CloudAction(private val scope: CoroutineScope, private val onError: (String) -> Unit) {
    var busy by mutableStateOf(false)
        private set
    fun launch(action: suspend () -> Unit) {
        if (busy) return
        scope.launch {
            busy = true
            try { action() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { onError(e.localizedMessage ?: "Could not save. Please retry.") }
            finally { busy = false }
        }
    }
}

@Composable
fun rememberCloudAction(): CloudAction {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val action = remember(scope, context) { CloudAction(scope) { Toast.makeText(context, it, Toast.LENGTH_LONG).show() } }
    if (action.busy) {
        AlertDialog(onDismissRequest = {}, title = { Text("Saving…") },
            text = { LinearProgressIndicator() }, confirmButton = {})
    }
    return action
}
