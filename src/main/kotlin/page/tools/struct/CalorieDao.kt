package page.tools.struct

import kotlin.math.ceil

fun baseCalorie(weight: Float): Int {
    return ceil(((weight * 48.5 + 2954.7) / 4.184)).toInt()
}