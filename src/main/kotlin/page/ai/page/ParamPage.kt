package page.ai.page

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import func.getPrefJson
import func.getPrefValue
import func.setPrefJson
import page.ai.GEMINI
import page.ai.TONGYI
import page.ai.XAI
import view.ColumnGap
import view.RowGap

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

    when (page) {
        TONGYI -> tongyiConfig(page)
        XAI -> Text(XAI)
        GEMINI -> Text(GEMINI)
    }
}

@Composable
fun tongyiConfig(page: String) {
    Column {
        var param by remember { mutableStateOf(getPrefJson(page) ?: emptyMap()) }
        Text("param $param")
        ColumnGap()
        var model by remember { mutableStateOf(param["model"].toString() ?: "") }
        Text("设置模型：$model")
        ColumnGap()
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(model, onValueChange = { it ->
                model = it
            })
            RowGap()
            OutlinedButton(onClick = {
                setPrefJson(page, mapOf("model" to model))
                getPrefJson(page)?.run {
                    param = this
                }
            }) {
                Text("保存")
            }
        }
        val models = listOf(
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

        ColumnGap()
        val columns = 5
        Column {
            for (i in models.indices step columns) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (j in i until minOf(i + columns, models.size)) {
                        val modelName = models[j]
                        Button(
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

        RowGap(

        )
        var enableSearch by remember { mutableStateOf(getPrefValue("enableSearch", false)) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "启用搜索")
            Spacer(modifier = Modifier.width(4.dp))
            Checkbox(modifier = Modifier.size(24.dp), checked = enableSearch, onCheckedChange = { isChecked ->
                run {
                    enableSearch = isChecked
                    setPrefJson(page, mapOf("enableSearch" to enableSearch))
                    getPrefJson(page)?.run {
                        param = this
                    }
                }
            })
        }
    }
}