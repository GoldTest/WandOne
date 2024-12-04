package page.setting

import APPViewModel.keyService
import APPViewModel.windowState
import APP_WINDOW_HEIGHT
import APP_WINDOW_HEIGHT_WMODE
import APP_WINDOW_WIDTH
import APP_WINDOW_WIDTH_WMODE
import PAGE_END
import PAGE_START
import TAB_SETTINGS
import SPACER_HEIGHT_12
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowPosition
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import func.*
import kotlinx.coroutines.launch
import model.SharedInstance.scope
import model.ToastViewModel.snack
import page.setting.struct.ApiKey
import view.ColumnGap
import view.RowGap


object SettingTab : Tab {
    private fun readResolve(): Any = SettingTab
    var index: UShort = 0u
    fun SettingTab(index: Int): SettingTab {
        this.index = index.toUShort()
        return SettingTab
    }

    override val options: TabOptions
        @Composable get() {
            val title = TAB_SETTINGS
            val icon = painterResource("icons/setting.svg")
            return remember {
                TabOptions(
                    index = index, title = title, icon = icon
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
                modifier = Modifier.size(24.dp), checked = startupEnabled.value, onCheckedChange = { isChecked ->
                    if (hasAdminPermission) run {
                        startupEnabled.value = isChecked
                        setStartupEnabled(isChecked, getCurrentApplicationPath())
                    }
                }, colors = unClickableCheckboxColor
            )
            if (!hasAdminPermission) Text(
                "(未获得管理员权限)", style = TextStyle(
                    color = Color.Gray, fontSize = 13.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(SPACER_HEIGHT_12))

        val hide = remember { mutableStateOf(getPrefValue("hideAfterLaunch", false)) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "启动后隐藏窗口")
            Spacer(modifier = Modifier.width(4.dp))
            Checkbox(modifier = Modifier.size(24.dp), checked = hide.value, onCheckedChange = { isChecked ->
                run {
                    hide.value = isChecked
                    setPrefValue("hideAfterLaunch", isChecked)
                }
            })
        }

        ColumnGap()
        val wideMode = remember { mutableStateOf(getPrefValue("wideMode", false)) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "宽体模式")
            Spacer(modifier = Modifier.width(4.dp))
            Checkbox(modifier = Modifier.size(24.dp), checked = wideMode.value, onCheckedChange = { isChecked ->
                run {
                    wideMode.value = isChecked
                    if (wideMode.value) {
                        windowState.value.size = DpSize(APP_WINDOW_WIDTH_WMODE, APP_WINDOW_HEIGHT_WMODE)
                        windowState.value.position = WindowPosition(Alignment.Center)
                    } else windowState.value.size = DpSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT)
                    setPrefValue("wideMode", isChecked)
                }
            })
        }

        ColumnGap()
        val webEnable = remember { mutableStateOf(getPrefValue("webEnable", false)) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "启动浏览器")
            Spacer(modifier = Modifier.width(4.dp))
            Checkbox(modifier = Modifier.size(24.dp), checked = webEnable.value, onCheckedChange = { isChecked ->
                run {
                    webEnable.value = isChecked
                    APPViewModel.webEnable.value = true
                    setPrefValue("webEnable", isChecked)
                }
            })
        }

        ColumnGap()

        val clipboard = LocalClipboardManager.current
        Row(verticalAlignment = Alignment.CenterVertically) {
            ClickableText(pathText, onClick = {
                clipboard.setText(realPath)
                scope.launch {
                    snack.value.showSnackbar("复制成功", "知道了")
                }
            })
            Text(
                "(点击复制)", style = TextStyle(
                    color = Color.Gray, fontSize = 13.sp
                )
            )
        }

        ColumnGap()

        //keys
        val showKeys = remember { mutableStateOf(false) }
        Text("API  KEYS 设置", style = TextStyle(color = Color.Blue), modifier = Modifier.clickable {
            showKeys.value = showKeys.value.not()
        })
        val keyList = keyService.keyFlow.collectAsState()
        if (showKeys.value) keySetting(keyList)
    }
}

@Composable
fun keySetting(keyList: State<MutableList<ApiKey>>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val key = remember { mutableStateOf("") }
        TextField(value = key.value, onValueChange = {
            key.value = it
        }, label = {
            Text("key workspace")
        })
        RowGap()
        Button({
            val apiKey = ApiKey(workSpace = key.value, defaultKey = "", keys = mutableListOf())
            if (apiKey.workSpace.isNotBlank())
                keyService.createApiKey(apiKey)
        }) {
            Text("保存")
        }
    }
    if (keyList.value.isNotEmpty())
        keyList.value.forEach {
            keyAdd(it, delete = { key ->
                keyService.removeApiKey(key)
            })
        }
}


@Composable
fun keyAdd(workKey: ApiKey, delete: (key: ApiKey) -> Unit) {
    val showEdit = remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(workKey.workSpace)
        RowGap()
        Button({
            showEdit.value = showEdit.value.not()
        }) {
            Text("编辑")
        }
        RowGap()
        Button({
            delete(workKey)
        }) {
            Text("刪除")
        }
    }
    val key = remember { mutableStateOf("") }
    if (showEdit.value) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(value = key.value, onValueChange = {
                key.value = it
            }, label = {
                Text("输入${workKey.workSpace} key")
            })
            RowGap()
            Button({
                if (workKey.workSpace.isNotBlank()) {
                    workKey.keys.add(key.value + " ")
                    keyService.updateApiKey(workKey)
                }

            }) {
                Text("保存")
            }
        }
    }
    val keys = remember { mutableStateListOf<String>() }
    //这里只监听了引用哦，所以直接更新keys就好了
    LaunchedEffect(workKey.keys) {
        keys.clear()
        keys.addAll(workKey.keys)
    }
    LazyColumn {
        itemsIndexed(keys) { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = workKey.defaultKey.trim() == item.trim(), onClick = {
                    if (workKey.workSpace.isNotBlank()) {
                        val apiKey =
                            ApiKey(workSpace = workKey.workSpace, defaultKey = item.trim(), keys = workKey.keys)
                        keyService.updateApiKey(apiKey)
                    }
                })
                RowGap(8.dp)
                Text(
                    item,
                    style = TextStyle(color = if (workKey.defaultKey.trim() == item.trim()) Color.Blue else Color.Unspecified)
                )
                RowGap(8.dp)
                Button(onClick = {
                    if (workKey.workSpace.isNotBlank()) {
                        workKey.keys.remove(item)
                        keys.remove(item)
                        keyService.updateApiKey(workKey)
                    }
                }) {
                    Text("删除")
                }
            }
        }
    }
}