package page.trade

import PAGE_END
import PAGE_START
import TAB_Trade
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import view.RowGap


object TradeTab : Tab {
    private fun readResolve(): Any = TradeTab
    var index: UShort = 0u
    fun TradeTab(index: Int): TradeTab {
        this.index = index.toUShort()
        return TradeTab
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = TAB_Trade
            val icon = painterResource("icons/settingInput.svg")
            return remember {
                TabOptions(
                    index = index,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        tradePage()
    }
}

@Composable
@Preview
fun tradePage() {
    Column(
        modifier = Modifier.padding(start = PAGE_START, end = PAGE_END)
    ) {

        val pages = listOf(
            0 to "对冲",
            1 to "自动介入",
            2 to "some api",
            3 to "交易自动策略",
            4 to "交易套利对冲",
        )
        var page by remember { mutableStateOf(getPrefValue("tradePage", 0)) }

        Row {
            pages.forEachIndexed { index, (pageId, text) ->
                Button(
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (page == pageId) MaterialTheme.colors.primary else Color.White
                    ),
                    onClick = {
                        page = pageId
                        setPrefValue("tradePage", pageId)
                    }
                ) {
                    Text(text)
                }
                RowGap()
            }
        }
        when (page) {
            1 -> Text("1111")
            2 -> Text("22222")
        }
    }
}