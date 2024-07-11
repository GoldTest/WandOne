package page.pipeline

import PAGE_END
import PAGE_START
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.lifecycle.LifecycleEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import model.*
import page.pipeline.CreateNodes.currentPipeline
import page.pipeline.CreateNodes.inputNodes
import page.pipeline.CreateNodes.processNodes
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import page.pipeline.PipeLineViewModel.pipelineService
import page.pipeline.struct.Node
import page.pipeline.struct.Pipeline
import page.pipeline.struct.ProcessNode

data class PipelineScreen(
    val pipeline: Pipeline? = null
) : Screen {
    override val key = uniqueScreenKey

    var update = false

    @Composable
    override fun Content() {

        LifecycleEffect(
            onStarted = {
                if (pipeline != null) {
                    inputNodes.clear()
                    inputNodes.addAll(pipeline.inputs)
                    processNodes.clear()
                    processNodes.addAll(pipeline.nodes)
                    currentPipeline.value.id = pipeline.id
                    currentPipeline.value.name = pipeline.name
                    currentPipeline.value.runningState = pipeline.runningState
                    currentPipeline.value.singleInput = pipeline.singleInput

                    update = true
                }
            },
            onDisposed = {
                inputNodes.clear()
                processNodes.clear()
            }
        )
        val navigator = LocalNavigator.currentOrThrow
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val name = remember { mutableStateOf<String>("默认管线") }
        Column(modifier = Modifier.padding(start = PAGE_START, end = PAGE_END)) {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.weight(1f)) {
                        Button(enabled = navigator.canPop,
                            onClick = {
                                navigator.pop()
                            }) {
                            Text("取消")
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f).align(alignment = Alignment.CenterVertically)
                    ) {
                        Text(
                            text = name.value,
                            style = MaterialTheme.typography.h5
                        )
                    }

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                        Button(enabled = navigator.canPop and currentPipeline.value.savable(),
                            onClick = {
                                if (update) {
                                    pipelineService.updatePipeline(currentPipeline.value)
                                } else {
                                    pipelineService.createPipeline(currentPipeline.value)
                                }
                                navigator.pop()
                            }) {
                            Text("保存")
                        }
                    }

                }

                Row {
                    Button(
                        enabled = inputNodes.size == 0,
                        onClick = {
                            bottomSheetNavigator.show(InputNodeScreen())
                        }
                    ) {
                        Text("输入节点")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = {
                        bottomSheetNavigator.show(NodeScreen())
                    }) {
                        Text("操作节点")
                    }
                }
            }

            if (inputNodes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "输入节点",
                    style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputNodeList(inputNodes, onRemove = {
                    inputNodes.remove(it)
                })
                if (inputNodes.size > 3) {
                    SharedInstance.scope.launch {
                        ToastViewModel.snack.value.showSnackbar(
                            "已经很多了，试试再加个规则进行管理吧",
                            "知道了"
                        )
                    }
                }
            }


            if (processNodes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(if (inputNodes.isNotEmpty()) 24.dp else 12.dp))
                Text(
                    text = "操作节点 拖拽排序",
                    style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProcessNodeList(processNodes, onRemove = {
                    processNodes.remove(it)
                })
            }
        }

    }
}

@Composable
fun EasyList(itemList: MutableList<out Any>, onRemove: (Any) -> Unit) {
    if (itemList.isEmpty()) return
    LazyColumn {
        this.items(itemList) { item ->
            EasyRow(item, onRemove = {
                onRemove(item)
            })
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun EasyRow(item: Any, onRemove: () -> Unit) {
    Box {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = item.toString(), modifier = Modifier.weight(1f))
            Spacer(Modifier.width(4.dp))
            Button(
                shape = CircleShape,
                modifier = Modifier.padding(0.dp).size(24.dp),
                contentPadding = PaddingValues(0.dp),
                onClick = onRemove
            ) {
                Text("-")
            }
        }
    }
}


@Composable
fun ProcessNodeList(itemList: MutableList<ProcessNode>, onRemove: (Any) -> Unit) {
    if (itemList.isEmpty()) return
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        itemList.apply {
            val removed = removeAt(from.index)
            add(to.index, removed)
        }
    })
    LazyColumn(
        state = state.listState,
        modifier = Modifier.reorderable(state)
    ) {
        items(itemList, { it }) { item ->
            ReorderableItem(state, key = item) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                Column {
                    Column(
                        modifier = Modifier
                            .shadow(elevation.value)
                            .detectReorder(state)
                            .background(MaterialTheme.colors.surface)
                    ) {
                        ProcessNode(item, onRemove = {
                            onRemove(item)
                        })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun InputNodeList(itemList: MutableList<out Node>, onRemove: (Any) -> Unit) {
    if (itemList.isEmpty()) return
    val lazyListState = rememberLazyListState()
    LazyColumn(state = lazyListState) {
        this.items(itemList) { item ->
            ProcessNode(item, onRemove = {
                onRemove(item)
            })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ProcessNode(item: Node, onRemove: () -> Unit) {
    Card(elevation = 6.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = item.describe(), modifier = Modifier.weight(1f))
            Spacer(Modifier.width(4.dp))
            Button(
                shape = CircleShape,
                modifier = Modifier.padding(0.dp).size(24.dp),
                contentPadding = PaddingValues(0.dp),
                onClick = onRemove
            ) {
                Text("-")
            }
        }
    }
}