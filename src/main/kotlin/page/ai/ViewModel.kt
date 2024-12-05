package page.ai

import APPViewModel.globalScope
import APPViewModel.keyService
import APPViewModel.promptService
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import com.alibaba.dashscope.aigc.generation.Generation
import com.alibaba.dashscope.aigc.generation.GenerationParam
import com.alibaba.dashscope.aigc.generation.GenerationResult
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.Role
import func.getPrefJson
import io.reactivex.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow

class AiViewModel {

//    private val _test = MutableStateFlow(mutableListOf<String>())
//    val test: StateFlow<List<String>> = _test.asStateFlow() //暴露展示
//    fun addTest() {
//        _test.update { it.toMutableList().apply { add("加") } }
//    }

//    tokens
//    计算

    val messageList = mutableStateListOf<Message>()

    fun clear() {
        messageList.clear()
    }

    private val gen by lazy { Generation() }

    val systemParam: () -> Message
        get() = {
            val currentPrompt = promptService.currentPrompt ?: "you are a helpful assistance"
            Message.builder()
                .role(Role.SYSTEM.value)
                .content(currentPrompt)
                .build()
        }

    val param: () -> GenerationParam.GenerationParamBuilder<*, *>
        get() = {
            val defaultKey = keyService.defaultKey ?: "null"
            val model = getPrefJson(TONGYI)?.get("model") ?: "qwen-plus"
            val enableSearch = getPrefJson(TONGYI)?.get("enableSearch") ?: false
            GenerationParam.builder()
                .apiKey(defaultKey)
                // 模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                .model(model.toString()) //grok-beta
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .enableSearch(enableSearch as Boolean)
        }
    val userMsg = Message.builder()
        .role(Role.USER.value)
        .build()


    fun singleMessage(input: String): GenerationResult {
        userMsg.content = input
        param().messages(listOf(systemParam(), userMsg))
        return gen.call(param().build())
    }

    fun streamMessage(input: String): Flowable<GenerationResult> {
        userMsg.content = input
        param().messages(listOf(systemParam(), userMsg)).incrementalOutput(true)
        return gen.streamCall(param().build())
    }

    fun streamMessage(input: MutableList<Message>): Flowable<GenerationResult>? {
        return try {
            val msg = mutableListOf<Message>()
            msg.add(systemParam())
            msg.addAll(input)
            val realParam = param().messages(msg).incrementalOutput(true).build()
            gen.streamCall(realParam)
        } catch (e: Exception) {
            Flowable.error(e)
        }
    }

    fun handleInput(
        input: String,
        lastMsg: MutableState<String>,
        stop: MutableState<Boolean>,
    ) {
        if (input.isNotBlank()) {
            val currentInput = Message.builder().role(Role.USER.value).content(input).build()
            messageList.add(currentInput)

            globalScope.launch {
                stop.value = false
                streamMessage(messageList)
                    ?.asFlow()
                    ?.takeWhile { stop.value.not() }
                    ?.flowOn(Dispatchers.IO)
                    ?.catch { e -> lastMsg.value = "Error: ${e.message}" }
                    ?.collect { msg: GenerationResult ->
                        lastMsg.value += msg.output.choices[0].message.content
                    }
                val currentOutput =
                    Message.builder().role(Role.ASSISTANT.value).content(lastMsg.value).build()
                messageList.add(currentOutput)
                lastMsg.value = ""
            }
        }
    }
}
