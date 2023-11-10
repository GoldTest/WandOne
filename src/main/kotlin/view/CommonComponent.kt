package view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Toast(
    message: String,
    duration: Long = 2000L
) {
    val toasts = rememberSaveable { mutableStateListOf<String>() }
    val visible = rememberSaveable { mutableStateOf(false) }

    CoroutineScope(Dispatchers.Main).launch {
        toasts.add(message)
        visible.value = true
        delay(duration)
        toasts.remove(message)
        visible.value = false
    }

    if (visible.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.9f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                color = Color.White,
                modifier = Modifier
                    .background(Color.Black)
                    .padding(16.dp)
            )
        }
    }
}