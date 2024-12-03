package page.ai

import APPViewModel.geminiViewModel
import APPViewModel.globalScope
import APPViewModel.tongyiViewModel
import APPViewModel.xAiViewModel
import PAGE_END
import PAGE_START
import TAB_AI
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import arc.mage.wandone.wandone.generated.resources.Res
import arc.mage.wandone.wandone.generated.resources.tuneSimple
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.Role
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.compose.extendedspans.ExtendedSpans
import com.mikepenz.markdown.compose.extendedspans.RoundedCornerSpanPainter
import com.mikepenz.markdown.compose.extendedspans.SquigglyUnderlineSpanPainter
import com.mikepenz.markdown.compose.extendedspans.rememberSquigglyUnderlineAnimator
import com.mikepenz.markdown.m2.markdownColor
import com.mikepenz.markdown.m2.markdownTypography
import com.mikepenz.markdown.model.markdownExtendedSpans
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import func.getPrefValue
import func.setPrefValue
import kotlinx.coroutines.launch
import model.ToastViewModel.snack
import org.jetbrains.compose.resources.painterResource
import page.ai.page.*
import page.web3.Web3Tab
import promptPage
import view.RowGap


object AITab : Tab {
    private fun readResolve(): Any = Web3Tab
    override val options: TabOptions
        @Composable get() {
            val title = TAB_AI
            val icon = painterResource(Res.drawable.tuneSimple)
            return remember {
                TabOptions(
                    index = 5u, title = title, icon = icon
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
                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (page.value == TONGYI) MaterialTheme.colors.primary else Color.White
                ), onClick = {
                    setPrefValue("aiapi", TONGYI)
                    page.value = TONGYI
                }) {
                    Text("千问")
                }
                RowGap()
                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (page.value == GEMINI) MaterialTheme.colors.primary else Color.White
                ), onClick = {
                    setPrefValue("aiapi", GEMINI)
                    page.value = GEMINI
                }) {
                    Text(GEMINI)
                }
                RowGap()
                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (page.value == XAI) MaterialTheme.colors.primary else Color.White
                ), onClick = {
                    setPrefValue("aiapi", XAI)
                    page.value = XAI
                }) {
                    Text(XAI)
                }
                Spacer(modifier = Modifier.weight(1f))

                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (page.value == "archive") MaterialTheme.colors.primary else Color.White
                ), onClick = {
                    setPrefValue("aiapi", "archive")
                    page.value = "archive"
                }) {
                    Text("archive")
                }
                RowGap()
                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (page.value == "param") MaterialTheme.colors.primary else Color.White
                ), onClick = {
                    setPrefValue("aiapi", "param")
                    page.value = "param"
                }) {
                    Text("param")
                }
                RowGap()
                Button(colors = ButtonDefaults.buttonColors(
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


@Composable
fun messageItem(msg: Message) {
    val backgroundColor = when (msg.role) {
        Role.USER.value -> Color.LightGray
        Role.ASSISTANT.value -> Color.White
        Role.SYSTEM.value -> Color.Gray
        else -> Color.White
    }
    val clipboard = LocalClipboardManager.current
    val dropDownState = remember { DropdownMenuState() }
    DropdownMenu(dropDownState) {
        DropdownMenuItem(onClick = {
            clipboard.setText(AnnotatedString(msg.content))
            dropDownState.status = DropdownMenuState.Status.Closed
            globalScope.launch {
                snack.value.showSnackbar("复制成功", "知道了")
            }
        }) {
            Text("复制")
        }
        DropdownMenuItem(onClick = {
            //todo
        }) {
            Text("收藏")
        }
    }
    Box(modifier = Modifier.background(backgroundColor, RoundedCornerShape(8.dp))
        .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 12.dp).clickable {}.pointerInput(Unit) {
            detectTapGestures(onDoubleTap = { offset ->
                dropDownState.status = DropdownMenuState.Status.Open(offset)
            })
        }) {

        val isDarkTheme = isSystemInDarkTheme()
        val highlightsBuilder = remember(isDarkTheme) {
            Highlights.Builder().theme(SyntaxThemes.atom(darkMode = isDarkTheme))
        }
        Markdown(
            content = msg.content,
            components = markdownComponents(
                codeBlock = { MarkdownHighlightedCodeBlock(it.content, it.node, highlightsBuilder) },
                codeFence = { MarkdownHighlightedCodeFence(it.content, it.node, highlightsBuilder) },
            ),
            imageTransformer = Coil3ImageTransformerImpl,
            extendedSpans = markdownExtendedSpans {
                val animator = rememberSquigglyUnderlineAnimator()
                remember {
                    ExtendedSpans(
                        RoundedCornerSpanPainter(),
                        SquigglyUnderlineSpanPainter(animator = animator)
                    )
                }
            },
            colors = markdownColor(),
            typography = markdownTypography()
        )
    }
}

