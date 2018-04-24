import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.util.*
import kotlinx.html.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, port = 8080) {
        install(Sessions) {
            cookie<MySession>("EXERCISE1_SESSION", SessionStorageMemory())
        }
        routing {
            homeRoute()
            loginRoute()
        }
    }
    server.start(wait = true)
}

data class MySession(val user: String)

fun Routing.homeRoute() {
    get("/") {
        val session = call.sessions.get<MySession>()
        call.respondHtml {
            body {
                h1 {
                    +"Hello ${(session?.user ?: "World!").escapeHTML()}"
                }
                p {
                    a(href = "/login") { +"Login" }
                }
            }
        }
    }
}

fun Routing.loginRoute() {
    get("/login") {
        call.respondHtml {
            body {
                val message = call.parameters["message"] ?: ""
                if (message.isNotEmpty()) {
                    div {
                        +message
                    }
                }
                p {
                    +"Note: Use the same user and password for login in this demo"
                }
                form(action = "/login", encType = FormEncType.applicationXWwwFormUrlEncoded, method = FormMethod.post) {
                    div {
                        +"Username:"
                        textInput(name = "username") { }
                    }
                    div {
                        +"Password:"
                        passwordInput(name = "password") { }
                    }
                    div {
                        submitInput()
                    }
                }
            }
        }
    }

    post("/login2") {
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

    // Version using the Authentication feature
    application.install(Authentication, {
        form("equal-auth") {
            userParamName = "username"
            passwordParamName = "password"
            challenge = FormAuthChallenge.Redirect { credentials -> "/login?message=Invalid%20credentials" }
            validate { credentials ->
                if (credentials.name == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }

        }
    })

    authenticate("equal-auth") {
        post("/login") {
            val principal = call.authentication.principal<UserIdPrincipal>()
            call.sessions.set(MySession(principal!!.name))
            call.respondRedirect("/")
        }
    }
}