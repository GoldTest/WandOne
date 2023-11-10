package func

import java.io.*
import java.nio.file.Files
import java.util.prefs.Preferences


fun getCurrentApplicationPath(): String {
    val currentDir = System.getProperty("user.dir") // 获取当前应用程序所在的目录路径
    val exeFileName = "WandOne.exe" // 你的 .exe 文件名（包括扩展名）
    val exeFile = File(currentDir, exeFileName)
    return exeFile.absolutePath
}

fun commonChangeFolder(sourceFolderPath: String, destinationFolderPath: String, migrationStatus: MutableList<String>?) {

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
    val keyName = "WandOneStartup"
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
    val keyName = "WandOneStartup"
    userPrefs.putBoolean(keyName, enabled)

    val startupFolderPath = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup"
    val shortcutPath = "$startupFolderPath\\WandOne.lnk"

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