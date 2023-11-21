package model

import kotlinx.serialization.Serializable
import page.pipeline.PipeLineViewModel.currentNodeDescribe
import page.pipeline.PipeLineViewModel.hitLog
import page.pipeline.PipeLineViewModel.tempLog
import java.io.File
import java.nio.file.Files


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

                if (inputs.first().log) {
                    tempLog.add("输入：${it.path}")
                }

                var processedData: Any = it
                for (node in nodes) {
                    processedData = node.realProcess(processedData)
                    if (processedData == false) break
                }
                if (processedData != false) {
                    hitLog.add(processedData.toString())
                }
                hitLog.add("\n")
            }
        }

    }
}

interface Describe {
    val nodeName: String
        get() = "default describe"

    fun describe(): String = nodeName
    fun log(): String = nodeName
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
    val log = true
    fun realInput(result: (File) -> Unit) {
        preInput()
        process(result)
    }

    fun preInput() {
        currentNodeDescribe.value = describe() //todo clean
    }

}

@Serializable
sealed class ProcessNode : Node(), Process {
    val log = true
    fun realProcess(input: Any): Any {
        preProcess()
        val result = process(input)
        val hit = if (result != false) "Hit" else "Miss"
        tempLog.add("${log()} $hit")
        return result
    }

    fun preProcess() {
        currentNodeDescribe.value = log()
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
    None, Prefix, Middle, Contain, Suffix
}

@Serializable
enum class RegexType {
    None, Contain, Match
}

@Serializable
class FilterNode(
    val filterHiddenFile: Boolean = true,
    val filterDirectory: Boolean = false,
) : MatchNode() {

    override fun describe(): String {
        return "过滤：${if (filterDirectory) "文件夹 " else ""}${if (filterHiddenFile) "隐藏文件 " else ""}"
    }

    override fun log(): String {
        return "过滤器节点"
    }

    override fun rule(input: Any): Boolean {
        return when (input) {
            is String -> return true
            is File -> {
                if (!input.exists()) return false
                if (filterHiddenFile && input.isHidden) {
                    return false
                }
                if (input.isDirectory) {
                    if (filterDirectory) false else true
                } else {
                    true
                }
            }

            else -> return false
        }
    }

    override fun operate(input: Any): Any {
        return input
    }
}

@Serializable
class MatchNameNode(
    val matchString: String? = null,
    val matchRegex: String? = null,
    val mode: NameMatchMode = NameMatchMode.None,
    val subMode: NameMatchSubMode = NameMatchSubMode.None,
    val filterPurePath: Boolean = true,
    val forceSubString: Boolean = false,
    val caseSensitive: Boolean = false,
    val regexType: RegexType = RegexType.None
) : MatchNode() {

    override fun rule(input: Any): Boolean {

        val path = when (input) {
            is String -> input
            is File -> {
                if (!input.exists()) return false
                input.path
            }

            else -> return false
        }

        return when (mode) {
            NameMatchMode.AllMode -> true
            NameMatchMode.EasyMode -> matchString?.let {
                return if (filterPurePath) {
                    val file = File(path).nameWithoutExtension
                    matchByString(file, it)
                } else {
                    matchByString(path, it)
                }
            } ?: false

            NameMatchMode.RegexMode -> matchRegex?.let {
                return if (filterPurePath) {
                    val file = File(path).name
                    return if (regexType == RegexType.Match) {
                        file.matches(Regex(matchRegex))
                    } else {
                        file.contains(Regex(matchRegex))
                    }
                } else {
                    path.matches(Regex(matchRegex))
                }
            } ?: false

            NameMatchMode.None -> false
        }

    }

    override fun operate(input: Any): Any {
        //default 只做个过滤
        return if (input is String) {
            File(input).takeIf { it.exists() } ?: false
        } else {
            input
        }
    }

    override fun describe(): String {
        val matchString = when (mode) {
            NameMatchMode.None, NameMatchMode.AllMode -> {
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

                    NameMatchSubMode.Middle -> {
                        "\n中缀匹配：$matchString"
                    }
                }
            }

            NameMatchMode.RegexMode -> {
                "\n正则公式：$matchRegex"
            }
        }

        return when (mode) {
            NameMatchMode.AllMode -> "匹配：全部文件$matchString"
            NameMatchMode.EasyMode -> "匹配：简单文件名$matchString"
            NameMatchMode.RegexMode -> "匹配：正则匹配$matchString"
            NameMatchMode.None -> "无匹配"
        }
    }

    override fun log(): String {
        return "匹配节点"
    }

