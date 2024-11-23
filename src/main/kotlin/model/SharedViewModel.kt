package model

import DEFAULT_DOUYIN_DEST_FOLDER
import DEFAULT_DOUYIN_SOURCE_FOLDER
import FILE_MIGRATE_MANUAL
import FILE_MIGRATE_START_SERVICE
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import service.file.FileMigrationService

object FileMigrateViewModel {
    var migrateState = mutableStateOf(FILE_MIGRATE_MANUAL)
    var serviceState = mutableStateOf(FILE_MIGRATE_START_SERVICE)
    var serviceRunningState = mutableStateOf(false)
}

object ToastViewModel {
    val snack = mutableStateOf(SnackbarHostState())
}

object SharedInstance {
    val scope = CoroutineScope(Dispatchers.IO + Job())
    val json = Json
    val gson by lazy { Gson() }
    val classLoader = SharedInstance.javaClass.classLoader
}
