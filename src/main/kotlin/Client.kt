package org.example

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.rpc.client.withService
import kotlinx.rpc.serialization.json
import kotlinx.rpc.streamScoped
import kotlinx.rpc.transport.ktor.client.KtorRPCClient
import kotlinx.rpc.transport.ktor.client.installRPC
import kotlinx.rpc.transport.ktor.client.rpc
import kotlinx.rpc.transport.ktor.client.rpcConfig
import org.example.model.Pizza
import org.example.model.PizzaShop


fun main() = runBlocking {
    val ktorClient = HttpClient {
        installRPC {
            waitForServices = true
        }
    }

    val client: KtorRPCClient = ktorClient.rpc {
        url {
            host = "localhost"
            port = 8080
            encodedPath = "pizza"
        }

        rpcConfig {
            serialization {
                json()
            }
        }
    }


    val pizzaShop: PizzaShop = client.withService<PizzaShop>()

    pizzaShop.orderPizza("AB12", Pizza("Pepperoni"))
    pizzaShop.orderPizza("AB12", Pizza("Hawaiian"))
    pizzaShop.orderPizza("AB12", Pizza("Calzone"))

    pizzaShop.orderPizza("CD34", Pizza("Margherita"))
    pizzaShop.orderPizza("CD34", Pizza("Sicilian"))
    pizzaShop.orderPizza("CD34", Pizza("California"))

    streamScoped {
        pizzaShop.viewOrders("AB12").collect {
            println("AB12 ordered ${it.name}")
        }

        pizzaShop.viewOrders("CD34").collect {
            println("CD34 ordered ${it.name}")
        }
    }

    ktorClient.close()
}

