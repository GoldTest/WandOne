package page.tools

import PAGE_END
import PAGE_START
import TAB_TOOLS
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions


object ToolsTab : Tab {
    private fun readResolve(): Any = ToolsTab
    override val options: TabOptions
        @Composable
        get() {
            val title = TAB_TOOLS
            val icon = painterResource("icons/bolt.svg")
            return remember {
                TabOptions(
                    index = 2u,
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

@Composable
@Preview
fun ToolsPage() {

    Column(
        modifier = Modifier.padding(start = PAGE_START, end = PAGE_END),
    ) {
        Button(
            enabled = true,
            onClick = {
                //点击监听
            },
            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            Text(
                text = "小工具"
            )
        }
    }
}