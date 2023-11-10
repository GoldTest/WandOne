package page

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import func.getCurrentApplicationPath
import func.isStartupEnabled
import func.setStartupEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
@Preview
fun SettingPage() {

    val startupEnabled = remember { mutableStateOf(isStartupEnabled()) }
    val exePath = remember { mutableStateOf(getCurrentApplicationPath()) }
    val pathText = AnnotatedString("当前 .exe 文件路径：\n${exePath.value}")
    val realPath = AnnotatedString(exePath.value)

    Column(
        modifier = Modifier.padding(start = 10.dp, top = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(text = "跟随系统启动，需要管理员权限")
            Spacer(modifier = Modifier.width(8.dp))
            Checkbox(checked = startupEnabled.value, onCheckedChange = { isChecked ->
                run {
                    startupEnabled.value = isChecked
                    setStartupEnabled(isChecked, getCurrentApplicationPath())
                }
            })
        }
        val clipboard = LocalClipboardManager.current
        ClickableText(pathText, onClick = {
            CoroutineScope(Dispatchers.Main).launch{
                clipboard.setText(realPath)
            }
        })
    }

}