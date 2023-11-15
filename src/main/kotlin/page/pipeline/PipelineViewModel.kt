package page.pipeline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import model.*
import model.Database.database

object PipeLineViewModel {
    val pipelineService = PipelineService(database)
    val pipelines = pipelineService.pipelineFlow
}

object CreateNodes {
    val inputNodes = mutableStateListOf<InputNode>()
    val processNodes = mutableStateListOf<ProcessNode>()
    val currentPipeline = mutableStateOf(Pipeline(inputs = inputNodes, nodes = processNodes))
}

val defaultBiliNodes = mutableListOf<ProcessNode>()

