package page.pipeline

import PAGE_END
import PAGE_START
import PAGE_TOP
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import kotlinx.coroutines.launch
import model.*
import model.SharedInstance.scope
import model.ToastViewModel.snack
import page.pipeline.CreateNodes.inputNodes
import page.pipeline.CreateNodes.processNodes
import java.awt.Dimension
import javax.swing.JFileChooser
import javax.swing.SwingUtilities


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

class InputNodeScreen : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalBottomSheetNavigator.current

        val input = remember { InputMultiFolderNode() }
        val folders = remember { mutableStateListOf<String>() }
        val recurse = remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 550.dp)
                .padding(start = PAGE_START, end = PAGE_END, top = PAGE_TOP)
        ) {
            Row {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            SwingUtilities.invokeLater {
                                val fileChooser = JFileChooser()
                                fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                fileChooser.setDialogTitle("选择监测文件夹")
                                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
                                fileChooser.setAcceptAllFileFilterUsed(false)
                                fileChooser.preferredSize = Dimension(800, 500)
                                fileChooser.isVisible = true
                                val result = fileChooser.showOpenDialog(null)
                                if (result == JFileChooser.APPROVE_OPTION) {
                                    val path = fileChooser.selectedFile.toPath().toString()
                                    if (folders.contains(path)) {
                                        scope.launch { snack.value.showSnackbar("文件夹重复了", "知道了") }
                                    } else {
                                        folders.add(path)
                                    }
                                }
                            }
                        },
                    ) {
                        Text("文件夹源")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("递归子文件夹")
                    Checkbox(checked = recurse.value, onCheckedChange = {
                        recurse.value = it
                    })
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    enabled = folders.isNotEmpty(),
                    onClick = {
                        input.sourceFolderList.clear()
                        input.sourceFolderList.addAll(folders)
                        input.recurse = recurse.value
                        inputNodes.add(input)
                        navigator.hide()
                    }
                ) {
                    Text("保存")
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            EasyList(folders, onRemove = {
                folders.remove(it)
            })
            if (folders.size > 3) {
                scope.launch { snack.value.showSnackbar("已经够多源文件夹了，试试再创建个管线吧") }
            }
        }
    }

}

