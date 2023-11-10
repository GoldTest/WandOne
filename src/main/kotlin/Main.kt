import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import constant.APP_WINDOW_HEIGHT
import constant.APP_WINDOW_TITLE
import constant.APP_WINDOW_WIDTH
import view.CustomIconPainter

fun main() = application {
    var isVisible by remember { mutableStateOf(false) }
    val viewModel = APPViewModel()

    val trayState = rememberTrayState()

    val activeIcon = CustomIconPainter(Color.Green)
    val deActiveIcon = CustomIconPainter(Color.Gray)


    val windowState = rememberWindowState()
    windowState.size = DpSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT)
    windowState.position = WindowPosition(Alignment.Center)

    Window(
        onCloseRequest = { isVisible = false },
        visible = isVisible,
        title = APP_WINDOW_TITLE,
        state = windowState
    ) {

        val icon = if (viewModel.serviceRunningState.value) {
            activeIcon
        } else {
            deActiveIcon
        }

        Tray(
            state = trayState,
            icon = icon,
            tooltip = "WandOne",
            onAction = {
                isVisible = true
            },
            menu = {
                Item(
                    "start BaseService.kt",
                    enabled = !viewModel.serviceRunningState.value,
                    onClick = {
                        viewModel.fileMigrationService.start()
                        viewModel.serviceState.value = "运行中，点击停止"
                    }
                )

                Item(
                    "close service",
                    enabled = viewModel.serviceRunningState.value,
                    onClick = {
                        viewModel.fileMigrationService.stop()
                        viewModel.serviceState.value = "启动服务"
                    }
                )

                Item(
                    "exit",
                    onClick = {
                        exitApplication()
                    }
                )
            }
        )

        App(viewModel = viewModel)
    }
}