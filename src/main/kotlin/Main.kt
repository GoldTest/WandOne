import APPViewModel.windowState
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import page.tray.Tray
import java.io.File

fun main() = run {
    application {
        //db
        val dbDirectory = File("data")
        if (!dbDirectory.exists()) {
            dbDirectory.mkdir()
        }

        //window
        val viewModel = APPViewModel
        windowState.value.size =
            if (viewModel.wideMode.value) DpSize(APP_WINDOW_WIDTH_WMODE, APP_WINDOW_HEIGHT_WMODE) else DpSize(
                APP_WINDOW_WIDTH,
                APP_WINDOW_HEIGHT
            )
        windowState.value.position = WindowPosition(Alignment.Center)
        Tray(this)
        val icon = painterResource(APP_ICON)
        Window(
            onCloseRequest = { viewModel.isVisible.value = false },
            visible = viewModel.isVisible.value,
            icon = icon,
            title = APP_WINDOW_TITLE,
            state = windowState.value
        ) {
            MaterialTheme {
                app()
            }
        }
    }
}
