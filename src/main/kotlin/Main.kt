import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = run {
    application {
        val viewModel = APPViewModel
        val windowState = rememberWindowState()
        windowState.size = DpSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT)
        windowState.position = WindowPosition(Alignment.Center)
//        Tray(this)

        val icon = painterResource(APP_ICON)
        Window(
            onCloseRequest = { viewModel.isVisible.value = false },
            visible = true,//viewModel.isVisible.value,
            icon = icon,
            title = APP_WINDOW_TITLE,
            state = windowState
        ) {

            MaterialTheme {
                App(viewModel = viewModel)
            }
        }
    }
}
