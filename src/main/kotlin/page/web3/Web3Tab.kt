package page.web3

import TAB_Web3
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions


object Web3Tab : Tab {
    private fun readResolve(): Any = Web3Tab
    override val options: TabOptions
        @Composable
        get() {
            val title = TAB_Web3
            val icon = painterResource("icons/settingInput.svg")
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
        web3Page()
    }
}

@Composable
@Preview
fun web3Page() {
    Column {

    }
}