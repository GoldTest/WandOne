import APPViewModel.promptService
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import func.getPrefValue
import kotlinx.coroutines.launch
import page.ai.GEMINI
import page.ai.Prompt
import page.ai.TONGYI
import page.ai.XAI

@Composable
fun promptPage() {
    val navigator = LocalNavigator.currentOrThrow
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val prompts = promptService.promptFlow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { bottomSheetNavigator.show(AddPromptScreen(null)) },
            ) {
                Text("添加 Prompt")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (getPrefValue("wideMode", false)) 3 else 2), // 设置每行显示的列数
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(prompts.value.size) { index ->
                val item = prompts.value[index]
                promptItem(item, onEdit = {
                    scope.launch {
                        bottomSheetNavigator.show(AddPromptScreen(item))
                    }
                }, onDelete = {
                    promptService.removePrompt(item)
                })
            }
        }
    }
}

@Composable
fun promptItem(prompt: Prompt, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp) // 调整内部间距以适应网格布局
            .clickable { promptService.setActive(!prompt.active, prompt) },
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prompt.workSpace,
                    style = MaterialTheme.typography.body2
                )
                if (prompt.active) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "已激活",
                        color = Color.Magenta,
                        style = MaterialTheme.typography.caption
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text("编辑", modifier = Modifier.clickable(onClick = onEdit))
                Spacer(modifier = Modifier.width(8.dp))
                Text("删除", modifier = Modifier.clickable(onClick = onDelete))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = prompt.prompt,
            )
        }
    }
}

class AddPromptScreen(val prompt: Prompt?) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalBottomSheetNavigator.current

        var workspace by remember { mutableStateOf(prompt?.workSpace ?: "") }
        var promptText by remember { mutableStateOf(prompt?.prompt ?: "") }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 550.dp)
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    enabled = workspace.isNotBlank() && promptText.isNotBlank(),
                    onClick = {
                        if (prompt == null) {
                            val prompt = Prompt(
                                workSpace = workspace,
                                prompt = promptText
                            )
                            promptService.createPrompt(prompt)
                        } else {
                            val prompt = Prompt(
                                id = prompt.id,
                                active = prompt.active,
                                workSpace = workspace,
                                prompt = promptText
                            )
                            promptService.updatePrompt(prompt)
                        }

                        navigator.hide()
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                ) {
                    Text("添加", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                label = { Text("Workspace") },
                value = workspace,
                onValueChange = { workspace = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    onClick = { workspace = TONGYI },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                ) {
                    Text(TONGYI)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { workspace = XAI },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                ) {
                    Text(XAI)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { workspace = GEMINI },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                ) {
                    Text(GEMINI)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                label = { Text("Prompt") },
                value = promptText,
                onValueChange = { promptText = it },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}