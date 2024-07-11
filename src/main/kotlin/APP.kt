import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import model.ToastViewModel
import page.media.MediaProcessTab
import page.pipeline.PipelineTab
import page.setting.SettingTab
import page.tools.ToolsTab

@Preview
@Composable
fun App(viewModel: APPViewModel) {
    val scaffoldState = rememberScaffoldState()

    TabNavigator(ToolsTab) { navigator ->
        Scaffold(
            scaffoldState = scaffoldState,
            content = {
                CurrentTab()
            },
            topBar = {
                BottomNavigation() {
                    TabNavigationItem(ToolsTab)
                    TabNavigationItem(PipelineTab)
                    TabNavigationItem(MediaProcessTab)
                    TabNavigationItem(SettingTab)
                }
            },
            floatingActionButton = {
                when (navigator.current.options.title) {
                    TAB_PIPELINE -> {
                        FloatingActionButton(
                            onClick = {
                                (navigator.current as PipelineTab).onFabClicked()
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
        )

        val state = ToastViewModel
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            SnackbarHost(
                modifier = Modifier.fillMaxWidth(),
                hostState = state.snack.value
            )
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    BottomNavigationItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        label = { Text(tab.options.title) },
        alwaysShowLabel = false,
        icon = { tab.options.icon?.let { Icon(painter = it, contentDescription = tab.options.title) } }
    )
}

interface FabAction {
    fun onFabClicked()
}

















