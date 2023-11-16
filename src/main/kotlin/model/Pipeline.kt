package model

import kotlinx.serialization.Serializable
import page.pipeline.PipeLineViewModel.currentNodeDescribe
import java.io.File


/***
 * //监测文件夹
 * 1,匹配节点
 * 遍历文件夹中所有文件名
 * 根据规则进行一个接一个的匹配，也就是一个规则可能有多个匹配节点
 * 可能有一个文件匹配，匹配成功，直接传递到下一个节点处理
 * 或者是需要多个文件匹配
 *  当匹配到第一个文件时，去匹配第二个文件，匹配到就算成功，匹配不到就算失败
 *
 * 2，操作节点
 * 接受传递来的单个文件，并对其进行操作
 *  重命名、移动
 * 接受传递来的多个文件，对其进行操作
 *  音视频合并
 * //目标文件夹
 */

@Serializable
data class Pipeline(
    var id: Int = 0,
    var name: String = "默认管线",
    var runningState: Boolean = false,
    var singleInput: Boolean = true,//todo multi input
    var inputs: MutableList<InputNode>,
    var nodes: MutableList<ProcessNode>
) {
    val service = false

    fun savable(): Boolean {
        return inputs.isNotEmpty() and nodes.isNotEmpty()
    }

    fun execute() {
        if (singleInput) {

            inputs.first().realInput {
                var processedData: Any = it
                println("in ${it.path}")
                nodes.forEach nodeLoop@{ node ->
                    processedData = node.realProcess(processedData)
                    //todo 这里操作界面数据，因为要展示，包括日志等
                    if (processedData == false) return@nodeLoop
                }
            }
        }

    }
}

interface Describe {
    val nodeName: String
        get() = "default describe"

    fun describe(): String = nodeName
}

interface Process {
    open fun process(input: Any): Any = input
    open fun process(result: (File) -> Unit) = Unit
}

interface Match {
    abstract fun rule(input: Any): Boolean
    abstract fun operate(input: Any): Any
}


@Serializable
sealed class Node : Describe, Process {
    open fun clear() {}
    open fun savable(): Boolean = false
}

@Serializable
sealed class InputNode : Node() {
    fun realInput(result: (File) -> Unit) {
        preInput()
        process(result)
    }

    fun preInput() {
        currentNodeDescribe.value = describe()
    }

}

@Serializable
sealed class ProcessNode : Node(), Process {
    fun realProcess(input: Any): Any {
        preProcess()
        return process(input)
    }

    fun preProcess() {
        currentNodeDescribe.value = describe()
        println("process ${describe()}")
    }
}

@Serializable
sealed class MatchNode : ProcessNode(), Match {
    final override fun process(input: Any): Any {
        return if (rule(input)) operate(input) else false
    }
}

@Serializable
class InputMultiFolderNode(
) : InputNode() {


    val sourceFolderList = mutableListOf<String>()
    var recurse = false

    override fun process(result: (File) -> Unit) {
        sourceFolderList.forEach { folderPath ->
            val folder = File(folderPath)
            if (folder.exists() && folder.isDirectory) {
                if (recurse) {
                    folder.walk().forEach { result(it) }
                } else {
                    folder.listFiles()?.forEach { result(it) }
                }
            }
        }
    }

    override fun describe(): String {
        val recurse = if (recurse) "\n递归子文件夹" else ""
        return "汇聚：\n${sourceFolderList.joinToString("\n")}$recurse"
    }

    override fun savable(): Boolean {
        return sourceFolderList.isNotEmpty()
    }

    override fun clear() {
        this.sourceFolderList.clear()
    }
}

@Serializable
enum class NameMatchMode {
    None, AllMode, EasyMode, RegexMode
}

@Serializable
enum class NameMatchSubMode {
    None, Prefix, Contain, Suffix
}

