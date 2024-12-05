package page.web3

import androidx.compose.runtime.mutableStateOf
import func.getPrefValue

class Web3ViewModel {


    val page = mutableStateOf(getPrefValue("web3Page", 0))

}