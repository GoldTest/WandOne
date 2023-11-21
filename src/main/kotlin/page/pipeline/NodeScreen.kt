package page.pipeline

import PAGE_END
import PAGE_START
import PAGE_TOP
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
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
import kotlin.Exception

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


class NodeScreen : Screen {
    @Composable
    override fun Content() {

        val navigator = LocalBottomSheetNavigator.current

        val type = remember { mutableStateOf("none") }
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
                        backgroundColor = if (type.value == "filter") MaterialTheme.colors.primary else Color.White
                    ),
                    onClick = {
                        type.value = "filter"
                    }) {
                    Text("过滤")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (type.value == "match") MaterialTheme.colors.primary else Color.White
                    ),
                    onClick = {
                        type.value = "match"
                    }) {
                    Text("匹配")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (type.value == "multiMatch") MaterialTheme.colors.primary else Color.White
                ), onClick = {
                    type.value = "multiMatch"
                }) {
                    Text("多匹配")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (type.value == "operate") MaterialTheme.colors.primary else Color.White
                ), onClick = {
                    type.value = "operate"
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
                "filter" -> FilterNodeScreen(end, currentNode)
                "match" -> MatchNodeScreen(end, currentNode)
                "multiMatch" -> MultiMatchNodeScreen(end, currentNode)
                "operate" -> OperateNodeScreen(end, currentNode)
            }
        }
    }
}

@Composable
fun FilterNodeScreen(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {

    val type = remember { mutableStateOf("none") }
    val filterDirectory = remember { mutableStateOf(true) }
    val filterHiddenFile = remember { mutableStateOf(true) }

    fun updateCurrentNode() {
        currentNode.value = null
        when (type.value) {
            "none" -> {
                currentNode.value = FilterNode(
                    filterDirectory = filterDirectory.value,
                    filterHiddenFile = filterHiddenFile.value
                )
            }
        }
        end.value = currentNode.value != null
    }

    LaunchedEffect(
        type.value,
        filterDirectory.value,
        filterHiddenFile.value,
    ) {
        updateCurrentNode()
    }

    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 450.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("过滤文件夹")
            Checkbox(
                checked = filterDirectory.value,
                onCheckedChange = { isChecked ->
                    filterDirectory.value = isChecked
                }
            )
            Text("过滤隐藏文件")
            Checkbox(
                checked = filterHiddenFile.value,
                onCheckedChange = { isChecked ->
                    filterHiddenFile.value = isChecked
                }
            )
        }
    }
}

@Composable
fun MatchNodeScreen(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {

    val type = remember { mutableStateOf("none") }
    fun updateCurrentNode() {
        currentNode.value = null
        when (type.value) {
            "all" -> {
                currentNode.value = MatchNameNode(
                    mode = NameMatchMode.AllMode
                )
            }
        }
        end.value = currentNode.value != null
    }

    LaunchedEffect(
        type.value,
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
        }
        when (type.value) {
            "all" -> {}
            "name" -> MatchName(end, currentNode)
            "type" -> MatchType(end, currentNode)
        }
    }
}


