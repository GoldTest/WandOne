import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import model.ToastViewModel
import page.MediaProcessPage
import page.MigratePage
import page.SettingPage

@Preview
@Composable
fun App(viewModel: APPViewModel) {

    val scaffoldState = rememberScaffoldState()

    TabNavigator(MigratePage) {
        Scaffold(
            scaffoldState = scaffoldState,
            content = {
                CurrentTab()
            },
            topBar = {
                BottomNavigation(
                    backgroundColor = MaterialTheme.colors.primarySurface,
                ) {
                    TabNavigationItem(MigratePage)
                    TabNavigationItem(MediaProcessPage)
                    TabNavigationItem(SettingPage)
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
                hostState = state.snackbarHostState.value
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

//            MainScope().launch {
//                val clipboard = LocalClipboardManager.current
//                clipboard.setText(realPath)
//                Toast("复制成功")
//            }























