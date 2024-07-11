package page.tools.struct

import org.jetbrains.exposed.sql.Database

class TomatoService(val database: Database) {

}

data class TomatoDao(
    val time: Int,
    val stage: Int
)