    private fun matchByString(path: String, matchString: String): Boolean {
        return when (subMode) {
            NameMatchSubMode.Contain -> path.contains(matchString, !caseSensitive)
            NameMatchSubMode.Prefix -> {
                if (forceSubString) {
                    (path.length > matchString.length) && path.startsWith(matchString, ignoreCase = !caseSensitive)
                } else {
                    path.startsWith(matchString, ignoreCase = !caseSensitive)
                }
            }

            NameMatchSubMode.Suffix -> {
                if (forceSubString) {
                    (path.length > matchString.length) && path.endsWith(matchString, ignoreCase = !caseSensitive)
                } else {
                    path.endsWith(matchString, ignoreCase = !caseSensitive)
                }
            }

            NameMatchSubMode.Middle -> {
                var result = false
                if (matchString.isEmpty() || path.length <= matchString.length) result = false
                var index = path.indexOf(matchString, ignoreCase = !caseSensitive)
                while (index != -1) {
                    if (index > 0 && index + matchString.length < path.length) result = true
                    index = path.indexOf(matchString, index + 1, ignoreCase = !caseSensitive)
                }

                if (forceSubString) {
                    result
                } else {
                    path.contains(matchString, !caseSensitive)
                }
            }

            NameMatchSubMode.None -> false
        }
    }
}

enum class FileType {
    None, All, Custom
}

@Serializable
class MatchTypeNode(
    val mode: FileType = FileType.None,
    val typeList: MutableList<String>? = null
) : MatchNode() {

    override fun rule(input: Any): Boolean {
        when (mode) {
            FileType.None -> false
            FileType.All -> true
            FileType.Custom -> {
                when (input) {
                    is File -> {
                        val extension = input.extension
                        if (extension.isEmpty()) return false
                        return typeList?.contains(extension) == true
                    }
                }
            }
        }
        return true
    }

    override fun operate(input: Any): Any {
        return input
    }

    override fun describe(): String {
        val type = if (mode == FileType.All) "全部类型" else typeList?.joinToString(" ")
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
        return Pair(input, null)
    }

}


@Serializable
class MatchMultiFileNode(
) : MatchNode() {
    //1,pair
    //2,serial

    override fun rule(input: Any): Boolean {
        return input is File && input.exists()
    }

    fun matchPairFile(filePath: String): File? {
        return File(filePath).listFiles { file ->
            pairRule(file.name)
        }?.firstOrNull()
    }

    fun pairRule(name: String): Boolean {
        return false
    }

    //todo 所有匹配节点和操作节点都校验有效性
    override fun operate(input: Any): Any {
        return when (input) {
            is File -> {
                val directory = input.parentFile
                val name = input.nameWithoutExtension

                val matchedFile = matchPairFile(input.path)
                return if (matchedFile != null) {
                    Pair(input, matchedFile)
                } else {
                    false
                }
            }

            else -> false
        }
    }
}


@Serializable
enum class EasyRenameMode {
    None, All, Prefix, Suffix, Type
}

@Serializable
class ProcessEasyRenameNode(
    var easyRenameMode: EasyRenameMode = EasyRenameMode.None,
    var replaceString: String,
    var ignoreFileType: Boolean = true,
    var maxRetryCount: Int = 100
) : ProcessNode() {

    override fun log(): String {
        return "简单重命名 $easyRenameMode"
    }

    override fun describe(): String {
        val desc = when (easyRenameMode) {
            EasyRenameMode.None -> "none"
            EasyRenameMode.Prefix -> "\n内容前插入：$replaceString"
            EasyRenameMode.Suffix -> "\n内容后插入：$replaceString"
            EasyRenameMode.Type -> "\n类型替换为：$replaceString"
            EasyRenameMode.All -> "\n内容替换为：$replaceString"
        }

        return "简单重命名：$desc"
    }

    override fun process(input: Any): Any {
        var result = input
        when (input) {
            is String -> {
                val lastDotIndex = input.lastIndexOf('.')
                result = when (easyRenameMode) {
                    EasyRenameMode.None -> input
                    EasyRenameMode.Prefix -> {
                        replaceString + input
                    }

                    EasyRenameMode.Suffix -> {
                        if (lastDotIndex != -1 && ignoreFileType) {
                            input.substring(0, lastDotIndex) + replaceString + input.substring(lastDotIndex)
                        } else {
                            input + replaceString
                        }
                    }

                    EasyRenameMode.Type -> {
                        if (lastDotIndex != -1) {
                            input.substring(0, lastDotIndex + 1) + replaceString
                        } else {
                            input
                        }
                    }

                    EasyRenameMode.All -> {
                        replaceString
                    }
                }
            }

            is File -> {
                if (input.exists().not()) return false
                val name = input.nameWithoutExtension
                var type = ""
                if (input.extension.isNotEmpty()) {
                    type = "." + input.extension
                }
                val parent = input.parent ?: return false
                when (easyRenameMode) {
                    EasyRenameMode.None -> {}
                    EasyRenameMode.Prefix -> {
                        var count = 0
                        while (count <= maxRetryCount) {
                            val suffix = if (count > 0) "-$count" else ""
                            var newName = "$replaceString$name$suffix$type"
                            result = File(parent, newName)
                            if (!result.exists()) {
                                return if (input.renameTo(result)) result else false
                            }
                            count++
                        }
                        return false
                    }

                    EasyRenameMode.Suffix -> {
                        var count = 0
                        while (count <= maxRetryCount) {
                            val suffix = if (count > 0) "-$count" else ""
                            var newName =
                                if (ignoreFileType) "$name$replaceString$suffix$type" else "$name$type$replaceString$suffix"
                            result = File(parent, newName)
                            if (!result.exists()) {
                                return if (input.renameTo(result)) result else false
                            }
                            count++
                        }
                        return false
                    }

                    EasyRenameMode.Type -> {
                        var count = 0
                        while (count <= maxRetryCount) {
                            val suffix = if (count > 0) "-$count" else ""
                            val newName = "$name$suffix.$replaceString"
                            result = File(parent, newName)
                            if (!result.exists()) {
                                val renameResult = input.renameTo(result)
                                return if (renameResult) result else false
                            }
                            count++
                        }
                        return false
                    }

                    EasyRenameMode.All -> {
                        var count = 0
                        while (count <= maxRetryCount) {
                            val suffix = if (count > 0) "-$count" else ""
                            val newName = if (ignoreFileType) "$replaceString$suffix$type" else "$replaceString$suffix"
                            result = File(parent, newName)
                            if (!result.exists()) {
                                val renameResult = input.renameTo(result)
                                return if (renameResult) result else false
                            }
                            count++
                        }
                        return false
                    }
                }
            }
        }

        return result
    }

}

