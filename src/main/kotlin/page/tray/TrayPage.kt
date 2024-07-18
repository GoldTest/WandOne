package page.tray

import APPViewModel
import APP_WINDOW_TITLE
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.rememberTrayState
import model.FileMigrateViewModel
import view.CustomIconPainter

@Composable
fun Tray(scope: ApplicationScope) {
    val fileMigrateViewModel = FileMigrateViewModel

    val activeIcon = CustomIconPainter(Color.Green)
    val deActiveIcon = CustomIconPainter(Color.Gray)
    val icon = if (fileMigrateViewModel.serviceRunningState.value) activeIcon else deActiveIcon
    val trayState = rememberTrayState()

    scope.Tray(state = trayState,
        icon = icon,
        tooltip = APP_WINDOW_TITLE,
        onAction = { APPViewModel.isVisible.value = true },
        menu = {
//            Item(
//                "migrate service",
//                enabled = !fileMigrateViewModel.serviceRunningState.value,
//                onClick = {
//                    fileMigrateViewModel.fileMigrationService.start()
//                    fileMigrateViewModel.serviceState.value = "运行中，点击停止"
//                }
//            )
//
//            Item(
//                "close service",
//                enabled = fileMigrateViewModel.serviceRunningState.value,
//                onClick = {
//                    fileMigrateViewModel.fileMigrationService.stop()
//                    fileMigrateViewModel.serviceState.value = "启动服务"
//                }
//            )

            Item(
                "exit",
                onClick = {
                    scope.exitApplication()
                }
            )
        })
}
