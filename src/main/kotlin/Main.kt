import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Desktop
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.prefs.Preferences
import kotlin.concurrent.schedule

@Composable
@Preview
fun App() {
    var migrate by remember { mutableStateOf("手动迁移") }
    var service by remember { mutableStateOf("启动服务") }
    val migrationStatus = remember { mutableStateListOf<String>() }
    val SOURCE_FOLDER = "D:/STOP"
    val DEST_FOLDER = "E:/抖音归档"

    val startupEnabled = remember { mutableStateOf(isStartupEnabled()) }


    val exePath = remember { mutableStateOf(getExePath()) }

    MaterialTheme {
        Column {
            var manualClick by remember { mutableStateOf(0) }
            Button(onClick = {
                migrationStatus.clear()
                changeFolder(SOURCE_FOLDER, DEST_FOLDER, migrationStatus)
                migrate = "手动迁移点击：$manualClick"
                manualClick++
            }) {
                Text(migrate)
            }

            if (migrationStatus.isNotEmpty()) {
                Text(migrationStatus.last())
            } else {
                Text("未开始")
            }

            val fileMigrationService = FileMigrationService(SOURCE_FOLDER, DEST_FOLDER)

            Button(onClick = {
                service = if (service == "运行中，点击停止") {
                    fileMigrationService.stop()
                    "启动服务"
                } else {
                    fileMigrationService.start()
                    "运行中，点击停止"
                }
            }) {
                Text(service)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "跟随系统启动 需要管理员权限")
                Spacer(modifier = Modifier.width(8.dp))
                Checkbox(
                    checked = startupEnabled.value,
                    onCheckedChange = { isChecked ->
                        run {
                            startupEnabled.value = isChecked
                            setStartupEnabled(isChecked, getExePath())
                        }
                    }
                )
            }
            Text(text = "当前 .exe 文件路径：${exePath.value}")


        }
    }
}

//fun requestAdminPrivileges(javaClass: Class<out Any>) {
//    val runtime = Runtime.getRuntime()
//    try {
//        runtime.exec("powershell -Command \"Start-Process -FilePath '${javaClass.protectionDomain.codeSource.location.toURI()}' -Verb RunAs\"")
//    } catch (e: IOException) {
//        e.printStackTrace()
//    }
//}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        this.window.isMinimized = true
        App()
    }
}


fun getExePath(): String {
    val currentDir = System.getProperty("user.dir") // 获取当前应用程序所在的目录路径
    val exeFileName = "autofilter.exe" // 你的 .exe 文件名（包括扩展名）
    val exeFile = File(currentDir, exeFileName)
    println(currentDir)
    return exeFile.absolutePath
}


fun changeFolder(sourceFolderPath: String, destinationFolderPath: String, migrationStatus: MutableList<String>?) {

    val sourceFolder = File(sourceFolderPath)
    val destinationFolder = File(destinationFolderPath)

    if (!sourceFolder.exists()) {
        println("Source folder doesn't exist.")
        migrationStatus?.add("未找到源文件夹")
        return
    }

    if (!destinationFolder.exists()) {
        destinationFolder.mkdirs()
    }

    val files = sourceFolder.listFiles() ?: return
    var migratedCount = 0
    val totalCount = files.size
    if (totalCount <= 0) {
        migrationStatus?.add("无文件")
        return
    }
    for (file in files) {
        val fileName = file.name
        val separatorIndex = fileName.indexOf("_")
        if (separatorIndex != -1) {
            val folderName = fileName.substring(0, separatorIndex)
            val destinationFolderName = File(destinationFolderPath, folderName)

            if (!destinationFolderName.exists()) {
                destinationFolderName.mkdirs()
            }

            val destinationFile = getUniqueFileName(destinationFolderName, fileName)
            file.renameTo(destinationFile)
            migratedCount++

            val progress = "$migratedCount/$totalCount 已迁移"
            migrationStatus?.add(progress)
        }
    }
}

fun getUniqueFileName(destinationFolder: File, fileName: String): File {
    var uniqueFileName = fileName
    val baseName = fileName.substringBeforeLast("_")
    val extension = getFileExtension(fileName)
    var counter = 1

    while (File(destinationFolder, uniqueFileName).exists()) {
        uniqueFileName = baseName + if (counter > 1) "_重复$counter" else ".$extension"
        counter++
    }

    return File(destinationFolder, uniqueFileName)
}

fun getFileExtension(fileName: String): String {
    val extensionIndex = fileName.lastIndexOf('.')
    return if (extensionIndex != -1) {
        fileName.substring(extensionIndex + 1)
    } else {
        ""
    }
}

fun isStartupEnabled(): Boolean {
    val userPrefs = Preferences.userRoot()
    val keyName = "AutofilterStartup"
    return userPrefs.getBoolean(keyName, false)
}


fun readInputStream(inputStream: InputStream): String {
    val reader = BufferedReader(InputStreamReader(inputStream))
    val stringBuilder = StringBuilder()

    try {
        var line: String? = reader.readLine()
        while (line != null) {
            stringBuilder.append(line)
            line = reader.readLine()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    return stringBuilder.toString()
}

fun setStartupEnabled(enabled: Boolean, exePath: String) {
    val userPrefs = Preferences.userRoot()
    val keyName = "AutofilterStartup"
    userPrefs.putBoolean(keyName, enabled)

    val startupFolderPath = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup"
    val shortcutPath = "$startupFolderPath\\autofilter.lnk"

    if (enabled) {

        val linkFile = File(shortcutPath)
        if (linkFile.exists()) {
            linkFile.delete()
        }

        var process: Process? = null;
        val adminShell = java.lang.String.format("cmd /c mklink \"%s\" \"%s\"", shortcutPath, exePath)
        try {
            val processBuilder = ProcessBuilder(adminShell.split(" "))
            processBuilder.redirectErrorStream(true)
            process = processBuilder.start()
            val inputStream = process.inputStream
            val errorStream = process.errorStream

            // 读取输入流和错误流的内容
            val out = readInputStream(inputStream)
            val err = readInputStream(errorStream)

            println(out)
            println(err)

            process.waitFor()
        } catch (e: IOException) {
            e.printStackTrace()
            println("创建桌面快捷方式失败，请确保以管理员权限启动...")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            process?.destroy()
        }


//        val adminShell = java.lang.String.format("cmd /c mklink \"%s\" \"%s\"", shortcutPath, exePath)
//        try {
//            println(adminShell)
//            // 请求管理员权限并执行命令
//            val p = Runtime.getRuntime().exec(adminShell)
//            val out: String = readInputStream(p.inputStream)
//            println(out)
//            val err: String = readInputStream(p.errorStream)
//            println(err)
//        } catch (e: IOException) {
//            e.printStackTrace()
//            println("创建桌面快捷方式失败,请确保以管理员权限启动...")
//        }

    } else {
        try {
            Files.deleteIfExists(Path.of(shortcutPath))
        } catch (e: IOException) {
            // 处理文件删除异常
            println("无法删除文件：$shortcutPath")
            e.printStackTrace()
        }
    }
}

open class FileMigrationService(
    private val sourceFolderPath: String, private val destinationFolderPath: String
) {

    private var timer: Timer? = null

    fun start() {
        timer = Timer()
        timer?.schedule(0, 1000) { // 每隔5秒检查一次
            changeFolder(sourceFolderPath, destinationFolderPath, null)
        }
    }

    fun stop() {
        timer?.cancel()
        timer = null
    }

}