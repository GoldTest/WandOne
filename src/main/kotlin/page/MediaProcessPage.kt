package page

import MEDIA_MERGE
import PAGE_END
import PAGE_START
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions


object MediaProcessPage : Tab {
    private fun readResolve(): Any = MediaProcessPage

    override val options: TabOptions
        @Composable
        get() {
            val title = MEDIA_MERGE
            val icon = painterResource("icons/mergeTwo.svg")

            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        MediaProcessPage()
    }
}

@Composable
@Preview
fun MediaProcessPage() {
    Column(
        modifier = Modifier.padding(start = PAGE_START, end = PAGE_END),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text("MediaProcess Page")
    }

}