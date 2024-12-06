package page.ai.page

import APPViewModel.globalScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch
import model.ToastViewModel.snack
import page.ai.AiViewModel
import view.RowGap

@Composable
fun tongyiPage(viewModel: AiViewModel) {

    // 当前对话维护的message列表
    val messageList = viewModel.messageList
    // 当前输入内容
    var input by remember { mutableStateOf("") }
    // 当前流式输出的内容
    val lastMsg = remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = scrollState,
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(messageList) { msg ->
                SelectionContainer() {
                    messageItem(msg, clipboard)
                }
            }
            item {
                if (lastMsg.value.isNotBlank()) {
                    val isDarkTheme = isSystemInDarkTheme()
                    val highlightsBuilder = remember(isDarkTheme) {
                        Highlights.Builder().theme(SyntaxThemes.atom(darkMode = isDarkTheme))
                    }
                    Markdown(
                        content = lastMsg.value,
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
                        modifier = Modifier.padding(8.dp),
                        colors = markdownColor(),
                        typography = markdownTypography()
                    )
                }
            }
        }
        LaunchedEffect(lastMsg.value) {
            scrollState.animateScrollToItem(messageList.size + 1)
        }

        LaunchedEffect(messageList.size) {
            scrollState.animateScrollToItem(messageList.size + 1)
        }

        val stop = remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input, onValueChange = { newValue ->
                    input = newValue
                }, label = { Text("输入内容") }, maxLines = 5, keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ), keyboardActions = KeyboardActions(onDone = {
                    //no use because max lines
                    viewModel.handleInput(input, lastMsg, stop)
                    input = ""
                }), modifier = Modifier.weight(1f).onPreviewKeyEvent { event ->
                    if (event.key == Key.Enter) {
                        viewModel.handleInput(input, lastMsg, stop)
                        input = ""
                        true
                    } else {
                        false
                    }
                })
            RowGap(8.dp)
            Button(
                onClick = {
                    // 停止按钮的逻辑
                    stop.value = true
                }, modifier = Modifier.clip(RoundedCornerShape(24.dp))
            ) {
                Text("停止")
            }
            RowGap(8.dp)
            Button(
                onClick = {
                    viewModel.clear()
                    lastMsg.value = ""
                }, modifier = Modifier.clip(RoundedCornerShape(24.dp))
            ) {
                Text("清理")
            }
        }
    }
}

@Composable
fun messageItem(msg: Message, clipboard: ClipboardManager) {
    val backgroundColor = when (msg.role) {
        Role.USER.value -> Color.LightGray
        Role.ASSISTANT.value -> Color.White
        Role.SYSTEM.value -> Color.Gray
        else -> Color.White
    }

    // 使用 Boolean 状态来控制菜单的显示
    var expanded by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Box() {
        Box(
            modifier = Modifier.background(backgroundColor, RoundedCornerShape(8.dp))
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 12.dp)
                .clickable {}
                .pointerInput(Unit) {
                    detectTapGestures(onDoubleTap = { tapOffset ->
                        expanded = true
                        offset = tapOffset // 更新双击位置
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
        // Dropdown menu is now inside the outer Box and uses the Boolean state
        DropdownMenu(
            offset = DpOffset(offset.x.dp, offset.y.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false }) {
            DropdownMenuItem(onClick = {
                clipboard.setText(AnnotatedString(msg.content))
                expanded = false
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
    }

}
