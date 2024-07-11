package page.tools

import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

@Composable
fun NoFap() {

    val show = remember { mutableStateOf(false) }
    Button(
        enabled = true,
        onClick = {
            show.value = !show.value
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = lerp(Color.Red, Color.White, 0.5f),
            contentColor = Color.Black
        )
    ) {
        Text(
            text = "戒除欲望"
        )
    }
    if (show.value) NoFapPage()

}

@Composable
fun NoFapPage() {

}
