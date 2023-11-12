package view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Toast(
    message: String,
    duration: Long = 2000L
) {
    val toasts = rememberSaveable { mutableStateListOf<String>() }
    val visible = rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    coroutineScope.launch {
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
                .alpha(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
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
//调用
//        val coroutineScope = rememberCoroutineScope()
//        val showToast = remember { mutableStateOf(false) }
//        Spacer(modifier = Modifier.height(SPACER_HEIGHT_12))
//        val clipboard = LocalClipboardManager.current
//        ClickableText(pathText, onClick = {
//            clipboard.setText(realPath)
//            showToast.value = true
//            coroutineScope.launch {
//                if (showToast.value) {
//                    delay(2000) // 等待 2 秒
//                    showToast.value = false
//                }
//            }
//        })
//        if (showToast.value) Toast("11111111111")
//        Snackbar {
//            Text("11111")
//        }