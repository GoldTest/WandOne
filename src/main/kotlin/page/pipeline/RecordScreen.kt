package page.pipeline

import PAGE_END
import PAGE_START
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
            }
            when (type.value) {
                "hit" -> {
                    Record(hitLog)
                }

                "record" -> {
                    Record(tempLog)
                }
            }
        }
    }
}

@Composable
fun Record(items: MutableList<String>) {
    Column {

        LazyColumn() {
            this.items(items) { item ->
                Text(item)
            }
        }
    }
}