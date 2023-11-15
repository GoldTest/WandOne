import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.*
import model.Database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.SharedInstance
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun main() = run {
    application {
        //db
        val dbDirectory = File("data")
        if (!dbDirectory.exists()) {
            dbDirectory.mkdir()
        }

        configureDatabases()



        val viewModel = APPViewModel
        val windowState = rememberWindowState()
        windowState.size = DpSize(APP_WINDOW_WIDTH, APP_WINDOW_HEIGHT)
        windowState.position = WindowPosition(Alignment.Center)
//        Tray(this)

        val icon = painterResource(APP_ICON)
        Window(
            onCloseRequest = { viewModel.isVisible.value = false },
            visible = true,//viewModel.isVisible.value,
            icon = icon,
            title = APP_WINDOW_TITLE,
            state = windowState
        ) {

            MaterialTheme {
                App(viewModel = viewModel)
            }
        }
    }
}

fun configureDatabases() {

    val userService = UserService(database)
    SharedInstance.scope.launch {
        //create
        val id = userService.create(ExposedUser("user0", 11))
        //read
        val read = userService.read(id)
        println("read :$read")
        //update
        val user2 = ExposedUser("user1", 111)
        userService.update(id, user2)
        val read2 = userService.read(id)

        println("update :$read2")
        //delete
        userService.delete(id)
        val read3 = userService.read(id)
        println("delete :$read3")
    }
}

data class ExposedUser(val name: String, val age: Int)
class UserService(private val database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)
        val age = integer("age")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(user: ExposedUser): Int = dbQuery {
        Users.insert {
            it[name] = user.name
            it[age] = user.age
        }[Users.id]
    }

    suspend fun read(id: Int): ExposedUser? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map { ExposedUser(it[Users.name], it[Users.age]) }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, user: ExposedUser) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[age] = user.age
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }
}