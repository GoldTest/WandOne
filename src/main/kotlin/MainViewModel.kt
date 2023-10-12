import androidx.compose.runtime.mutableStateOf

class MainViewModel {
    var migrateState = mutableStateOf("手动迁移")
    var serviceState = mutableStateOf("启动服务")
    var serviceRunningState = mutableStateOf(false)
    val fileMigrationService = FileMigrationService(SOURCE_FOLDER, DEST_FOLDER, this)
}