package page.pipeline

import PAGE_END
import PAGE_START
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class RecordScreen(
    val recordList: MutableList<String>
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Column(modifier = Modifier.padding(start = PAGE_START, end = PAGE_END)) {
            Button(onClick = {
                if (navigator.canPop) {
                    navigator.pop()
                }
            }) {
                Text("返回")
            }
            LazyColumn {
                this.item(recordList) {
                    Text(this.toString())
                }
            }
        }
    }

}