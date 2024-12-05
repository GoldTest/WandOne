package page.setting.struct

import func.getPrefValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class KeysService(private val database: Database) {
    private val _keyFlow = MutableStateFlow<MutableList<ApiKey>>(mutableListOf())
    val keyFlow = _keyFlow.asStateFlow()

    init {
        transaction(database) {
            SchemaUtils.create(_Key)
        }
        update()
    }

    val defaultKey
        get() =
            _keyFlow.value.find {
                it.workSpace == getPrefValue(
                    "aiapi",
                    "tongyi"
                ) && it.defaultKey.isNotBlank()
            }?.defaultKey

    fun update() {
        _keyFlow.value = transaction { _Key.selectAll().map { toApiKey(it) }.toMutableList() }
    }

    object _Key : Table() {
        val id = integer("id").autoIncrement()
        val workSpace = varchar("workSpace", 255)
        val defaultKey = varchar("defaultKey", 255)
        val keys = text("keys") //空格分隔的key字段列表

        override val primaryKey = PrimaryKey(id)
    }

    fun createApiKey(apiKey: ApiKey) {
        transaction {
            _Key.insert {
                it[workSpace] = apiKey.workSpace
                it[defaultKey] = apiKey.defaultKey
                it[keys] = apiKey.keys.joinToString(" ")
            }
        }
        update()
    }

    fun removeApiKey(apiKey: ApiKey) {
        transaction {
            _Key.deleteWhere { workSpace.eq(apiKey.workSpace) }

        }
        update()
    }

    fun updateApiKey(key: ApiKey) {
        transaction {
            _Key.update(where = {
                _Key.workSpace eq key.workSpace
            }) {
                it[workSpace] = key.workSpace
                it[defaultKey] = key.defaultKey
                it[keys] = key.keys.joinToString(" ")
            }
        }
        update()
    }


    private fun toApiKey(row: ResultRow): ApiKey {
        return ApiKey(
            workSpace = row[_Key.workSpace],
            defaultKey = row[_Key.defaultKey],
            keys = row[_Key.keys].split(" ").filter { it.isNotBlank() }.toMutableList()
        )
    }
}


@Serializable
data class ApiKey(
    val workSpace: String = "",
    val defaultKey: String = "",
    val keys: MutableList<String>,
)

