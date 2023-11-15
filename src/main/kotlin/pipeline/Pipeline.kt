package pipeline

import androidx.compose.runtime.mutableStateListOf
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

data class Pipeline(
    var name: String = "管线",
    var runningState: Boolean = false,
    var input: InputNode,
    var nodes: MutableList<ProcessNode>
) {

    val service = false

    fun savable(): Boolean {
        return input.savable() // todo 记得加上and nodes.isNotEmpty()
    }

    fun execute(realInput: String) {
        input.process {
            var processedData: Any = it
            nodes.forEach nodeLoop@{ node ->
                processedData = node.process(processedData)

                //todo 这里操作界面数据，因为要展示，包括日志等
                if (processedData == false) return@nodeLoop
            }
        }
    }

    fun deepCopy(): Pipeline {
        return this.copy(
            name = name,
            runningState = runningState,
            input = this.input,
            nodes = this.nodes
        )
    }
}


sealed class Node {
    open var nodeName: String = "name"
    open fun clear() {}
    open fun describe(): String = nodeName
}

sealed class InputNode : Node() {
    open fun process(result: (File) -> Unit) {}
    open fun savable(): Boolean = false
}

sealed class ProcessNode : Node() {
    open fun process(input: Any): Any = input
}

interface Match {
    open fun rule(input: Any): Boolean = false
    open fun operate(input: Any): Any = input
}

sealed class MatchNode : ProcessNode(), Match {
    override fun process(input: Any): Any {
        return if (rule(input)) operate(input) else false
    }
}

class InputMultiFolderNode() : InputNode() {
    override var nodeName: String
        get() = "文件夹监测节点"
        set(value) {}
    val sourceFolderList = mutableStateListOf<String>()
    override fun process(result: (File) -> Unit) {
        sourceFolderList.forEach { folderPath ->
            val folder = File(folderPath)
            if (folder.exists() && folder.isDirectory) {
                folder.walk().forEach { result(it) }
            }
        }
    }

    override fun savable(): Boolean {
        return sourceFolderList.isNotEmpty()
    }

    override fun clear() {
        this.sourceFolderList.clear()
    }
}


enum class NameMatchMode {
    ALL_MODE, EASY_MODE, REGEX_MODE
}

enum class NameMatchSubMode {
    PREFIX, CONTAIN, SUFFIX
}

class MatchNameNode(
    val matchString: String? = null,
    val matchRegex: Regex? = null,
    val mode: NameMatchMode = NameMatchMode.EASY_MODE,
    val subMode: NameMatchSubMode = NameMatchSubMode.CONTAIN,
    val forceCondition: Boolean = false,
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
            NameMatchMode.ALL_MODE -> true
            NameMatchMode.EASY_MODE -> matchString?.let { matchByString(path, it) } ?: false
            NameMatchMode.REGEX_MODE -> matchRegex?.let { path.matches(it) } ?: false
        }

    }

    override fun operate(input: Any): Any {
        return if (input is String) File(input) else input
    }

    override fun describe(): String {
        return when (mode) {
            NameMatchMode.ALL_MODE -> "全部文件匹配"
            NameMatchMode.EASY_MODE -> "简单文件名匹配：$matchString"
            NameMatchMode.REGEX_MODE -> "正则匹配：$matchRegex"
        }
    }

    private fun matchByString(path: String, matchString: String): Boolean {
        return when (subMode) {
            NameMatchSubMode.CONTAIN -> path.contains(matchString)
            NameMatchSubMode.PREFIX -> path.hasPrefix(matchString, forceCondition)
            NameMatchSubMode.SUFFIX -> path.hasSuffix(matchString, forceCondition)
        }
    }

    private fun String.hasPrefix(prefix: String, requireExtra: Boolean): Boolean {
        return startsWith(prefix) && (!requireExtra || length > prefix.length)
    }

    private fun String.hasSuffix(suffix: String, requireExtra: Boolean): Boolean {
        return endsWith(suffix) && (!requireExtra || length > suffix.length)
    }
}

class MatchTypeNode() : MatchNode() {

}

class MatchSizeNode() : MatchNode() {

}

class MatchPairMediaNode() : MatchNode() {

}

class MatchMultiFileNode() : MatchNode() {

}

class ProcessRenameNode() : ProcessNode() {

}

class ProcessSaveNode() : ProcessNode() {

    override fun process(input: Any): Any {

        return super.process(input)
    }
}


class ProcessMediaMergeNode() : ProcessNode() {

}

class ProcessMultiFileNode() : ProcessNode() {

}

class ProcessMoveNode : ProcessNode() {
    val destFolderList = mutableStateListOf<String>()
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