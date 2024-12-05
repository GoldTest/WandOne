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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
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
import view.ColumnGap
import view.RowGap

@Composable
fun promptPage() {
    val navigator = LocalNavigator.currentOrThrow
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    val prompts = promptService.promptFlow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var expandedPromptId by remember { mutableStateOf(0) }

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
            columns = StaggeredGridCells.Fixed(if (getPrefValue("wideMode", false)) 4 else 2), // 设置每行显示的列数
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(prompts.value.size) { index ->
                val item = prompts.value[index]
                promptItem(
                    item,
                    onEdit = {
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
    var expand by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp) // 调整内部间距以适应网格布局
            .clickable {
                promptService.setActive(!prompt.active, prompt)
                expand = !expand
            },
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prompt.workSpace,
                    style = MaterialTheme.typography.caption
                )

                Text(prompt.title ?: "", style = MaterialTheme.typography.h4)
                if (prompt.active) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "已激活",
                        color = Color.Magenta,
                        style = MaterialTheme.typography.h5
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "编辑", modifier = Modifier.clickable(onClick = onEdit),
                    style = MaterialTheme.typography.body2
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "删除", modifier = Modifier.clickable(onClick = onDelete),
                    style = MaterialTheme.typography.body2
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                maxLines = if (expand) Int.MAX_VALUE else 3,
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
        var titleText by remember { mutableStateOf(prompt?.title ?: "") }
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
                                prompt = promptText,
                                title = titleText,
                            )
                            promptService.createPrompt(prompt)
                        } else {
                            val prompt = Prompt(
                                id = prompt.id,
                                active = prompt.active,
                                title = titleText,
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

            ColumnGap()
            OutlinedTextField(
                label = { Text("Title") },
                value = titleText,
                onValueChange = { titleText = it },
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
                    onClick = { workspace = GEMINI },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                ) {
                    Text(GEMINI)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { workspace = XAI },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                ) {
                    Text(XAI)
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