import androidx.compose.runtime.mutableStateOf
import constant.DEFAULT_DOUYIN_DEST_FOLDER
import constant.DEFAULT_DOUYIN_SOURCE_FOLDER
import service.file.FileMigrationService

class APPViewModel {
    var migrateState = mutableStateOf("手动迁移")
    var serviceState = mutableStateOf("启动服务")
    var serviceRunningState = mutableStateOf(false)
    val fileMigrationService = FileMigrationService(DEFAULT_DOUYIN_SOURCE_FOLDER, DEFAULT_DOUYIN_DEST_FOLDER, this)
}