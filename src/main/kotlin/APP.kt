import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import constant.*
import func.*

@Preview
@Composable
fun App(viewModel: APPViewModel) {

    val migrationStatus = remember { mutableStateListOf<String>() }

    MaterialTheme {
        Column(
            modifier = Modifier.padding(start = 10.dp, top = 8.dp)
        ) {
            var manualClick by remember { mutableStateOf(0) }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.padding(end = 8.dp),
                    onClick = {
                        migrationStatus.clear()
                        commonChangeFolder(DEFAULT_DOUYIN_SOURCE_FOLDER, DEFAULT_DOUYIN_DEST_FOLDER, migrationStatus)
                        viewModel.migrateState.value = "手动迁移点击：$manualClick"
                        manualClick++
                    },
                ) {
                    Text(viewModel.migrateState.value)
                }

                if (migrationStatus.isNotEmpty()) {
                    Text(migrationStatus.last())
                } else {
                    Text("未手动迁移过")
                }
            }

            Button(onClick = {
                viewModel.serviceState.value = if (viewModel.serviceState.value == "运行中，点击停止") {
                    viewModel.fileMigrationService.stop()
                    "启动服务"
                } else {
                    viewModel.fileMigrationService.start()
                    "运行中，点击停止"
                }
            }) {
                Text(viewModel.serviceState.value)
            }

        }
    }
}



//            MainScope().launch {
//                val clipboard = LocalClipboardManager.current
//                clipboard.setText(realPath)
//                Toast("复制成功")
//            }































