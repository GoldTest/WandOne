package page.ai.page

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import func.getPrefJson
import func.setPrefJson
import page.ai.GEMINI
import page.ai.TONGYI
import page.ai.XAI
import view.ColumnGap
import view.RowGap
import view.detectRightClick

@Composable
fun paramPage() {
    var page by remember { mutableStateOf(TONGYI) }
    val pages = listOf(
        TONGYI to "通义千问",
        XAI to "X.AI",
        GEMINI to "Gemini"
    )

    Row {
        pages.forEachIndexed { index, (pageId, text) ->
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (page == pageId) MaterialTheme.colors.primary else Color.White
                ),
                onClick = { page = pageId }
            ) {
                Text(text)
            }
            RowGap()
        }
    }
    val tongyiPre = listOf(
        "qwen-turbo",
        "bailian-v1",
        "dolly-12b-v2",
        "qwen-plus",
        "qwen-max",
        "qwen-long",
        "qwen-coder-plus",
        "qwen2.5-72b-instruct",
        "qwen2-72b-instruct",
        "qwen1.5-110b-chat",
        "qwen-72b-chat",
        "baichuan2-turbo",
        "chatglm3-6b",
        "yi-large-rag",
        "abab6.5t-chat",
    )
    val xAIPre = listOf(
        "grok-beta",
        "grok-vision-beta",
    )
    val geminiPre = listOf(
        "gemini-1.5-pro",
        "gemini-1.5-flash",
        "gemini-1.5-flash-8b",
        "gemini-exp-1121",
        "learnlm-1.5-experimental-1121",
        "gemma-2-2b-it",
        "gemma-2-9b-it",
        "gemma-2-27b-it",
    )
    when (page) {
        TONGYI -> configPage(page, tongyiPre)
        XAI -> configPage(page, xAIPre)
        GEMINI -> configPage(page, geminiPre)
    }
}

@Composable
fun configPage(page: String, someModels: List<String>) {
    Column {
        var param by remember { mutableStateOf(getPrefJson(page) ?: emptyMap()) }
        var model by remember { mutableStateOf(param["model"].toString()) }
        var preModels by remember {
            mutableStateOf(
                ((param["preModels"] ?: emptyList<String>()) as List<*>).map { it.toString() }
            )
        }
        var enableSearch by remember { mutableStateOf(param["enableSearch"] ?: false) }

        ColumnGap()
        Text("param $param")
        ColumnGap()
        Text("设置模型：$model")
        ColumnGap()
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(model, onValueChange = { it ->
                model = it
            })
            RowGap()
            OutlinedButton(onClick = {
                setPrefJson(page, mapOf("model" to model))
                if (preModels.contains(model).not()) {
                    val newPreModels = preModels.toMutableList()
                    newPreModels.add(model)
                    setPrefJson(page, mapOf("preModels" to newPreModels))
                    preModels = newPreModels
                }
                getPrefJson(page)?.run {
                    param = this
                }
            }) {
                Text("保存")
            }
        }


        if (preModels.isEmpty()) {
            setPrefJson(page, mapOf("preModels" to someModels))
            getPrefJson(page)?.run {
                preModels = ((param["preModels"] ?: emptyList<String>()) as List<*>).map { it.toString() }
                param = this
            }
        }

        ColumnGap()
        val columns = 5
        Column {
            for (i in preModels.indices step columns) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (j in i until minOf(i + columns, preModels.size)) {
                        val modelName = preModels[j]
                        Button(
                            modifier = Modifier.detectRightClick() {
                                if (preModels.contains(modelName)) {
                                    val newPreModels = preModels.toMutableList()
                                    newPreModels.remove(modelName)
                                    setPrefJson(page, mapOf("preModels" to newPreModels))
                                    preModels = newPreModels
                                }
                                getPrefJson(page)?.run {
                                    param = this
                                }
                            },
                            onClick = { model = modelName },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                        ) {
                            Text(modelName)
                        }
                        RowGap()
                    }
                }
            }
        }
        RowGap()
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "启用搜索")
            Spacer(modifier = Modifier.width(4.dp))
            Checkbox(
                modifier = Modifier.size(24.dp),
                checked = enableSearch as Boolean,
                onCheckedChange = { isChecked ->
                    enableSearch = isChecked
                    setPrefJson(page, mapOf("enableSearch" to enableSearch))
                    getPrefJson(page)?.run {
                        param = this
                    }
                })
        }
    }
}