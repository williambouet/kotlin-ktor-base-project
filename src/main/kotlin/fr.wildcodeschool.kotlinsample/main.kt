package fr.wildcodeschool.kotlinsample

import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import java.io.*
import java.nio.charset.Charset
import java.text.*
import java.util.*

fun getDepartmentSurface(csvLine: String): Float? {
    val cells: List<String> = csvLine.split(';')
    if (cells.size < 7) {
        return null
    }

    val departmentName = cells[3]
    val surface = cells[6]
    if (surface.isBlank() || surface == "surf_km2") {
        return null
    }
    println("departement $departmentName a une surface de : $surface")

    return surface.toFloat()
}

fun main() {
    println("Bonjour cher utilisateur, nous allons calculer la surface de la France !")
    val filePath = "/home/louis/Downloads/contours-des-departements-francais-issus-dopenstreetmap.csv"

    var total = 0f
    File(filePath).forEachLine { line ->
        total += (getDepartmentSurface(line) ?: 0f)
    }

    println("Le total est $total")

    val out: File = File.createTempFile("fr_stats", ".json")
    val json = """
        {
          "totalSurface": $total
        }
    """.trimIndent()
    println("Ecriture du JSON $json vers $out")
    PrintWriter(FileWriter(out, Charset.defaultCharset()))
        .use { it.write(json) }
}