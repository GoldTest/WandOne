import androidx.compose.runtime.*
import func.getPrefValue

object APPViewModel {
    var isVisible = mutableStateOf(getPrefValue("hideAfterLaunch").not())
}