@Composable
fun MatchNodeScreen(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {

    val type = remember { mutableStateOf("none") }
    val nameMatchMode = remember { mutableStateOf(NameMatchMode.None) }
    val nameMatchSubMode = remember { mutableStateOf(NameMatchSubMode.None) }
    val fileType = remember { mutableStateOf("none") }
    val text = remember { mutableStateOf("") }
    val containDirectory = remember { mutableStateOf(false) }
    val forceSubString = remember { mutableStateOf(false) }


    fun updateCurrentNode() {
        currentNode.value = null
        when (type.value) {
            "all" -> {
                currentNode.value = MatchNameNode(
                    mode = NameMatchMode.AllMode,
                    containDirectory = containDirectory.value
                )
            }

            "name" -> {
                when (nameMatchMode.value) {
                    NameMatchMode.None, NameMatchMode.AllMode -> {}
                    NameMatchMode.EasyMode -> {
                        when (nameMatchSubMode.value) {
                            NameMatchSubMode.None -> {}
                            NameMatchSubMode.Prefix -> {
                                if (text.value.isNotBlank()) {
                                    currentNode.value = MatchNameNode(
                                        matchString = text.value,
                                        mode = NameMatchMode.EasyMode,
                                        subMode = NameMatchSubMode.Prefix,
                                        containDirectory = containDirectory.value,
                                        forceSubString = forceSubString.value
                                    )
                                }
                            }

                            NameMatchSubMode.Contain -> {
                                if (text.value.isNotBlank()) {
                                    currentNode.value = MatchNameNode(
                                        matchString = text.value,
                                        mode = NameMatchMode.EasyMode,
                                        subMode = NameMatchSubMode.Contain,
                                        containDirectory = containDirectory.value,
                                        forceSubString = forceSubString.value
                                    )
                                }
                            }

                            NameMatchSubMode.Suffix -> {
                                if (text.value.isNotBlank()) {
                                    currentNode.value = MatchNameNode(
                                        matchString = text.value,
                                        mode = NameMatchMode.EasyMode,
                                        subMode = NameMatchSubMode.Suffix,
                                        containDirectory = containDirectory.value,
                                        forceSubString = forceSubString.value
                                    )
                                }
                            }
                        }
                    }

                    NameMatchMode.RegexMode -> {
                        if (text.value.isNotBlank()) {
                            currentNode.value = MatchNameNode(
                                matchRegex = text.value,
                                containDirectory = containDirectory.value
                            )
                        }
                    }
                }
            }

            "type" -> {
                when (fileType.value) {
                    "all" -> {
                        currentNode.value = MatchTypeNode(
                            mode = FileType.All
                        )
                    }
                }
            }

        }
        end.value = currentNode.value != null
    }

    LaunchedEffect(
        type.value,
        nameMatchMode.value,
        containDirectory.value,
        forceSubString.value,
        fileType.value,
        nameMatchSubMode.value,
        text.value
    ) {
        updateCurrentNode()
    }

    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 450.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GenericRadio("all", type, "全部")
            GenericRadio("name", type, "文件名")
            GenericRadio("type", type, "文件类型")
            Text("包含文件夹")
            Checkbox(
                checked = containDirectory.value,
                onCheckedChange = { isChecked ->
                    containDirectory.value = isChecked
                }
            )
        }
        when (type.value) {
            "all" -> {}
            "name" -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GenericRadio(NameMatchMode.EasyMode, nameMatchMode, "简单文件名匹配")
                    GenericRadio(NameMatchMode.RegexMode, nameMatchMode, "文件名正则匹配")
                }
            }

            "type" -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GenericRadio("all", fileType, "全部类型")
                }
            }
        }
        if (type.value == "name") when (nameMatchMode.value) {
            NameMatchMode.None, NameMatchMode.AllMode -> {}
            NameMatchMode.EasyMode -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GenericRadio(NameMatchSubMode.Contain, nameMatchSubMode, "包含")
                    GenericRadio(NameMatchSubMode.Prefix, nameMatchSubMode, "前缀")
                    GenericRadio(NameMatchSubMode.Suffix, nameMatchSubMode, "后缀")
                }
                if (nameMatchSubMode.value == NameMatchSubMode.Suffix || nameMatchSubMode.value == NameMatchSubMode.Prefix) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (nameMatchSubMode.value == NameMatchSubMode.Prefix) "是否强制需要前半部分" else "是否强制需要后半部分")
                        Checkbox(
                            checked = forceSubString.value,
                            onCheckedChange = { isChecked ->
                                forceSubString.value = isChecked
                            })
                    }
                }
                TextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    label = { Text(if (nameMatchMode.value == NameMatchMode.EasyMode) "输入简单匹配文字" else "输入正则公式") }
                )
            }

            NameMatchMode.RegexMode -> {
                TextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    label = { Text(if (nameMatchMode.value == NameMatchMode.EasyMode) "输入简单匹配文字" else "输入正则公式") }
                )
            }
        }
    }
}

@Composable
fun OperateNodeScreen(result: ((MatchNode) -> Unit)? = null) {
    val operateChooseState = remember { mutableStateOf(0) }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GenericRadio(1, operateChooseState, "重命名")
            GenericRadio(2, operateChooseState, "媒体合并")
            GenericRadio(3, operateChooseState, "移动")
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
fun <T> GenericRadio(
    option: T,
    selectedOption: MutableState<T>,
    label: String,
    onOptionSelected: ((T) -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        RadioButton(
            modifier = Modifier.padding(0.dp),
            selected = (selectedOption.value == option),
            onClick = {
                selectedOption.value = option
                onOptionSelected?.invoke(option)
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


//                println("contain ${containDirectory.value}")
//                val localNode = MatchNameNode(
//                    mode = NameMatchMode.ALL_MODE,
//                    containDirectory = containDirectory.value
//                )
//                currentNode.value = localNode