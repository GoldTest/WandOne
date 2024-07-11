package page.tools.struct

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import model.SharedInstance
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import page.pipeline.struct.Pipeline
import page.pipeline.struct.PipelineService._PipeLine.autoIncrement
import kotlin.math.ceil

//object CalorieViewModel{
//    val calorieDateBase =
//    val calorieService = CalorieService()
//
//}


class CalorieService(private val database: Database) {


    private val _pipelineFlow = MutableStateFlow<MutableList<Pipeline>>(mutableListOf())
    val pipelineFlow = _pipelineFlow.asStateFlow()

    init {
        transaction(database) {
            SchemaUtils.create(_Calorie)
        }
        updateFlow()
    }

    fun updateFlow() {

    }

    object _Calorie : Table() {
        val id = integer("id").autoIncrement()
        val weight = float("weight")

        override val primaryKey = PrimaryKey(id)
    }


    private fun toCalorieBean(row: ResultRow): CalorieBean {
        return CalorieBean(
            id = row[_Calorie.id],
            weight = row[_Calorie.weight],
            baseCalorie = baseCalorie(row[_Calorie.weight])
        )
    }
}


@Serializable
data class CalorieBean(
    var id: Int = 0,
    val weight: Float,
    val baseCalorie: Int
)

fun baseCalorie(weight: Float): Int {

    return ceil(((weight * 48.5 + 2954.7) / 4.184)).toInt()
}