import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import model.Database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.SharedInstance
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
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
