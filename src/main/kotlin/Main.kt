import APPViewModel.windowState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberSaveableWebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import page.tray.Tray
import view.RowGap
import java.io.File
import kotlin.math.max

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
        windowWeb()
    }
}

@Composable
fun windowWeb() {
    val windowState = rememberWindowState()
    var visible by remember { APPViewModel.webWindowVisible }
    windowState.size = DpSize(600.dp, 900.dp)
    windowState.position = WindowPosition(alignment = Alignment.CenterEnd)

    var state by mutableStateOf(rememberSaveableWebViewState("https://music.163.com/outchain/player?type=4&id=526213626&auto=0&height=430"))
    Window(
        state = windowState,
        visible = visible,
        onCloseRequest = {
            visible = false
        },
        title = if (state.pageTitle.toString() == "null") "简单浏览器" else "${state.pageTitle}"
    ) {
        var restartRequired by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(0F) }
        var initialized by remember { mutableStateOf(false) }
        val navigator = rememberWebViewNavigator()

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(builder = {
                    installDir(File("kcef-bundle"))
                    progress {
                        onDownloading {
                            downloading = max(it, 0F)
                        }
                        onInitialized {
                            initialized = true
                        }
                    }
                    settings {
                        cachePath = File("cache").absolutePath
                    }
                }, onError = {
                    it?.printStackTrace()
                }, onRestartRequired = {
                    restartRequired = true
                })
            }
        }
        if (restartRequired) {
            Text(text = "Restart required.")
        } else {
            if (initialized) {
                Column(modifier = Modifier.fillMaxSize()) {
                    var web by remember { mutableStateOf("") }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RowGap()
                        Button(onClick = {
                            if (navigator.canGoBack) {
                                navigator.navigateBack()
                            }
                        }) {
                            Text("Back")
                        }
                        RowGap()
                        OutlinedTextField(
                            value = web,
                            onValueChange = { web = it },
                            modifier = Modifier.weight(1f).height(48.dp),
                            singleLine = true
                        )
                        RowGap()
                        Button(onClick = {
                            navigator.loadUrl(web)
                        }) {
                            Text("Go")
                        }
                        RowGap()
                    }
                    Column() {
                        val loadingState = state.loadingState
                        if (loadingState is LoadingState.Loading) {
                            LinearProgressIndicator(
                                progress = loadingState.progress,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        WebView(
                            state = state,
                            modifier = Modifier.fillMaxSize(),
                            navigator = navigator,
                        )
                    }

                }
            } else {
                Text(text = "Downloading $downloading%")
            }
        }

        DisposableEffect(Unit) {
            onDispose {
//                globalScope.launch() {
//                    KCEF.dispose()
//                }
            }
        }
    }
}