@Serializable
class MatchNameNode(
    val matchString: String? = null,
    val matchRegex: String? = null,
    val mode: NameMatchMode = NameMatchMode.None,
    val subMode: NameMatchSubMode = NameMatchSubMode.None,
    val forceSubString: Boolean = false,
    val containDirectory: Boolean = false
) : MatchNode() {

    override fun rule(input: Any): Boolean {

        val path = when (input) {
            is String -> input
            is File -> {
                if (!input.exists()) return false
                if (input.isDirectory) {
                    if (containDirectory) input.path else return false
                } else {
                    input.path
                }
            }

            else -> return false
        }

        return when (mode) {
            NameMatchMode.AllMode -> true
            NameMatchMode.EasyMode -> matchString?.let { matchByString(path, it) } ?: false
            NameMatchMode.RegexMode -> matchRegex?.let { path.matches(Regex(matchRegex)) } ?: false
            NameMatchMode.None -> false
        }

    }

    override fun operate(input: Any): Any {
        return if (input is String) File(input) else input
    }

    override fun describe(): String {
        val containDirectory = if (containDirectory) "\n包含文件夹" else ""
        val matchString = when (mode) {
            NameMatchMode.None -> {
                ""
            }

            NameMatchMode.AllMode -> {
                ""
            }

            NameMatchMode.EasyMode -> {
                when (subMode) {
                    NameMatchSubMode.None -> {
                        ""
                    }

                    NameMatchSubMode.Prefix -> {
                        "\n前缀匹配：$matchString"
                    }

                    NameMatchSubMode.Contain -> {
                        "\n包含匹配：$matchString"
                    }

                    NameMatchSubMode.Suffix -> {
                        "\n后缀匹配：$matchString"
                    }
                }
            }

            NameMatchMode.RegexMode -> {
                "\n正则公式：$matchRegex$containDirectory"
            }
        }

        return when (mode) {
            NameMatchMode.AllMode -> "匹配：全部文件$matchString$containDirectory"
            NameMatchMode.EasyMode -> "匹配：简单文件名$matchString"
            NameMatchMode.RegexMode -> "匹配：正则匹配$matchString"
            NameMatchMode.None -> "无匹配"
        }
    }

    private fun matchByString(path: String, matchString: String): Boolean {
        return when (subMode) {
            NameMatchSubMode.Contain -> path.contains(matchString)
            NameMatchSubMode.Prefix -> path.hasPrefix(matchString, forceSubString)
            NameMatchSubMode.Suffix -> path.hasSuffix(matchString, forceSubString)
            NameMatchSubMode.None -> false
        }
    }

    private fun String.hasPrefix(prefix: String, requireExtra: Boolean): Boolean {
        return startsWith(prefix) && (!requireExtra || length > prefix.length)
    }

    private fun String.hasSuffix(suffix: String, requireExtra: Boolean): Boolean {
        return endsWith(suffix) && (!requireExtra || length > suffix.length)
    }
}

enum class FileType {
    None, All
}

@Serializable
class MatchTypeNode(
    val mode: FileType = FileType.None
) : MatchNode() {
    override fun rule(input: Any): Boolean {
        return true
    }

    override fun operate(input: Any): Any {
        return input
    }

    override fun describe(): String {
        val type = if (mode == FileType.All) "全部类型" else ""
        return "匹配：$type"
    }

}

@Serializable
class MatchSizeNode() : MatchNode() {
    override fun rule(input: Any): Boolean {
        return true
    }

    override fun operate(input: Any): Any {
        return input
    }

}

@Serializable
class MatchPairMediaNode() : MatchNode() {
    override fun rule(input: Any): Boolean {
        return true
    }

    override fun operate(input: Any): Any {
        return input
    }

}

@Serializable
class MatchMultiFileNode() : MatchNode() {
    override fun rule(input: Any): Boolean {
        return true
    }

    override fun operate(input: Any): Any {
        return input
    }

}

@Serializable
class ProcessRenameNode() : ProcessNode() {

}

@Serializable
class ProcessSaveNode() : ProcessNode() {

    override fun process(input: Any): Any {

        return super.process(input)
    }
}


@Serializable
class ProcessMediaMergeNode() : ProcessNode() {

}

@Serializable
class ProcessMultiFileNode() : ProcessNode() {

}

@Serializable
class ProcessMoveNode : ProcessNode() {
    //    val destFolderList = mutableStateListOf<String>()
    val singleFolderSave = false

}


//class ActionNode(private val action: (File) -> Unit) : Node {
//    override fun process(file: File): Boolean {
//        action(file)
//        return true  // 继续流程
//    }
//}

//class Pipeline(private val nodes: List<Node>) {
//    fun execute(file: File) {
//        for (node in nodes) {
//            val continueProcess = node.process(file)
//            if (!continueProcess) break
//        }
//    }
//}

//val pipeline = Pipeline(listOf(
//    MatchNode { file -> file.extension == "txt" },  // 匹配节点
//    ActionNode { file -> println("Processing file: ${file.name}") }  // 操作节点
//    // 其他节点...
//))
//
//val fileToProcess = File("example.txt")
//pipeline.execute(fileToProcess)

//val pipeline = Pipeline(listOf(ListToFileNode(), FileToFileNode()))
//val initialInput = listOf(File("path/to/file1"), File("path/to/file2"))
//val finalOutput = pipeline.execute(initialInput)