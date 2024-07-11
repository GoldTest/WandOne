package page.pipeline.struct

import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import model.SharedInstance.json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Database {
    val database by lazy {
        Database.connect(
            url = "jdbc:sqlite:data/pipeline.db",
            user = "root",
            driver = "org.sqlite.JDBC"
        )
    }
}

class PipelineService(private val database: Database) {
    private val _pipelineFlow = MutableStateFlow<MutableList<Pipeline>>(mutableListOf())
    val pipelineFlow = _pipelineFlow.asStateFlow()

    init {
        transaction(database) {
            SchemaUtils.create(_PipeLine)
        }
        updatePipelines()
    }

    object _PipeLine : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", 255)
        val runningState = bool("running_state")
        val input = text("input")
        val nodes = text("nodes")

        override val primaryKey = PrimaryKey(id)
    }


    fun createPipeline(pipeline: Pipeline) {
        transaction {
            _PipeLine.insert {
                it[name] = pipeline.name
                it[runningState] = pipeline.runningState
                it[input] = json.encodeToString(pipeline.inputs)
                it[nodes] = json.encodeToString(pipeline.nodes)
            }
        }
        updatePipelines()
    }

    private fun updatePipelines() {
        _pipelineFlow.value = transaction {
            _PipeLine.selectAll().map { toPipeline(it) }.toMutableList()
        }
    }

    fun updatePipeline(pipeline: Pipeline) {
        transaction {
            _PipeLine.update(where = {
                _PipeLine.id eq pipeline.id
            }) {
                it[name] = pipeline.name
                it[runningState] = pipeline.runningState
                it[input] = json.encodeToString(pipeline.inputs)
                it[nodes] = json.encodeToString(pipeline.nodes)
            }
        }
        updatePipelines()
    }

    fun deletePipeline(id: Int) {
        transaction {
            _PipeLine.deleteWhere { _PipeLine.id.eq(id) }
        }
        updatePipelines()
    }

    private fun toPipeline(row: ResultRow): Pipeline {
        return Pipeline(
            id = row[_PipeLine.id],
            name = row[_PipeLine.name],
            runningState = row[_PipeLine.runningState],
            inputs = json.decodeFromString(row[_PipeLine.input]),
            nodes = json.decodeFromString(row[_PipeLine.nodes])
        )
    }
}


object DefaultPipeline {
    fun addDefaultBiliPipeline() {

    }

    fun addDefaultTikTokPipeline() {

    }
}