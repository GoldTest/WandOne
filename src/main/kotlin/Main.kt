import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import java.util.*
import kotlin.concurrent.schedule


@Composable
@Preview
fun App() {
    var migrate by remember { mutableStateOf("手动迁移") }
    var service by remember { mutableStateOf("启动服务") }
    val migrationStatus = remember { mutableStateListOf<String>() }
    val SOURCE_FOLDER = "D:/STOP"
    val DEST_FOLDER = "E:/抖音归档"

    MaterialTheme {
        Column {
            Button(onClick = {
                migrationStatus.clear()
                changeFolder(SOURCE_FOLDER, DEST_FOLDER, migrationStatus)
                migrate = "已点击"
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
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
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
    if (totalCount <= 0) migrationStatus?.add("无文件")
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


open class FileMigrationService(
    private val sourceFolderPath: String, private val destinationFolderPath: String
) {

    private var timer: Timer? = null

    fun start() {
        timer = Timer()
        timer?.schedule(0, 5000) { // 每隔5秒检查一次
            changeFolder(sourceFolderPath, destinationFolderPath, null)
        }
    }

    fun stop() {
        timer?.cancel()
        timer = null
    }

}