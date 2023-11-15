package page.pipeline

import PAGE_END
import PAGE_START
import PAGE_TOP
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import model.SharedInstance.scope
import model.ToastViewModel.snack
import page.pipeline.CreateNodes.inputNode
import page.pipeline.CreateNodes.processNodes
import pipeline.*
import javax.swing.JFileChooser
import javax.swing.SwingUtilities


class InputNodeScreen : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow

        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 550.dp)
                .padding(start = PAGE_START, end = PAGE_END, top = PAGE_TOP)
        ) {
            Row {
                Button(
                    onClick = {
                        SwingUtilities.invokeLater {
                            val fileChooser = JFileChooser()
                            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                            fileChooser.setDialogTitle("选择监测文件夹")
                            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
                            fileChooser.setAcceptAllFileFilterUsed(false)
                            fileChooser.isVisible = true
                            val result = fileChooser.showOpenDialog(null)
                            if (result == JFileChooser.APPROVE_OPTION) {
                                inputNode.value.sourceFolderList.add(fileChooser.selectedFile.toPath().toString())
                            }
                        }
                    },
                ) {
                    Text("文件夹源")
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.width(12.dp))
            EasyList(inputNode.value.sourceFolderList, onRemove = {
                inputNode.value.sourceFolderList.remove(it)
            })
            if (inputNode.value.sourceFolderList.size > 3) {
                scope.launch { snack.value.showSnackbar("已经够多源文件夹了，试试再创建个管线吧") }
            }
        }
    }

}

class NodeScreen : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalBottomSheetNavigator.current

        val type = remember { mutableStateOf(0) }
        val end = remember { mutableStateOf(false) }
        val currentNode = remember { mutableStateOf<ProcessNode?>(null) }

        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 550.dp)
                .padding(start = PAGE_START, end = PAGE_END, top = PAGE_TOP)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("节点类型", style = TextStyle(fontSize = 12.sp))
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (type.value == 1) MaterialTheme.colors.primary else Color.White
                    ),
                    onClick = {
                        type.value = 1
                    }) {
                    Text("匹配")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (type.value == 2) MaterialTheme.colors.primary else Color.White
                ), onClick = {
                    type.value = 2
                }) {
                    Text("操作")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(enabled = end.value and (currentNode.value != null),
                    onClick = {
                        currentNode.value?.let { processNodes.add(it) }
                        navigator.hide()
                    }) {
                    Text("保存")
                }
            }
            when (type.value) {
                0 -> {}
                1 -> MatchNodeScreen(end, currentNode)

                2 -> OperateNodeScreen {
//                    currentNode.value = it
                }
            }
        }
    }
}

@Composable
fun MatchNodeScreen(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {

    val rule0 = remember { mutableStateOf(0) }
    val rule1 = remember { mutableStateOf(0) }
    val rule2 = remember { mutableStateOf(0) }
    val text = remember { mutableStateOf("") }
    val containDirectory = remember { mutableStateOf(false) }


    fun clearState() {
        currentNode.value = null
        end.value = false
    }

    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 450.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LabelWithRadio("全部", 3, rule0) {
                val localNode = MatchNameNode(mode = NameMatchMode.ALL_MODE, containDirectory = containDirectory.value)
                currentNode.value = localNode
                end.value = true
            }
            LabelWithRadio("文件名", 1, rule0, ::clearState)
            LabelWithRadio("文件类型", 2, rule0, ::clearState)
            Text("包含文件夹")
            Checkbox(
                checked = containDirectory.value,
                onCheckedChange = { isChecked ->
                    containDirectory.value = isChecked
                }
            )
        }
        when (rule0.value) {
            1 ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelWithRadio("简单文件名匹配", 1, rule1, ::clearState)
                    LabelWithRadio("文件名正则匹配", 2, rule1, ::clearState)
                }

            2 ->
                Row {
                    Text("JPG")
                    Checkbox(checked = false, onCheckedChange = {})
                    Text("PNG")
                    Checkbox(checked = false, onCheckedChange = {})
                    Text("JPEG")
                    Checkbox(checked = false, onCheckedChange = {})
                }
        }
        if ((rule0.value == 1)) when (rule1.value) {
            1 -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LabelWithRadio("包含", 1, rule2)
                    LabelWithRadio("前缀", 2, rule2)
                    LabelWithRadio("中缀", 3, rule2)
                    LabelWithRadio("后缀", 4, rule2)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (rule2.value) {
                        2 -> Text("是否强制需要前半部分")
                        3 -> Text("是否强制需要前后部分")
                        4 -> Text("是否强制需要后半部分")
                    }
                    when (rule2.value) {
                        2, 3, 4 -> Checkbox(checked = false, onCheckedChange = {

                        })
                    }

                }
                TextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    label = { Text(if (rule1.value == 1) "输入简单匹配文字" else "输入正则公式") }
                )
            }

            2 ->
                TextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    label = { Text(if (rule1.value == 1) "输入简单匹配文字" else "输入正则公式") }
                )
        }

    }
}

@Composable
fun OperateNodeScreen(result: ((MatchNode) -> Unit)? = null) {
    val operateChooseState = remember { mutableStateOf(0) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LabelWithRadio("重命名", 1, operateChooseState)
            LabelWithRadio("媒体合并", 2, operateChooseState)
            LabelWithRadio("移动", 3, operateChooseState)
        }
        when (operateChooseState.value) {
            1 -> {
                Text("重命名")
            }

            2 -> {
                Text("媒体合并")

            }

            3 -> {
                Text("移动")

            }
        }
    }

}

@Composable
fun LabelWithRadio(
    text: String,
    value: Int,
    state: MutableState<Int>,
    onBeforeChange: (() -> Unit)? = null,
    result: ((Int) -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text)
        RadioButton(
            modifier = Modifier.padding(0.dp),
            selected = state.value == value,
            onClick = {
                onBeforeChange?.invoke()
                state.value = value
                result?.invoke(state.value)
            }
        )
    }
}

//        SwingUtilities.invokeLater {
//            val fileChooser = JFileChooser()
//            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
//            fileChooser.setDialogTitle("选择目标文件夹");
//            fileChooser.setAcceptAllFileFilterUsed(false);
//            fileChooser.isVisible = true
//            val result = fileChooser.showOpenDialog(null)
//            if (result == JFileChooser.APPROVE_OPTION) {
////                            destNode.destFolderList.add(fileChooser.selectedFile.toPath().toString())
//            }
//        }
