package page.media

import APPViewModel.webViewModel
import PAGE_END
import PAGE_START
import TAB_MEDIA_MERGE
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import func.getPrefValue
import func.setPrefValue
import view.RowGap


object MediaProcessTab : Tab {
    private fun readResolve(): Any = MediaProcessTab
    var index: UShort = 0u
    fun MediaProcessTab(index: Int): MediaProcessTab {
        this.index = index.toUShort()
        return MediaProcessTab
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = TAB_MEDIA_MERGE
            val icon = painterResource("icons/mergeTwo.svg")

            return remember {
                TabOptions(
                    index = index,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        mediaPage()
    }
}

@Composable
@Preview
fun mediaPage() {
    val viewModel = webViewModel


    Column(
        modifier = Modifier.padding(start = PAGE_START, end = PAGE_END)
    ) {

        val pages = listOf(
            0 to "网页",
            1 to "音视频合并",
            2 to "音视频处理",
            3 to "音视频剪切",
        )
        var page by remember { mutableStateOf(getPrefValue("mediaPage", 0)) }
        var webUrl by remember { viewModel.url }
        val webUrlInput = remember { mutableStateOf("") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            pages.forEachIndexed { index, (pageId, text) ->
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (page == pageId) MaterialTheme.colors.primary else Color.White
                    ),
                    onClick = {
                        page = pageId
                        setPrefValue("mediaPage", pageId)
                    }
                ) {
                    Text(text)
                }
                RowGap()
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = webUrlInput.value,
                    onValueChange = { it ->
                        webUrlInput.value = it
                    })
                RowGap()
                Button(onClick = {
                    //back
                }) {
                    Text("back")
                }
                RowGap()
                Button(onClick = {
                    webUrl = webUrlInput.value
                }) {
                    Text("go")
                }
            }
        }

        when (page) {
            0 -> webview()
        }
    }
}

@Composable
fun webview() {
}