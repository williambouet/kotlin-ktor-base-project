package fr.wildcodeschool.kotlinsample

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.serialization.json.Json
import kotlinx.coroutines.*
import java.io.File
import java.nio.file.Files

class SpaceXApi {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    suspend fun getAllLaunches(): List<RocketLaunch> {
        return httpClient.get("https://api.spacexdata.com/v5/launches").body()
    }

    private val targetDir = Files.createTempDirectory("spacex-articles")
    suspend fun downloadArticle(launch: RocketLaunch) {
        launch.links.article?.let { articleUrl ->
            val url = Url(articleUrl)

            val targetPath = targetDir.resolve("article_" + launch.flightNumber)
            println("download to " + targetPath)
            httpClient.get(url).bodyAsChannel().copyAndClose(targetPath.toFile().writeChannel())
        }
    }
}

fun main() = runBlocking<Unit> {
    val service = SpaceXApi()
    val launches: List<RocketLaunch> = service.getAllLaunches()
    for (l in launches) {
        async {
            println("download article for launch ${l.flightNumber}")
            service.downloadArticle(l)
            println("DONE download article for launch ${l.flightNumber}")
        }
    }
}