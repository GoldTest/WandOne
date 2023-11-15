package page.pipeline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import pipeline.*

object PipeLineViewModel {
    val pipelines = mutableStateListOf<Pipeline>()
}

object CreateNodes {
    val inputNode = mutableStateOf(InputMultiFolderNode())
    val processNodes = mutableStateListOf<ProcessNode>()
    val currentPipeline = mutableStateOf(Pipeline(input = inputNode.value, nodes = processNodes))
}

val defaultBiliNodes = mutableListOf<ProcessNode>()