@Composable
fun MatchName(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {
    val nameMatchMode = remember { mutableStateOf(NameMatchMode.None) }
    val nameMatchSubMode = remember { mutableStateOf(NameMatchSubMode.None) }
    val text = remember { mutableStateOf("") }

    val filterToFileName = remember { mutableStateOf(true) }
    val forceSubString = remember { mutableStateOf(false) }
    val caseSensitive = remember { mutableStateOf(false) }
    val regexType = remember { mutableStateOf(RegexType.None) }


    fun updateCurrentNode() {
        currentNode.value = null
        when (nameMatchMode.value) {
            NameMatchMode.EasyMode -> {
                if (text.value.isNotBlank()) {
                    currentNode.value = MatchNameNode(
                        matchString = text.value,
                        mode = NameMatchMode.EasyMode,
                        subMode = nameMatchSubMode.value,
                        forceSubString = forceSubString.value,
                        filterPurePath = filterToFileName.value,
                        caseSensitive = caseSensitive.value
                    )
                }
            }

            NameMatchMode.RegexMode -> {
                if (text.value.isNotBlank()) {
                    currentNode.value = MatchNameNode(
                        matchRegex = text.value,
                        mode = NameMatchMode.RegexMode,
                        filterPurePath = filterToFileName.value
                    )
                }
            }
            // 对于 NameMatchMode.None 和 NameMatchMode.AllMode 以及未处理的情况，不执行任何操作
            NameMatchMode.None, NameMatchMode.AllMode -> {}
        }
        end.value = currentNode != null
    }
    LaunchedEffect(
        nameMatchMode.value,
        forceSubString.value,
        nameMatchSubMode.value,
        filterToFileName.value,
        caseSensitive.value,
        regexType.value,
        text.value
    ) {
        updateCurrentNode()
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        GenericRadio(NameMatchMode.EasyMode, nameMatchMode, "简单文件名匹配")
        GenericRadio(NameMatchMode.RegexMode, nameMatchMode, "文件名正则匹配")
        Text("过滤路径")
        Checkbox(
            checked = filterToFileName.value,
            onCheckedChange = { isChecked ->
                filterToFileName.value = isChecked
            }
        )
    }
    when (nameMatchMode.value) {
        NameMatchMode.None, NameMatchMode.AllMode -> {}
        NameMatchMode.EasyMode -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GenericRadio(NameMatchSubMode.Contain, nameMatchSubMode, "包含")
                GenericRadio(NameMatchSubMode.Prefix, nameMatchSubMode, "前缀")
                GenericRadio(NameMatchSubMode.Middle, nameMatchSubMode, "中缀")
                GenericRadio(NameMatchSubMode.Suffix, nameMatchSubMode, "后缀")
                Text("区分大小写")
                Checkbox(
                    checked = caseSensitive.value,
                    onCheckedChange = { isChecked ->
                        caseSensitive.value = isChecked
                    }
                )
            }

            val forceSubStringDescribe = when (nameMatchSubMode.value) {
                NameMatchSubMode.None, NameMatchSubMode.Contain -> ""
                NameMatchSubMode.Prefix -> "是否强制需要后半部分"
                NameMatchSubMode.Middle -> "是否强制需要前后两部分"
                NameMatchSubMode.Suffix -> "是否强制需要前半部分"
            }
            if (nameMatchSubMode.value != NameMatchSubMode.None && nameMatchSubMode.value != NameMatchSubMode.Contain) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(forceSubStringDescribe)
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("完全正则匹配")
                Checkbox(
                    checked = regexType.value == RegexType.Match,
                    onCheckedChange = { isChecked ->
                        if (isChecked) regexType.value = RegexType.Match
                        else regexType.value = RegexType.Contain
                    }
                )
            }
            TextField(
                value = text.value,
                onValueChange = { text.value = it },
                label = { Text(if (nameMatchMode.value == NameMatchMode.EasyMode) "输入简单匹配文字" else "输入正则公式") }
            )
        }
    }

}

