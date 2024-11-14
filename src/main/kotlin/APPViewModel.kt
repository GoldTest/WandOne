import androidx.compose.runtime.*
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import func.getPrefValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.exposed.sql.Database
import page.setting.struct.KeysService

object APPViewModel {
    var isVisible = mutableStateOf(getPrefValue("hideAfterLaunch", false).not())
    var wideMode = mutableStateOf(getPrefValue("wideMode", false))
    val database by lazy {
        Database.connect(
            url = "jdbc:sqlite:data/pipeline.db",
            user = "root",
            driver = "org.sqlite.JDBC",
            password = "admin"
        )
    }
    val keyService = KeysService(database)

    val globalScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    val windowState = mutableStateOf(WindowState())//rememberWindowState()
}