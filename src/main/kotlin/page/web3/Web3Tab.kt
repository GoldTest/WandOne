package page.web3

import PAGE_END
import PAGE_START
import TAB_Web3
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import func.getPrefValue
import func.setPrefValue
import page.web3.henge.autoHenge
import view.RowGap


object Web3Tab : Tab {
    private fun readResolve(): Any = Web3Tab
    override val options: TabOptions
        @Composable
        get() {
            val title = TAB_Web3
            val icon = painterResource("icons/settingInput.svg")
            return remember {
                TabOptions(
                    index = 4u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        web3Page()
    }
}

@Composable
@Preview
fun web3Page() {
    Column(
        modifier = Modifier.padding(start = PAGE_START, end = PAGE_END)
    ) {
        Column {
            Text("poly相关")
            Text("自动对冲 1，加权分配，2，倾斜分配")
            Text(
                "auto in策略 分份额 一份一份参与 挂单、可吃单、可吃单数量、比分、 稳定度、稳定值" +
                        "倾斜、倾斜程度、吃单买 挂单买"
            )
            Text("分配份额， 可盈利，盈利值，列表，时间")
        }

        val pages = listOf(
            0 to "对冲",
            1 to "自动介入",
            2 to "some api",
            3 to "交易自动策略",
            4 to "交易套利对冲",
        )
        var page by remember { mutableStateOf(getPrefValue("web3Page", 0)) }

        Row {
            pages.forEachIndexed { index, (pageId, text) ->
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (page == pageId) MaterialTheme.colors.primary else Color.White
                    ),
                    onClick = {
                        page = pageId
                        setPrefValue("web3Page", pageId)
                    }
                ) {
                    Text(text)
                }
                RowGap()
                if (index == 2) Spacer(Modifier.weight(1f))
            }
        }
        when (page) {
            0 -> autoHenge(page)
            1 -> Text("自动介入")
            2 -> Text("api 调试")
        }
    }
}