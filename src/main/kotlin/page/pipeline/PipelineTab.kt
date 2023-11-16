package page.pipeline

import DEFAULT_DOUYIN_DEST_FOLDER
import DEFAULT_DOUYIN_SOURCE_FOLDER
import PAGE_END
import PAGE_START
import TAB_PIPELINE
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import func.commonChangeFolder
import model.Describe
import model.FileMigrateViewModel
import model.Pipeline
import page.pipeline.PipeLineViewModel.currentNodeDescribe
import page.pipeline.PipeLineViewModel.pipelineService
import page.pipeline.PipeLineViewModel.pipelines


object PipelineTab : Tab {
    private fun readResolve(): Any = PipelineTab

    override val options: TabOptions
        @Composable
        get() {
            val title = TAB_PIPELINE
            val painter = painterResource("icons/media.svg")
            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = painter
                )
            }
        }

    @Composable
    override fun Content() {
        PipelinePage()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun Tab.PipelinePage() {

    LifecycleEffect(
        onStarted = { },
        onDisposed = { }
    )
    BottomSheetNavigator {
        Navigator(page.pipeline.PipelinePage())
    }
}

data class PipelinePage(
    val index: Int = 0
) : Screen {
    override val key = uniqueScreenKey


    @Composable
    override fun Content() {
        val migrationStatus = remember { mutableStateListOf<String>() }
        val pipelinesLocal by pipelines.collectAsState()
        Column(
            modifier = Modifier.padding(start = PAGE_START, end = PAGE_END),
        ) {
            var manualClick by remember { mutableStateOf(0) }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        migrationStatus.clear()
                        commonChangeFolder(DEFAULT_DOUYIN_SOURCE_FOLDER, DEFAULT_DOUYIN_DEST_FOLDER, migrationStatus)
                        FileMigrateViewModel.migrateState.value = "手动迁移点击：$manualClick"
                        manualClick++
                    },
                ) {
                    Text(FileMigrateViewModel.migrateState.value)
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (migrationStatus.isNotEmpty()) {
                    Text(migrationStatus.last())
                } else {
                    Text("未手动迁移过")
                }
            }

            Button(onClick = {
                FileMigrateViewModel.serviceState.value =
                    if (FileMigrateViewModel.serviceState.value == "运行中，点击停止") {
                        FileMigrateViewModel.fileMigrationService.stop()
                        "启动服务"
                    } else {
                        FileMigrateViewModel.fileMigrationService.start()
                        "运行中，点击停止"
                    }
            }) {
                Text(FileMigrateViewModel.serviceState.value)
            }
            val navigator = LocalNavigator.currentOrThrow
            Button(onClick = {
                navigator.push(PipelineScreen())
            }) {
                Text("添加规则")
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
    LazyColumn {
        this.items(pipelines) {
            Pipeline(it, onUpdate = {
                onUpdate?.invoke(it)
            }) {
                onRemove.invoke(it.id)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun Pipeline(pipeline: Pipeline, onUpdate: ((Pipeline) -> Unit)? = null, onRemove: () -> Unit) {
    val navigator = LocalNavigator.currentOrThrow
    Card(elevation = 4.dp) {
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
                        Button(shape = CircleShape, onClick = {
                            navigator.push(PipelineScreen(pipeline))
                        }) {
                            Text("编辑")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(shape = CircleShape, onClick = {
                            onRemove.invoke()
                        }) {
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
                        Button(onClick = {
                            pipeline.execute()
                        }) {
                            Text("手动执行")
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Button(onClick = {

                            navigator.push(RecordScreen(mutableListOf<String>("111","11213123")))
                        }) {
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