@Composable
fun MatchType(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {
    val fileType = remember { mutableStateOf("none") }
    val typeList = remember { mutableStateListOf<String>() }
    fun updateCurrentNode() {
        currentNode.value = null

        when (fileType.value) {
            "all" -> {
                currentNode.value = MatchTypeNode(
                    mode = FileType.All
                )
            }

            "selected" -> {
                if (typeList.size > 0) {
                    currentNode.value = MatchTypeNode(
                        mode = FileType.Custom,
                        typeList = typeList
                    )
                }
            }

        }
        end.value = currentNode.value != null
    }
    LaunchedEffect(
        fileType.value,
        typeList.size
    ) {
        updateCurrentNode()
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        GenericRadio("all", fileType, "全部类型")
        GenericRadio("selected", fileType, "选择类型")
    }
    when (fileType.value) {
        "selected" -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.width(48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(fontSize = 12.sp, color = Color.Gray, text = "文档")
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TypeListCheckBox("TXT", typeList, "txt")
                TypeListCheckBox("PDF", typeList, "pdf")
                TypeListCheckBox("PPT/x", typeList, "ppt", "pptx")
                TypeListCheckBox("DOC/x", typeList, "doc", "docx")
                TypeListCheckBox("XLS/x", typeList, "xls", "xlsx")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.width(48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(fontSize = 12.sp, color = Color.Gray, text = "视频")
                }
                TypeListCheckBox("MOV", typeList, "mov")
                TypeListCheckBox("AVI", typeList, "avi")
                TypeListCheckBox("MP4/s", typeList, "m4s", "mp4")

            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.width(48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(fontSize = 12.sp, color = Color.Gray, text = "图片")
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TypeListCheckBox("PNG", typeList, "png")
                TypeListCheckBox("JPG/eg", typeList, "jpg", "jpeg")
                TypeListCheckBox("WEBP", typeList, "webp")
                TypeListCheckBox("GIF", typeList, "gif")
                TypeListCheckBox("BMP", typeList, "bmp")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.width(48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(fontSize = 12.sp, color = Color.Gray, text = "自定义")
                }
                val textField = remember { mutableStateOf("") }
                TextField(
                    modifier = Modifier.height(48.dp),
                    value = textField.value,
                    onValueChange = {
                        textField.value = it
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(shape = CircleShape, onClick = {
                    if (textField.value.isNotEmpty()) typeList.add(textField.value)
                }) {
                    Text(text = "+")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            EasyList(typeList) { remove ->
                typeList.removeIf { it == remove }
            }
        }
    }
}


@Composable
fun MultiMatchNodeScreen(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {

    val type = remember { mutableStateOf("none") }
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 450.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GenericRadio("pair", type, "配对")
            GenericRadio("serial", type, "系列")
        }
        when (type.value) {
            "pair" -> MatchPair(end, currentNode)

            "serial" -> MatchSerial(end, currentNode)

        }
    }
}

@Composable
fun MatchPair(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {

    val inputPreview = remember { mutableStateOf("12312123-1-12312.mp4") }
    val inputRegex = remember { mutableStateOf("") }
    val inputResult = remember { mutableStateOf("") }

    val pairPreview = remember { mutableStateOf("") }
    val pairRegex = remember { mutableStateOf("") }
    val pairResult = remember { mutableStateOf("") }

    val error = remember { mutableStateOf(false) }

    fun updateCurrentNode() {
        currentNode.value = null
        if (inputRegex.value.isNotEmpty()) {
        }
        end.value = currentNode.value != null
    }

    LaunchedEffect(
        inputPreview.value,
        inputRegex.value
    ) {
        try {
            error.value = false
            inputResult.value = if (inputRegex.value.isNotEmpty()) {
                val regex = Regex(inputRegex.value)
                regex.find(inputPreview.value)?.value ?: ""
            } else ""
        } catch (e: Exception) {
            error.value = true
        }
        updateCurrentNode()
    }


    Column {
        TextField(
            value = inputPreview.value,
            onValueChange = {
                inputPreview.value = it
            },
            label = {
                Text("输入测试")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = inputRegex.value,
            onValueChange = {
                inputRegex.value = it
            },
            label = {
                Text("输入匹配正则")
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("匹配结果预览：${if (error.value) "报错了" else ""}${inputResult.value}")
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("相等")
            Checkbox(checked = false, onCheckedChange = {})
            Text("包含")
            Checkbox(checked = false, onCheckedChange = {})
            Text("部分")
            Checkbox(checked = false, onCheckedChange = {})
        }
        Row {
            Text("目标文件分割规则")
        }

    }
}

@Composable
fun MatchSerial(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {
    Column {
        Row {
            Text("文件特征匹配")
        }
        Row {
            Text("前导")
            Text("包含")

        }
    }
}

@Composable
fun OperateNodeScreen(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {
    val operateChooseState = remember { mutableStateOf("none") }
    fun updateOperateNode() {
        currentNode.value = null
        if (operateChooseState.value == "delete") {
            currentNode.value = ProcessDeleteNode()
        }
        end.value = currentNode.value != null
    }
    LaunchedEffect(
        operateChooseState.value
    ) {
        updateOperateNode()
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GenericRadio("rename", operateChooseState, "重命名")
            GenericRadio("mediaMerge", operateChooseState, "媒体合并")
            GenericRadio("move", operateChooseState, "移动")
            GenericRadio("delete", operateChooseState, "删除", radioColor = Color.Red, textColor = Color.Red)
        }
        when (operateChooseState.value) {
            "rename" -> Rename(end, currentNode)
            "mediaMerge" -> MediaMerge(end, currentNode)
            "move" -> Move(end, currentNode)
            "delete" -> Text(
                color = Color.Red,
                text = "是的，这个节点可以直接保存，删除匹配到的内容\n高危节点，极度注意前序操作\n相信我，你不会想把自己的电脑弄得一团糟的"
            )
        }
    }
}

@Composable
fun Rename(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {
    val renameType = remember { mutableStateOf("none") }
    Row {
        GenericRadio(
            "normal", renameType, "简单",
            textColor = Color.Red,
            radioColor = Color.Red,
        )
        GenericRadio(
            "regexWithPreview", renameType, "正则带预览",
            textColor = Color.Red,
            radioColor = Color.Red
        )
    }
    when (renameType.value) {
        "normal" -> EasyRename(end, currentNode)
        "regexWithPreview" -> RegexRename(end, currentNode)
    }
}

@Composable
fun EasyRename(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {
    val type = remember { mutableStateOf(EasyRenameMode.None) }
    val ignoreFileType = remember { mutableStateOf(true) }
    val replacement = remember { mutableStateOf("") }
    val sliderValue = remember { mutableStateOf("100") }


    fun updateCurrentNode() {
        currentNode.value = null
        when (type.value) {
            EasyRenameMode.None -> {}
            else -> {
                if (type.value == EasyRenameMode.Type && ignoreFileType.value) {
                    ignoreFileType.value = false
                }
                if (replacement.value.isNotBlank()) {
                    currentNode.value = ProcessEasyRenameNode(
                        easyRenameMode = type.value,
                        replaceString = replacement.value,
                        ignoreFileType = ignoreFileType.value,
                        maxRetryCount = sliderValue.value.toInt()
                    )
                }
            }
        }
        end.value = currentNode.value != null
    }

    LaunchedEffect(type.value, sliderValue.value, replacement.value) {
        updateCurrentNode()
    }

    LaunchedEffect(ignoreFileType.value) {
        if (type.value == EasyRenameMode.Type && ignoreFileType.value) {
            type.value = EasyRenameMode.None
        }
        updateCurrentNode()
    }

    Text("危险节点，请注意操作，谨慎保存", color = Color.Red)
    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        GenericRadio(EasyRenameMode.All, type, "全部替换")
        GenericRadio(EasyRenameMode.Prefix, type, "前插入")
        GenericRadio(EasyRenameMode.Suffix, type, "后插入")
        GenericRadio(EasyRenameMode.Type, type, "类型替换")
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("无视类型")
        Checkbox(checked = ignoreFileType.value, onCheckedChange = {
            ignoreFileType.value = it
        })
    }
    val hintText = when (type.value) {
        EasyRenameMode.None -> ""
        EasyRenameMode.Prefix -> "输入前插入内容"
        EasyRenameMode.Suffix -> "输入后插入内容"
        EasyRenameMode.Type -> "输入想要替换成的文件类型"
        EasyRenameMode.All -> "输入全部替换内容"
    }
    if (type.value != EasyRenameMode.None) {
        TextField(
            value = replacement.value,
            onValueChange = { replacement.value = it },
            label = { Text(hintText) }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = sliderValue.value,
            onValueChange = { newText ->
                if (newText.all { it.isDigit() }) { // 确保只有数字
                    sliderValue.value = newText
                }
            },
            label = { Text("输入冲突最大重试次数") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("最多重试${sliderValue.value}次")
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text("无视类型即只操作最后一个“.”之前的内容，比如aabb.txt，只操作aabb")
    Spacer(modifier = Modifier.height(8.dp))
    Text("类型替换即只操作最后一个“.”之后的内容，比如aabb.txt，只操作txt")
    Spacer(modifier = Modifier.height(8.dp))
    Text("类型替换和无视类型冲突")
}

@Composable
fun RegexRename(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {
    val renameRegex = remember { mutableStateOf("") }
    val renameReplace = remember { mutableStateOf("") }
    val previewText = remember { mutableStateOf("这里是-测试输入-点击修改.txt") }
    val error = remember { mutableStateOf(false) }

    val renamePreview = remember { mutableStateOf("") }
    val showHint = remember { mutableStateOf(false) }

    fun updateCurrentNode() {
        currentNode.value = null
        if (renameRegex.value.isNotEmpty()) {
            currentNode.value = ProcessRegexRenameNode(
                regex = renameRegex.value,
                replacement = renameReplace.value
            )
        }
        end.value = currentNode.value != null
    }

    LaunchedEffect(renameReplace.value, renameRegex.value) {
        try {
            error.value = false
            renamePreview.value = if (renameRegex.value.isNotEmpty())
                previewText.value.replace(
                    Regex(renameRegex.value),
                    renameReplace.value
                ) else previewText.value
        } catch (e: Exception) {
            error.value = true
        }
        updateCurrentNode()
    }

    Text("危险节点，请注意自己的公式，谨慎保存", color = Color.Red)
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = previewText.value,
        onValueChange = { previewText.value = it },
        label = { Text("输入测试输入") }
    )
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = renameRegex.value,
        onValueChange = { renameRegex.value = it },
        label = { Text("输入正则公式") }
    )
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = renameReplace.value,
        onValueChange = { renameReplace.value = it },
        label = { Text("输入替换内容") }
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        fontSize = 12.sp,
        color = Color.Gray,
        text = "结果预览" + if (error.value) " 有报错" else ""
    )

    Spacer(modifier = Modifier.height(8.dp))
    Text(renamePreview.value)
    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("查看提示")
        Checkbox(modifier = Modifier.size(24.dp), checked = showHint.value, onCheckedChange = {
            showHint.value = it
        })
    }
    if (showHint.value) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            fontSize = 14.sp,
            text = "你需要一些正则的知识来正确这个节点，去问AI也是个好办法" +
                    "\n$0,$1...代表着捕获组，利用这个你可以相当方便的对原输入进行处理" +
                    "\n所有输入默认过滤路径，以防止出现不可挽回的后果(你不会想知道的)" +
                    "\n我会为你提供一些预设的正则匹配如下"

        )
        Row {
            Button(onClick = {
                previewText.value = "1023456789-1-30112.mp4"
                renameRegex.value = "^\\d{5,}"
                renameReplace.value = "bili_\$0"
            }) {
                Text("bili分割文件")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                previewText.value = "作者名_标题_内容.mp4"
                renameRegex.value = "^(.*?)_"
                renameReplace.value = "douyin_\$0"
            }) {
                Text("通配首个_替换")
            }
        }
    }
}

@Composable
fun MediaMerge(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {
    val type = remember { mutableStateOf("none") }
    Row {
        GenericRadio("normal", type, "媒体合并")
    }
}

@Composable
fun Move(
    end: MutableState<Boolean>,
    currentNode: MutableState<ProcessNode?>
) {
    val type = remember { mutableStateOf("none") }
    val folder = remember { mutableStateOf("") }
    val sliderValue = remember { mutableStateOf("100") }
    val subFolder = remember { mutableStateOf(false) }
    val subFolderRegex = remember { mutableStateOf("") }
    val subFolderReplaceRegex = remember { mutableStateOf("$0") }


    fun updateCurrentNode() {
        currentNode.value = null
        if (folder.value.isNotEmpty()) {
            currentNode.value = ProcessMoveNode(
                destFolder = folder.value,
                maxRetryCount = sliderValue.value.toInt(),
                subFolder = subFolder.value,
                subFolderRegex = subFolderRegex.value,
                subFolderReplaceRegex = subFolderReplaceRegex.value
            )
        }
        end.value = currentNode.value != null
    }
    LaunchedEffect(
        subFolder.value,
        type.value,
        folder.value,
        subFolderRegex.value,
        subFolderReplaceRegex.value
    ) {
        updateCurrentNode()
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(
            enabled = folder.value.isEmpty(),
            onClick = {
                val fileChooser = JFileChooser()
                fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                fileChooser.setDialogTitle("选择移动文件夹")
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
                fileChooser.setAcceptAllFileFilterUsed(false)
                fileChooser.preferredSize = Dimension(800, 500)
                fileChooser.isVisible = true
                val result = fileChooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val path = fileChooser.selectedFile.toPath().toString()
                    folder.value = path
                }
            }) {
            Text("选择文件夹")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(fontSize = 12.sp, color = Color.Gray, text = "或者")
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = folder.value,
            onValueChange = { folder.value = it },
            label = { Text("输入文件夹") }
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("子文件夹模式")
        Checkbox(checked = subFolder.value, onCheckedChange = {
            subFolder.value = it
        })
    }
    if (subFolder.value) {
        val previewInput = remember { mutableStateOf("这里是-预览输入-文件.txt") }
        val previewResult = remember { mutableStateOf("") }
        val error = remember { mutableStateOf(false) }
        LaunchedEffect(
            previewInput.value,
            previewResult.value,
            subFolderRegex.value,
            subFolderReplaceRegex.value
        ) {
            error.value = false
            try {
                previewResult.value = if (subFolderRegex.value.isNotEmpty()) {
                    val regex = Regex(subFolderRegex.value)
                    val matchResult = regex.find(previewInput.value)
                    val result = if (matchResult != null) {
                        subFolderReplaceRegex.value.replace(Regex("\\$(\\d)")) { match ->
                            val groupIndex = match.groupValues[1].toInt()
                            matchResult.groupValues.getOrElse(groupIndex) { "" }
                        }
                    } else {
                        ""
                    }
                    result
                } else ""
            } catch (e: Exception) {
                error.value = true
            }
        }

        TextField(value = previewInput.value, onValueChange = {
            previewInput.value = it
        }, label = { Text("输入预览输入") })
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(value = subFolderRegex.value, onValueChange = {
                subFolderRegex.value = it
            }, label = { Text("输入子文件夹正则") })

            TextField(value = subFolderReplaceRegex.value, onValueChange = {
                subFolderReplaceRegex.value = it
            }, label = { Text("输入捕获组替换正则") })
        }

        Text(
            "预览匹配结果：${if (error.value) "出错了 " else ""}" +
                    if (previewResult.value.isBlank()) "未匹配到结果，不移动" else previewResult.value
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("如果你不了解正则和捕获组的概念，可以尝试询问AI来满足你的需求\n在这里 子文件夹会由匹配结果按规则创建")
    }

    Spacer(modifier = Modifier.height(36.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = sliderValue.value,
            onValueChange = { newText ->
                if (newText.all { it.isDigit() }) { // 确保只有数字
                    sliderValue.value = newText
                }
            },
            label = { Text("输入冲突最大重试次数") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("最多重试${sliderValue.value}次")
    }


    Spacer(modifier = Modifier.height(8.dp))
    if (folder.value.isNotEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(folder.value, modifier = Modifier.weight(1f))
            Button(
                shape = CircleShape,
                modifier = Modifier.size(24.dp),
                contentPadding = PaddingValues(0.dp),
                onClick = { folder.value = "" }
            ) {
                Text("-")
            }
        }
    }
}


@Composable
fun <T> GenericRadio(
    option: T,
    selectedOption: MutableState<T>,
    label: String,
    textColor: Color? = null,
    radioColor: Color? = null,
    onOptionSelected: ((T) -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (textColor != null) {
            Text(label, color = textColor)
        } else {
            Text(label)
        }
        RadioButton(
            modifier = Modifier.padding(0.dp),
            selected = (selectedOption.value == option),
            onClick = {
                selectedOption.value = option
                onOptionSelected?.invoke(option)
            },
            colors = RadioButtonDefaults.colors( // 设置RadioButton文本颜色
                selectedColor = radioColor ?: MaterialTheme.colors.secondary,
                unselectedColor = radioColor ?: MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        )
    }
}

@Preview
@Composable
fun TypeListCheckBox(
    label: String,
    typeList: MutableList<String>,
    vararg fileType: String
) {
    val isChecked = remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        Checkbox(
            checked = isChecked.value,
            onCheckedChange = { checked ->
                isChecked.value = checked
                if (checked) {
                    fileType.forEach { typeList.add(it) }
                } else {
                    fileType.forEach { type -> typeList.removeIf { it == type } }
                }
            }
        )
    }
}