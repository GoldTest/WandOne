package page.ai

import func.getPrefValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PromptService(private val database: Database) {
    private val _promptFlow = MutableStateFlow<MutableList<Prompt>>(mutableListOf())
    val promptFlow = _promptFlow.asStateFlow()

    init {
        transaction(database) {
            SchemaUtils.create(_Prompt)
        }
        update()
    }

    val currentPrompt
        get() =
            _promptFlow.value.find {
                it.workSpace == getPrefValue(
                    "aiapi",
                    "tongyi"
                ) && it.active
            }?.prompt

    fun update() {
        _promptFlow.value = transaction { _Prompt.selectAll().map { toPrompt(it) }.toMutableList() }
    }

    object _Prompt : Table() {
        val id = integer("id").autoIncrement()
        val workSpace = varchar("workSpace", 255)
        val active = bool("active")
        val title = text("title") //空格分隔的key字段列表
        val prompt = text("prompt") //空格分隔的key字段列表

        override val primaryKey = PrimaryKey(id)
    }

    fun createPrompt(input: Prompt) {
        transaction {
            _Prompt.insert {
                it[workSpace] = input.workSpace
                it[active] = input.active
                it[title] = input.title ?: ""
                it[prompt] = input.prompt
            }
        }
        update()
    }

    fun removePrompt(input: Prompt) {
        transaction {
            _Prompt.deleteWhere { id.eq(input.id) }
        }
        update()
    }

    fun updatePrompt(input: Prompt) {
        transaction {
            _Prompt.update(where = {
                _Prompt.id eq input.id
            }) {
                it[workSpace] = input.workSpace
                it[active] = input.active
                it[title] = input.title ?: ""
                it[prompt] = input.prompt
            }
        }
        update()
    }


    private fun toPrompt(row: ResultRow): Prompt {
        return Prompt(
            id = row[_Prompt.id],
            workSpace = row[_Prompt.workSpace],
            active = row[_Prompt.active],
            title = row[_Prompt.title],
            prompt = row[_Prompt.prompt],
        )
    }

    fun setActive(active: Boolean, input: Prompt) {
        if (active) {
            transaction {
                _Prompt.update(where = { _Prompt.workSpace eq input.workSpace }) {
                    it[_Prompt.active] = active.not()
                }
                _Prompt.update(where = {
                    _Prompt.id eq input.id
                }) {
                    it[_Prompt.active] = active
                }
            }
            update()
        }
    }
}


@Serializable
data class Prompt(
    var id: Int = 0,
    val workSpace: String = "",
    val active: Boolean = false,
    val title: String? = "",
    val prompt: String = "",
)