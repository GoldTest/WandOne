package page.pipeline

import DEFAULT_DOUYIN_DEST_FOLDER
import DEFAULT_DOUYIN_SOURCE_FOLDER
import PAGE_END
import PAGE_START
import TAB_PIPELINE
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
import model.FileMigrateViewModel
import model.Pipeline
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
                navigator.push(AddPipeLineScreen())
            }) {
                Text("添加规则")
            }
            Text("ruls:${pipelines.value.size}")

            if (pipelines.value.isNotEmpty()) {
                PipelineList(pipelines.value) {

                }
            }
        }
    }
}


@Composable
fun PipelineList(pipelines: MutableList<Pipeline>, onRemove: () -> Unit) {
    if (pipelines.isEmpty()) return
    LazyColumn {
        this.items(pipelines) {
            Pipeline(it) {
                onRemove.invoke()
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun Pipeline(pipeline: Pipeline, onRemove: () -> Unit) {

    Row(modifier = Modifier.background(Color.Gray)) {

        Column(modifier = Modifier.weight(1f)) {
            Text(pipeline.name)

            Column {
                pipeline.inputs.forEach {
                    Text(it.describe())
                }
            }

            Column {
                pipeline.nodes.forEach {
                    Text(it.describe())
                }
            }

        }

        Button(
            shape = CircleShape,
            onClick = {
                onRemove.invoke()
            }
        ) {
            Text("-")
        }
    }

}