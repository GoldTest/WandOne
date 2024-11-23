package page.web3.henge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun autoHenge(page: Int) {
    Column(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        var allMoney by remember { mutableStateOf(0) }
        Text("总金额 $allMoney usd")
        OutlinedTextField(value = if (allMoney == 0) "" else allMoney.toString(), onValueChange = {
            if (it.matches(Regex("[0-9]*"))) {
                allMoney = it.toIntOrNull() ?: 0
            }
        })

        Row {
            Text("总收益")
        }

        val priceMap = remember { mutableStateListOf(0) }

        priceMap.forEach { currentPrice ->
            Row {
                var bias by remember { mutableStateOf(false) }
                Text("价格 $currentPrice")
                Button(onClick = {
                    //todo map?
                }) {
                    Text("额度")
                }
                Checkbox(checked = bias, onCheckedChange = { bias = it })
                Button(onClick = {
                    priceMap.remove(currentPrice)
                }) {
                    Text("删除")
                }
            }
        }

        Row {
            var price by remember { mutableStateOf(0) }
            OutlinedTextField(value = if (price == 0) "" else price.toString(), onValueChange = {
                if (it.matches(Regex("[0-9]*"))) {
                    price = it.toIntOrNull() ?: 0
                }
            })
            Button(onClick = {
                priceMap.add(price)
            }) {
                Text("增加选项")
            }
        }
    }
}