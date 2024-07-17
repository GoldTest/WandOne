package page.setting

import PAGE_END
import PAGE_START
import TAB_SETTINGS
import SPACER_HEIGHT_12
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import func.*
import kotlinx.coroutines.launch
import model.SharedInstance.scope
import model.ToastViewModel.snack
import view.ColumnGap


object SettingTab : Tab {
    private fun readResolve(): Any = SettingTab
    override val options: TabOptions
        @Composable
        get() {
            val title = TAB_SETTINGS
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
        SettingPage()
    }
}

@Composable
@Preview
fun SettingPage() {

    val startupEnabled = remember { mutableStateOf(isStartupEnabled()) }
    val exePath = remember { mutableStateOf(getCurrentApplicationPath()) }


    val pathText = AnnotatedString("当前 .exe 文件路径：\n${exePath.value}")
    val realPath = AnnotatedString(exePath.value)

    val hasAdminPermission = hasAdminPermission()
    val unClickableCheckboxColor = if (!hasAdminPermission) CheckboxDefaults.colors(
        checkmarkColor = Color.Gray,
        disabledColor = Color.Gray,
        checkedColor = Color.LightGray,
        uncheckedColor = Color.Gray,

        ) else CheckboxDefaults.colors()

    val textLineThrough = buildAnnotatedString {
        withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
            append("开机自启动")
        }
    }
    val textNormal = buildAnnotatedString {
        append("开机自启动")
    }


    Column(
        modifier = Modifier.padding(start = PAGE_START, end = PAGE_END),
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = if (hasAdminPermission) textNormal else textLineThrough)
            Spacer(modifier = Modifier.width(4.dp))
            Checkbox(
                modifier = Modifier.size(24.dp),
                checked = startupEnabled.value,
                onCheckedChange = { isChecked ->
                    if (hasAdminPermission) run {
                        startupEnabled.value = isChecked
                        setStartupEnabled(isChecked, getCurrentApplicationPath())
                    }
                },
                colors = unClickableCheckboxColor
            )
            if (!hasAdminPermission) Text(
                "(未获得管理员权限)", style = TextStyle(
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(SPACER_HEIGHT_12))


        val hide = remember { mutableStateOf(getPrefValue("hideAfterLaunch")) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "启动后隐藏窗口")
            Spacer(modifier = Modifier.width(4.dp))
            Checkbox(
                modifier = Modifier.size(24.dp),
                checked = hide.value,
                onCheckedChange = { isChecked ->
                    run {
                        hide.value = isChecked
                        setPrefValue("hideAfterLaunch", isChecked)
                    }
                }
            )
        }


        ColumnGap(12.dp)

        val clipboard = LocalClipboardManager.current
        ClickableText(pathText, onClick = {
            clipboard.setText(realPath)
            scope.launch {
                snack.value.showSnackbar("复制成功", "知道了")
            }
        })
        Text(
            "(点击复制)", style = TextStyle(
                color = Color.Gray,
                fontSize = 13.sp
            )
        )


    }
}