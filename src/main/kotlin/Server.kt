package org.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.rpc.serialization.json
import kotlinx.rpc.transport.ktor.server.RPC
import kotlinx.rpc.transport.ktor.server.rpc
import org.example.model.Pizza
import org.example.model.PizzaShop
import org.example.model.Receipt
import kotlin.coroutines.CoroutineContext

class PizzaShopImpl(
    override val coroutineContext: CoroutineContext
) : PizzaShop {

    private val openOrders = mutableMapOf<String, MutableList<Pizza>>()

    override suspend fun orderPizza(clientID: String, pizza: Pizza): Receipt {
        if(openOrders.containsKey(clientID)) {
            openOrders[clientID]?.add(pizza)
        } else {
            openOrders[clientID] = mutableListOf(pizza)
        }
        return Receipt(3.45)
    }

    override suspend fun viewOrders(clientID: String): Flow<Pizza> {
        val orders = openOrders[clientID]
        if (orders != null) {
            return flow {
                for (order in orders) {
                    emit(order)
                    delay(1000)
                }
            }
        }
        return flow {}
    }
}
fun main() {
    embeddedServer(Netty, port = 8080) {
        module()
        println("Server running")
    }.start(wait = true)
}

fun Application.module() {
    install(RPC)

    routing {
        rpc ("/pizza") {
            rpcConfig {
                serialization {
                    json()
                }
            }

            registerService<PizzaShop> { ctx -> PizzaShopImpl(ctx) }
        }
    }
}
