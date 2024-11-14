package page.web3

import APPViewModel.globalScope
import PAGE_END
import PAGE_START
import TAB_AI
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.Role
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m2.markdownColor
import com.mikepenz.markdown.m2.markdownTypography
//import com.mikepenz.markdown.m2.markdownColor
//import com.mikepenz.markdown.m2.markdownTypography
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import func.getPrefValue
import func.setPrefValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import page.ai.tongyi.streamMessage
import view.RowGap


object AITab : Tab {
    private fun readResolve(): Any = Web3Tab
    override val options: TabOptions
        @Composable get() {
            val title = TAB_AI
            val icon = painterResource("icons/tuneSimple.svg")
            return remember {
                TabOptions(
                    index = 2u, title = title, icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        aiPage()
    }
}

@Composable
@Preview
fun aiPage() {
    val page = remember { mutableStateOf(getPrefValue("aiapi", "")) }

    Column(
        modifier = Modifier.padding(start = PAGE_START, end = PAGE_END),
    ) {
        Row {
            Button(onClick = {
                setPrefValue("aiapi", "tongyi")
                page.value = "tongyi"
            }) {
                Text("通义")
            }
            RowGap()
            Button(onClick = {
                setPrefValue("aiapi", "claude")
                page.value = "claude"
            }) {
                Text("Claude")
            }
            Spacer(modifier = Modifier.weight(1f))

            Button(onClick = {
                //todo
            }) {
                Text("prompt")
            }
        }

        when (page.value) {
            "tongyi" -> tongyiPage()
            "claude" -> claudePage()
        }
    }
}


@Composable
fun tongyiPage() {

    // 当前对话维护的message列表
    val messageList = remember { mutableStateListOf<Message>() }
    // 当前输入内容
    var input by remember { mutableStateOf("") }
    // 当前流式输出的内容
    val lastMsg = remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = scrollState,
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(messageList) { msg ->
                MessageItem(msg)
            }
            item {
                if (lastMsg.value.isNotBlank())
                    Markdown(
                        modifier = Modifier.padding(8.dp),
                        content = lastMsg.value,
                        colors = markdownColor(),
                        typography = markdownTypography()
                    )
            }
        }
        LaunchedEffect(lastMsg.value) {
            scrollState.animateScrollToItem(messageList.size + 1)
        }
        
        LaunchedEffect(messageList.size) {
            scrollState.animateScrollToItem(messageList.size + 1)
        }


        val stop = remember { MutableStateFlow(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = { newValue ->
                    input = newValue
                },
                label = { Text("输入内容") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (input.isNotBlank()) {
                        val currentInput = Message.builder().role(Role.USER.value).content(input).build()
                        input = ""
                        messageList.add(currentInput)
                        globalScope.launch {
                            stop.value = false
                            streamMessage(messageList)
                                ?.asFlow()
                                ?.takeWhile { stop.value.not() }
                                ?.flowOn(Dispatchers.IO)
                                ?.collect { msg ->
                                    lastMsg.value += msg.output.choices[0].message.content
                                }
                            val currentOutput =
                                Message.builder().role(Role.ASSISTANT.value).content(lastMsg.value).build()
                            messageList.add(currentOutput)
                            lastMsg.value = ""
                        }
                    }
                }),
                modifier = Modifier.weight(1f)
            )
            RowGap(8.dp)
            Button(
                onClick = {
                    // 停止按钮的逻辑
                    stop.value = true
                },
                modifier = Modifier.clip(RoundedCornerShape(24.dp))
            ) {
                Text("停止")
            }
            RowGap(8.dp)
            Button(
                onClick = {
                    // 导出当前聊天 todo
                },
                modifier = Modifier.clip(RoundedCornerShape(24.dp))
            ) {
                Text("导出")
            }
        }
    }
}

@Composable
fun MessageItem(msg: Message) {
    val backgroundColor = when (msg.role) {
        Role.USER.value -> Color.LightGray
        Role.ASSISTANT.value -> Color.White
        Role.SYSTEM.value -> Color.Gray
        else -> Color.White
    }
    val textColor = when (msg.role) {
        Role.USER.value -> Color.Black
        Role.ASSISTANT.value -> Color.Black
        Role.SYSTEM.value -> Color.White
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 12.dp)
    ) {
        Markdown(
            content = msg.content,
            colors = markdownColor(),
            typography = markdownTypography()
        )
//        Text(
//            text = msg.content,
//            color = textColor,
//            style = MaterialTheme.typography.body1
//        )
    }
}

@Composable
fun claudePage() {

}