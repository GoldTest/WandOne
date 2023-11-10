package service.file

import APPViewModel
import func.commonChangeFolder
import java.util.*
import kotlin.concurrent.schedule

open class FileMigrationService(
    private val sourceFolderPath: String,
    private val destinationFolderPath: String,
    private val viewModel: APPViewModel
) {

    private var timer: Timer? = null

    fun start() {
        if (viewModel.serviceRunningState.value) return
        timer = Timer()
        timer?.schedule(0, 10000) {//gap 10s
            commonChangeFolder(sourceFolderPath, destinationFolderPath, null)
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