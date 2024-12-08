package page.ai

import APPViewModel.geminiViewModel
import APPViewModel.tongyiViewModel
import APPViewModel.xAiViewModel
import PAGE_END
import PAGE_START
import TAB_AI
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import arc.mage.wandone.wandone.generated.resources.Res
import arc.mage.wandone.wandone.generated.resources.tuneSimple
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import func.getPrefValue
import func.setPrefValue
import org.jetbrains.compose.resources.painterResource
import page.ai.page.*
import page.web3.Web3Tab
import promptPage
import view.RowGap


object AITab : Tab {
    var index: UShort = 0u
    fun AITab(index: Int): AITab {
        this.index = index.toUShort()
        return AITab
    }

    private fun readResolve(): Any = Web3Tab
    override val options: TabOptions
        @Composable get() {
            val title = TAB_AI
            val icon = painterResource(Res.drawable.tuneSimple)
            return remember {
                TabOptions(
                    index = index, title = title, icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        aiPage()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun Tab.aiPage(
) {
    LifecycleEffect(
        onStarted = { },
        onDisposed = { }
    )
    BottomSheetNavigator {
        Navigator(AiPage())
    }
}


class AiPage() : Screen {

    @Composable
    override fun Content() {
        val page = remember { mutableStateOf(getPrefValue("aiapi", "")) }

        Column(
            modifier = Modifier.padding(start = PAGE_START, end = PAGE_END),
        ) {
            Row {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (page.value == TONGYI) MaterialTheme.colors.primary else Color.White
                    ), onClick = {
                        setPrefValue("aiapi", TONGYI)
                        page.value = TONGYI
                    }) {
                    Text("千问")
                }
                RowGap()
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (page.value == GEMINI) MaterialTheme.colors.primary else Color.White
                    ), onClick = {
                        setPrefValue("aiapi", GEMINI)
                        page.value = GEMINI
                    }) {
                    Text(GEMINI)
                }
                RowGap()
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (page.value == XAI) MaterialTheme.colors.primary else Color.White
                    ), onClick = {
                        setPrefValue("aiapi", XAI)
                        page.value = XAI
                    }) {
                    Text(XAI)
                }
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (page.value == "archive") MaterialTheme.colors.primary else Color.White
                    ), onClick = {
                        setPrefValue("aiapi", "archive")
                        page.value = "archive"
                    }) {
                    Text("archive")
                }
                RowGap()
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (page.value == "param") MaterialTheme.colors.primary else Color.White
                    ), onClick = {
                        setPrefValue("aiapi", "param")
                        page.value = "param"
                    }) {
                    Text("param")
                }
                RowGap()
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (page.value == "prompt") MaterialTheme.colors.primary else Color.White
                    ), onClick = {
                        page.value = "prompt"
                        setPrefValue("aiapi", "prompt")
                    }) {
                    Text("prompt")
                }
            }
            when (page.value) {
                TONGYI -> tongyiPage(tongyiViewModel)
                GEMINI -> geminiPage(geminiViewModel)
                XAI -> xaiPage(xAiViewModel)
                "archive" -> archivePage()
                "param" -> paramPage()
                "prompt" -> promptPage()
            }
        }
    }
}


