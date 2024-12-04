import APPViewModel.windowState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBuilder.Download
import dev.datlag.kcef.KCEFBuilder.Download.Builder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import page.tray.Tray
import java.io.File
import kotlin.math.max

fun main1() = run {
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

fun main() = run {
    application {
        Window(onCloseRequest = ::exitApplication) {
            var restartRequired by remember { mutableStateOf(false) }
            var downloading by remember { mutableStateOf(0F) }
            var initialized by remember { mutableStateOf(false) }
            val download: Download = remember { Builder().github().build() }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    KCEF.init(builder = {
                        installDir(File("kcef-bundle"))
                        /*
                          Add this code when using JDK 17.
                          Builder().github {
                              release("jbr-release-17.0.10b1087.23")
                          }.buffer(download.bufferSize).build()
                         */
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
                        val state = rememberWebViewState("https://baidu.com")

                        Text(text = "${state.pageTitle}")
                        val loadingState = state.loadingState
                        if (loadingState is LoadingState.Loading) {
                            LinearProgressIndicator(
                                progress = loadingState.progress,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        WebView(
                            state, modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Text(text = "Downloading $downloading%")
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    KCEF.disposeBlocking()
                }
            }
        }
    }
}