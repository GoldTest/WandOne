package page.pipeline

import PAGE_END
import PAGE_START
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.SharedInstance.scope
import page.pipeline.PipeLineViewModel.hitLog
import page.pipeline.PipeLineViewModel.tempLog
import view.ColumnGap

class RecordScreen(
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val type = remember { mutableStateOf("hit") }
        Column(modifier = Modifier.padding(start = PAGE_START, end = PAGE_END)) {
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    if (navigator.canPop) {
                        navigator.pop()
                    }
                }) {
                    Text("返回")
                }
                Spacer(modifier = Modifier.width(12.dp))
                GenericRadio("hit", type, "命中日志")
                GenericRadio("record", type, "全部日志")
                GenericRadio("test", type, "测试")
            }
            when (type.value) {
                "hit" -> {
                    Record(hitLog)
                }

                "record" -> {
                    Record(tempLog)
                }

                "test" -> {
                    LazyScrollable()
                }
            }
        }
    }
}

@Composable
fun Record(items: MutableList<String>) {
    Column {
        val state = rememberLazyListState()

        Row {
            Text("处理文件数：${items.size / 2}")
        }
        ColumnGap(12.dp)
        Box {

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp)
            ) {
                this.items(items) { item ->
                    Text(item)
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(alignment = Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = state
                )
            )
        }

    }

}

@Composable
fun LazyScrollable() {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = Color(180, 180, 180))
            .padding(10.dp)
    ) {

        val state = rememberLazyListState()

        LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state) {
            items(1000) { x ->
                TextBox("Item #$x")
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = state
            )
        )
    }
}

@Composable
fun TextBox(text: String = "Item") {
    Box(
        modifier = Modifier.height(32.dp)
            .fillMaxWidth()
            .background(color = Color(0, 0, 0, 20))
            .padding(start = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = text)
    }
}