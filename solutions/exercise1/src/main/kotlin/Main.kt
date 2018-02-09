import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import kotlinx.html.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        install(Sessions) {
            cookie<MySession>("EXERCISE1_SESSION", SessionStorageMemory())
        }
        routing {
            rootRoute()
            loginRoute()
        }
    }
    server.start(wait = true)
}

data class MySession(val user: String)

fun Routing.rootRoute() {
    get("/") {
        val session = call.sessions.get<MySession>()
        call.respondHtml {
            body {
                h1 {
                    // Alternatively:
                    //+"Hello ${(session?.user ?: "World").escapeHTML()}!"

                    +"Hello "
                    if (session != null) {
                        +session.user
                    } else {
                        +"World"
                    }
                    +"!"
                }
            }
        }
    }
}

fun Routing.loginRoute() {
    get("/login") {
        call.respondHtml {
            body {
                form(action = "/login", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post) {
                    div {
                        +"Username:"
                        textInput(name = "username") {  }
                    }
                    div {
                        +"Password:"
                        passwordInput(name = "password") {  }
                    }
                    div {
                        submitInput()
                    }
                }
            }
        }
    }
    post("/login") {
        val post = call.receiveOrNull() ?: Parameters.Empty
        val username = post["username"]
        val password = post["password"]

        if (username != null && username.isNotBlank() && username == password) {
            call.sessions.set(MySession(username))
            call.respondRedirect("/")
        } else {
            call.respondHtml {
                body {
                    +"Invalid credentials. "
                    a(href = "/login") { +"Retry?" }
                }
            }
        }
    }
}