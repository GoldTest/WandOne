package page.pipeline

import FabAction
import PAGE_END
import PAGE_START
import TAB_PIPELINE
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import page.pipeline.PipeLineViewModel.currentNodeDescribe
import page.pipeline.PipeLineViewModel.fabClicked
import page.pipeline.PipeLineViewModel.hitLog
import page.pipeline.PipeLineViewModel.pipelineService
import page.pipeline.PipeLineViewModel.pipelines
import page.pipeline.PipeLineViewModel.tempLog
import page.pipeline.struct.Describe
import page.pipeline.struct.Pipeline
import view.customScrollable


object PipelineTab : Tab, FabAction {
    private fun readResolve(): Any = PipelineTab
    var index: UShort = 0u
    fun PipelineTab(index: Int): PipelineTab {
        this.index = index.toUShort()
        return PipelineTab
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = TAB_PIPELINE
            val painter = painterResource("icons/media.svg")
            return remember {
                TabOptions(
                    index = index,
                    title = title,
                    icon = painter
                )
            }
        }

    @Composable
    override fun Content() {
        pipelinePage()
    }

    override fun onFabClicked() {
        fabClicked.value = !fabClicked.value
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun Tab.pipelinePage(
) {
    LifecycleEffect(
        onStarted = { },
        onDisposed = { }
    )
    BottomSheetNavigator {
        Navigator(PipelinePage())
    }
}

data class PipelinePage(
    val index: Int = 0,
) : Screen {
    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val migrationStatus = remember { mutableStateListOf<String>() }
        val pipelinesLocal by pipelines.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        Column(
            modifier = Modifier.padding(start = PAGE_START, end = PAGE_END).fillMaxHeight()
        ) {
            val isFirstComposition = remember { mutableStateOf(true) }
            LaunchedEffect(fabClicked.value) {
                if (!isFirstComposition.value) {
                    navigator.push(PipelineScreen())
                } else {
                    isFirstComposition.value = false
                }
            }
            if (pipelinesLocal.isNotEmpty()) {
                PipelineList(pipelinesLocal, onUpdate = {
                    pipelineService.updatePipeline(it)
                }) {
                    pipelineService.deletePipeline(it)
                }
            }
        }
    }
}


@Composable
fun PipelineList(pipelines: MutableList<Pipeline>, onUpdate: ((Pipeline) -> Unit)? = null, onRemove: (Int) -> Unit) {
    if (pipelines.isEmpty()) return

    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        state = scrollState,
        modifier = Modifier.customScrollable(scrollState, coroutineScope)
    ) {
        this.item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        this.items(pipelines) {
            Pipeline(it, onUpdate = {
                onUpdate?.invoke(it)
            }) {
                onRemove.invoke(it.id)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun Pipeline(pipeline: Pipeline, onUpdate: ((Pipeline) -> Unit)? = null, onRemove: () -> Unit) {
    val navigator = LocalNavigator.currentOrThrow
    Card(elevation = 8.dp) {//border = BorderStroke(width = 1.dp, Color.LightGray)
        Box {
            Row(modifier = Modifier.padding(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
//                Text(text = pipeline.name, style = MaterialTheme.typography.h6)
                    if (pipeline.inputs.isNotEmpty()) {
                        Text("输入节点", style = TextStyle(fontSize = 12.sp, color = Color.Gray))
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    pipeline.inputs.forEach { input ->
                        NodeDescribe(input)
                    }

                    if (pipeline.nodes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("操作节点", style = TextStyle(fontSize = 12.sp, color = Color.Gray))
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    pipeline.nodes.forEachIndexed { index, node ->
                        NodeDescribe(node)
                        if (index != pipeline.nodes.size - 1) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.Bottom) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            shape = CircleShape, onClick = {
                                navigator.push(PipelineScreen(pipeline))
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("编辑")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            shape = CircleShape, onClick = {
                                onRemove.invoke()
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("删除")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("服务模式", fontSize = 12.sp, color = Color.Gray)
                            Switch(
                                modifier = Modifier.height(24.dp),
                                checked = pipeline.runningState,
                                onCheckedChange = {
                                    pipeline.runningState = it
                                    onUpdate?.invoke(pipeline)
                                })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                hitLog.clear()
                                tempLog.clear()
                                pipeline.execute()
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("手动执行")
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Button(
                            onClick = {
                                navigator.push(RecordScreen())
                            }, colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("执行记录")
                        }
                    }
                    Text(currentNodeDescribe.value)
                }
            }
            Text(
                modifier = Modifier.align(alignment = Alignment.BottomEnd).padding(4.dp),
                text = pipeline.name,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun NodeDescribe(node: Describe) {
    Row(verticalAlignment = Alignment.Top) {
        Column {
            Spacer(modifier = Modifier.height(1.dp))
            Icon(
                painter = painterResource("icons/nodes/right_arrow.svg"),
                contentDescription = "Indicator",
                modifier = Modifier
                    .size(16.dp)
            )
        }

        Text(text = node.describe(), style = TextStyle(fontSize = 15.sp))
    }
}