@Serializable
class ProcessRegexRenameNode(
    val regex: String,
    val replacement: String
) : ProcessNode() {

    override fun log(): String {
        return "正则重命名"
    }

    override fun describe(): String {
        return "正则重命名：$regex\n替换为：$replacement"
    }

    override fun process(input: Any): Any {

        var result: Any = input
        when (input) {
            is String -> {
                result = if (regex.isNotEmpty())
                    input.replace(Regex(regex), replacement) else input
            }

            is File -> {
                if (input.exists().not()) return false

                val oldName = input.name
                val newName = if (regex.isNotEmpty())
                    oldName.replace(Regex(regex), replacement) else oldName
                val parent = input.parent ?: return false
                val newFile = File(parent, newName)
                return input.renameTo(newFile)
            }
        }
        return result
    }
}

@Serializable
class ProcessMediaMergeNode() : ProcessNode() {

}

@Serializable
class ProcessMultiFileNode() : ProcessNode() {

}

@Serializable
class ProcessMoveNode(
    val destFolder: String,
    val maxRetryCount: Int = 100,
    val subFolder: Boolean = false,
    val subFolderRegex: String = "",
    val subFolderReplaceRegex: String = ""
) : ProcessNode() {


    override fun describe(): String {
        return "移动到：$destFolder"
    }

    fun ensurePathExists(pathString: String): Boolean {
        val path = File(pathString)
        return if (path.exists()) {
            path.isDirectory
        } else {
            path.mkdirs()
        }
    }

    override fun process(input: Any): Any {

        when (input) {
            is File -> {
                if (subFolder) {
                    try {
                        if (!input.exists() || !ensurePathExists(destFolder)) return false

                        val matchResult = subFolderRegex.toRegex().find(input.name)
                        val subFolder = matchResult?.let {
                            subFolderReplaceRegex.replace(Regex("\\$(\\d)")) { match ->
                                val groupIndex = match.groupValues[1].toInt()
                                it.groupValues.getOrElse(groupIndex) { "" }
                            }
                        } ?: return false
                        if (!ensurePathExists("$destFolder\\$subFolder")) return false

                        val destBase = "$destFolder\\$subFolder"
                        var destFile = File(destBase, input.name)
                        var count = 0
                        while (destFile.exists() && count <= maxRetryCount) {
                            count++
                            val newName = "${input.nameWithoutExtension}-$count." +
                                    (input.extension.takeIf { it.isNotEmpty() } ?: "")
                            destFile = File(destBase, newName)
                        }

                        return input.renameTo(destFile)
                    } catch (e: Exception) {
                        return false
                    }
                } else if (input.exists() && ensurePathExists(destFolder)) {
                    var destFile = File(destFolder, input.name)
                    var count = 0
                    while (destFile.exists() && count <= maxRetryCount) {
                        count++
                        val newName = if (input.extension.isNotEmpty())
                            "${input.nameWithoutExtension}-$count.${input.extension}" else
                            "${input.nameWithoutExtension}-$count"
                        destFile = File(destFolder, newName)
                    }

                    return input.renameTo(destFile)
                }
            }
        }

        return false
    }
}

@Serializable
class ProcessDeleteNode(
) : ProcessNode() {

    override fun describe(): String {
        return "删除节点"
    }

    override fun process(input: Any): Any {
        when (input) {
            is File -> {
                if (input.exists() && input.isDirectory) {
                    input.delete()
                    return false //截断
                }
            }
        }
        return false
    }
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