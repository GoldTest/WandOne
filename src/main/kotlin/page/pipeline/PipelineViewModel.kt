package page.pipeline

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import page.pipeline.struct.Database.database
import page.pipeline.struct.InputNode
import page.pipeline.struct.PipelineService
import page.pipeline.struct.ProcessNode

object PipeLineViewModel {
    val pipelineService = PipelineService(database)
    val pipelines = pipelineService.pipelineFlow
    //todo 本地维护状态 单向流动数据


    //每个管道都有一个日志列表 记录着一段时间内节点的记录
    val logQueue = arrayListOf<String>()

    //每个管道当前节点，以及当前的处理过程
    val tempLog = mutableStateListOf<String>("")
    val hitLog = mutableStateListOf<String>("")
    val currentNodeDescribe = mutableStateOf("未启动")
    val fabClicked = mutableStateOf(false)
}

object CreateNodes {
    val inputNodes = mutableStateListOf<InputNode>()
    val processNodes = mutableStateListOf<ProcessNode>()
    val currentPipeline = mutableStateOf(page.pipeline.struct.Pipeline(inputs = inputNodes, nodes = processNodes))
}