package page.tools

import PAGE_END
import PAGE_START
import TAB_TOOLS
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import view.ColumnGap


object ToolsTab : Tab {
    private fun readResolve(): Any = ToolsTab
    var index: UShort = 0u
    fun ToolsTab(index: Int): ToolsTab {
        this.index = index.toUShort()
        return ToolsTab
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = TAB_TOOLS
            val icon = painterResource("icons/bolt.svg")
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
        ToolsPage()
    }
}

@Preview
@Composable
fun ToolsPage() {

    Column(
        modifier = Modifier.padding(start = PAGE_START, top = 8.dp, end = PAGE_END),
    ) {

        //番茄
        Tomato()
        ColumnGap()
        //卡路里
//        Calorie()
//        ColumnGap()

    }
}



















