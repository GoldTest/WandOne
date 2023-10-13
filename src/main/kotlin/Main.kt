import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.io.*
import java.nio.file.Files

import java.util.*
import java.util.prefs.Preferences
import kotlin.concurrent.schedule
import kotlin.math.min
import kotlin.math.sqrt


val SOURCE_FOLDER = "D:/STOP"
val DEST_FOLDER = "E:/抖音归档"


@Composable
@Preview
fun App(viewModel: MainViewModel) {

    val migrationStatus = remember { mutableStateListOf<String>() }
    val startupEnabled = remember { mutableStateOf(isStartupEnabled()) }
    val exePath = remember { mutableStateOf(getExePath()) }

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
                        changeFolder(SOURCE_FOLDER, DEST_FOLDER, migrationStatus)
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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "跟随系统启动 需要管理员权限")
                Spacer(modifier = Modifier.width(8.dp))
                Checkbox(checked = startupEnabled.value, onCheckedChange = { isChecked ->
                    run {
                        startupEnabled.value = isChecked
                        setStartupEnabled(isChecked, getExePath())
                    }
                })
            }
            Text(text = "当前 .exe 文件路径：\n${exePath.value}")

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

class CustomIconPainter(private val color: Color) : Painter() {
    override val intrinsicSize = Size(Float.MAX_VALUE, Float.MAX_VALUE)

    override fun DrawScope.onDraw() {

        // 绘制图标的样式
        val style: DrawStyle = Stroke(width = 2f, cap = StrokeCap.Round)

        // 设置绘制的颜色
        val color: Color = color

        // 在这里使用 drawXXX() 方法绘制自定义的图标形状
        val path = Path().apply {
            // 绘制圆形
            val centerX = size.minDimension / 2f
            val centerY = size.minDimension / 2f
            val radius = min(centerX, centerY) * 0.9f// 圆形半径为宽高中较小值的一半
            val ovalRect = Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            val startAngle = 0f
            val sweepAngle = 360f
            addArc(ovalRect, startAngle, sweepAngle)

            // 绘制正三角形
            val triangleSize = min(centerX, centerY) * 2f// 正三角形边长为圆形半径的一半
            val triangleHeight = triangleSize * sqrt(3f) / 2f // 正三角形的高度
            val triangleTop = centerY - triangleHeight / 2f // 正三角形的顶部位置
            val triangleBottom = centerY + triangleHeight / 2f // 正三角形的底部位置
            val triangleLeft = centerX - triangleSize / 2f // 正三角形的左边位置
            val triangleRight = centerX + triangleSize / 2f // 正三角形的右边位置
            moveTo(centerX, triangleTop) // 移动到正三角形的顶部
            lineTo(triangleRight, triangleBottom) // 绘制正三角形的右边线
            lineTo(triangleLeft, triangleBottom) // 绘制正三角形的底边线
            close() // 封闭路径
        }


        // 使用 drawPath() 方法绘制图标形状
        drawPath(path = path, color = color, style = style)
    }
}

fun main() = application {
    var isVisible by remember { mutableStateOf(false) }
    val viewModel = MainViewModel()

    val trayState = rememberTrayState()

    val activeIcon = CustomIconPainter(Color.Green)
    val deActiveIcon = CustomIconPainter(Color.Gray)


    val windowState = rememberWindowState()
    windowState.size = DpSize(400.dp, 600.dp)
    windowState.position = WindowPosition(Alignment.Center)

    Window(
        onCloseRequest = { isVisible = false },
        visible = isVisible,
        title = "auto filter",
        state = windowState
    ) {

        val icon = if (viewModel.serviceRunningState.value) {
            activeIcon
        } else {
            deActiveIcon
        }

        Tray(
            state = trayState,
            icon = icon,
            tooltip = "auto filter",
            onAction = {
                isVisible = true
            },
            menu = {
                Item(
                    "start service",
                    enabled = !viewModel.serviceRunningState.value,
                    onClick = {
                        viewModel.fileMigrationService.start()
                        viewModel.serviceState.value = "运行中，点击停止"
                    }
                )

                Item(
                    "close service",
                    enabled = viewModel.serviceRunningState.value,
                    onClick = {
                        viewModel.fileMigrationService.stop()
                        viewModel.serviceState.value = "启动服务"
                    }
                )

                Item(
                    "exit",
                    onClick = {
                        exitApplication()
                    }
                )
            }
        )

        App(viewModel = viewModel)
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
        migrationStatus?.add("无文件需要迁移")
        return
    }
    for (file in files) {
        val fileName = file.name
        if (fileName.endsWith(".crdownload")) continue
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

        var process: Process? = null
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
            Files.deleteIfExists(java.nio.file.Path.of(shortcutPath))
        } catch (e: IOException) {
            // 处理文件删除异常
            println("无法删除文件：$shortcutPath")
            e.printStackTrace()
        }
    }
}

open class FileMigrationService(
    private val sourceFolderPath: String,
    private val destinationFolderPath: String,
    private val viewModel: MainViewModel
) {

    private var timer: Timer? = null

    fun start() {
        if (viewModel.serviceRunningState.value) return
        timer = Timer()
        timer?.schedule(0, 1000) { // 每隔5秒检查一次
            changeFolder(sourceFolderPath, destinationFolderPath, null)
        }
        viewModel.serviceRunningState.value = true
    }

    fun stop() {
        if (!viewModel.serviceRunningState.value) return
        timer?.cancel()
        timer = null
        viewModel.serviceRunningState.value = false
    }

}