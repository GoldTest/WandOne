import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import page.Tray

fun main() = run {
    application {
        val viewModel = APPViewModel
        val windowState = rememberWindowState()
        windowState.size = DpSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT)
        windowState.position = WindowPosition(Alignment.Center)
        Tray(this)
        Window(
            onCloseRequest = { viewModel.isVisible.value = false },
            visible = viewModel.isVisible.value,
            title = APP_WINDOW_TITLE,
            state = windowState
        ) {
            MaterialTheme {
                App(viewModel = viewModel)
            }
        }
    }
}
