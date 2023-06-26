
package fr.wildcodeschool.kotlinsample

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.http.*
import kotlinx.coroutines.channels.*
import java.io.File




class SpaceXApi {
    //used to prepare API connection
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    //first suspend
    suspend fun getAllLaunches(): List<RocketLaunch> {
        return httpClient.get("https://api.spacexdata.com/v5/launches").body()
    }

    //second suspend
    suspend fun downloadArticle(launch: RocketLaunch, client: HttpClient) {
        val url = launch.links.article
        println("article for launch ${launch.flightNumber} : ${url}" )

        GlobalScope.launch(Dispatchers.IO) {
            val file = File("C:/users/willi/OneDrive/Bureau/Articles/${url.pathSegments.last()}")
            client.get(url).bodyAsChannel().copyAndClose(file.writeChannel())
            println("Finished downloading..")
        }
    }    
}

fun main() = runBlocking<Unit> {
    println("OPERATION STARTED.")
    
    val service = SpaceXApi() //instance of

    val launches: List<RocketLaunch> = service.getAllLaunches() //launch first suspend

    var i = 0               //launch second suspend
    for (l in launches) {
        println("Launch $i : ${l}")
        service.downloadArticle(l, service)
        i++
    }
    
    println("OPERATION FINISHED.")
}


//DTO
@Serializable
data class RocketLaunch(
    @SerialName("flight_number")
    val flightNumber: Int,
    @SerialName("name")
    val missionName: String,
    @SerialName("date_utc")
    val launchDateUTC: String,
    @SerialName("details")
    val details: String?,
    @SerialName("success")
    val launchSuccess: Boolean?,
    @SerialName("links")
    val links: Links
) {
}

@Serializable
data class Links(
    @SerialName("patch")
    val patch: Patch?,
    @SerialName("article")
    val article: String?
)

@Serializable
data class Patch(
    @SerialName("small")
    val small: String?,
    @SerialName("large")
    val large: String?
)