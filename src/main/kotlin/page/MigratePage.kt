package page

import DEFAULT_DOUYIN_DEST_FOLDER
import DEFAULT_DOUYIN_SOURCE_FOLDER
import FILE_MIGRATE
import PAGE_END
import PAGE_START
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import func.commonChangeFolder
import model.FileMigrateViewModel.fileMigrationService
import model.FileMigrateViewModel.migrateState
import model.FileMigrateViewModel.serviceState


object MigratePage : Tab {
    private fun readResolve(): Any = MigratePage

    val viewModel = MigrateViewModel
    override val options: TabOptions
        @Composable
        get() {
            val title = FILE_MIGRATE
            val painter = painterResource("icons/media.svg")
//            val icon = rememberVectorPainter()

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
        MigratePage()
    }
}

@Composable
@Preview
fun MigratePage() {

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
                    migrateState.value = "手动迁移点击：$manualClick"
                    manualClick++
                },
            ) {
                Text(migrateState.value)
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (migrationStatus.isNotEmpty()) {
                Text(migrationStatus.last())
            } else {
                Text("未手动迁移过")
            }
        }

        Button(onClick = {
            serviceState.value = if (serviceState.value == "运行中，点击停止") {
                fileMigrationService.stop()
                "启动服务"
            } else {
                fileMigrationService.start()
                "运行中，点击停止"
            }
        }) {
            Text(serviceState.value)
        }
    }

}

object MigrateViewModel {


}