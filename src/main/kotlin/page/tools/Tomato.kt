package page.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import func.countdown
import func.playAudio
import java.util.concurrent.Executors


@Composable
fun Tomato() {
    /**
     * 1.5S倒计时后自动进入坚持时间
     * 2.2分钟坚持时间后自动进入番茄钟
     * 3.番茄计数器
     * 4.可视化番茄
     */

    val showWindow = remember { mutableStateOf(false) }
    val showPlantation = remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
//        Text("番茄计时器 ")
        Button(
            enabled = true,
            onClick = {
                showWindow.value = !showWindow.value
            },
//            shape = RoundedCornerShape(8.dp, 8.dp, 8.dp, 8.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = lerp(Color.Red, Color.White, 0.5f),
                contentColor = Color.Black
            )
        ) {
            Text(
//                color = Color.Red,
                text = "番茄计时器"
            )
        }
        if (showWindow.value) TomatoPage(showWindow)
        Spacer(Modifier.width(12.dp))
        Button(
            enabled = true,
            onClick = {
                showPlantation.value = !showPlantation.value
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = lerp(Color.Green, Color.White, 0.5f),
                contentColor = Color.Black
            )
        ) {
            Text(text = "种植园")
        }
    }
    if (showPlantation.value) Column {
        Text("种植园来咯")
    }
}

@Composable
fun TomatoPage(showWindow: MutableState<Boolean>) {

    val stage = remember { mutableStateOf("begin") }

    val state = rememberWindowState()
    //todo uncomment
//    state.placement = WindowPlacement.Fullscreen
    if (showWindow.value) Window(
        onCloseRequest = {
            //todo 打断番茄
            showWindow.value = false
        },
        visible = true,
        title = "番茄计时器",
        state = state,
        resizable = false,
    ) {
        MaterialTheme {
            when (stage.value) {
                "begin" -> {
                    val count = remember { mutableStateOf(1) }
                    countdown(5) {
                        count.value = it
                    }
                    TomatoBegin(count)
                    if (count.value == 0) stage.value = "persist"
                }

                "persist" -> {
                    val count = remember { mutableStateOf(1) }
                    countdown(3) {
                        count.value = it
                    }
                    TomatoPersist(count)
                    if (count.value == 0) {
                        val executor = Executors.newSingleThreadExecutor()

                        executor.submit {
                            playAudio("bell-ding.wav")
                        }
//                        playAudio("bell-ding.wav")
                        stage.value = "tomato"
                    }
                }

                "tomato" -> {
                    TomatoLoop()
                }
            }
        }
    }
}

@Composable
fun TomatoBegin(count: MutableState<Int>) {
    Box(Modifier.fillMaxSize().background(color = Color.Green), contentAlignment = Alignment.Center) {
        Text(fontSize = 48.sp, text = "将在 ${count.value} 秒后进入坚持时间")
    }
}

@Composable
fun TomatoPersist(count: MutableState<Int>) {
    Text("将在${count.value} 秒后进入番茄时间")
}

@Composable
fun TomatoLoop(count: MutableState<Int> = mutableStateOf(0)) {
    Text("番茄时间")
}

