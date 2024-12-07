package view

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.customScrollable(
    scrollState: LazyListState,
    coroutineScope: CoroutineScope,
): Modifier {

    return this.draggable(
        orientation = Orientation.Vertical,
        state = rememberDraggableState { delta ->
            coroutineScope.launch {
                scrollState.scrollBy(-delta)
            }
        },
    ).onPointerEvent(PointerEventType.Scroll) {
        coroutineScope.launch {
            scrollState.scrollBy(it.changes.first().scrollDelta.y * 300f)
        }
    }
}

@Composable
fun ColumnGap(gap: Dp = 12.dp) {
    Spacer(Modifier.height(gap))
}

@Composable
fun RowGap(gap: Dp = 12.dp) {
    Spacer(Modifier.width(gap))
}

fun commonMarkdownColors() {}
fun commonMarkdownTypography() {}

fun Modifier.detectRightClick(onRightClick: (Offset) -> Unit): Modifier {
    return pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Press &&
                    event.buttons.isSecondaryPressed
                ) {
                    onRightClick(event.changes.first().position)
                }
            }
        }
    }
}

fun Modifier.visible(visible: Boolean): Modifier =
    this.then(if (visible) Modifier else Modifier.alpha(0f))