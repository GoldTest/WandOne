import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.WindowState
import func.getPrefValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.exposed.sql.Database
import page.ai.AiViewModel
import page.ai.PromptService
import page.setting.struct.KeysService
import page.web3.Web3ViewModel

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

    val tongyiViewModel by lazy { AiViewModel() }
    val xAiViewModel by lazy { AiViewModel() }
    val geminiViewModel by lazy { AiViewModel() }
    val promptService by lazy { PromptService(database) }

    val web3ViewModel by lazy { Web3ViewModel() }

    val webViewModel by lazy { WebViewModel() }
}