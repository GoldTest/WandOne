import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.tab.*
import model.ToastViewModel
import page.media.MediaProcessTab
import page.pipeline.PipelineTab
import page.setting.SettingTab
import page.tools.ToolsTab

@OptIn(ExperimentalVoyagerApi::class)
@Preview
@Composable
fun App(viewModel: APPViewModel) {
    val scaffoldState = rememberScaffoldState()

    TabNavigator(PipelineTab) { navigator ->
        Scaffold(
            scaffoldState = scaffoldState,
            content = {
                CurrentTab()
            },
            topBar = {
                BottomNavigation(
                    backgroundColor = MaterialTheme.colors.primarySurface,
                ) {
                    TabNavigationItem(PipelineTab)
                    TabNavigationItem(MediaProcessTab)
                    TabNavigationItem(ToolsTab)
                    TabNavigationItem(SettingTab)
                }
            },
//            floatingActionButton = {
//                FloatingActionButton(
//                    onClick = { /* Handle FAB click */ }
//                ) {
//                    Icon(Icons.Default.Favorite, contentDescription = null)
//                }
//            }
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

















