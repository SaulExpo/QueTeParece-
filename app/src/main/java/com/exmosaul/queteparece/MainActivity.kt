package com.exmosaul.queteparece

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.exmosaul.queteparece.ui.navigation.AppNavHost
import com.exmosaul.queteparece.ui.theme.AppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

        val auth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(navController = navController)
                }
            }
        }
    }
}

private fun addSampleMoviesToFirestore() {
    val db = FirebaseFirestore.getInstance()
    val movies = listOf(
    mapOf(
        "title" to "Dune: Parte Dos",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg",
        "category" to "novedades",
        "isFeatured" to true,
        "type" to "live action",
        "genres" to listOf("Ciencia ficción", "Aventura", "Drama"),
        "rating" to 9
    ),
    mapOf(
        "title" to "Oppenheimer",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/ptpr0kGAckfQkJeJIt8st5dglvd.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Drama", "Histórica", "Biográfica"),
        "rating" to 9
    ),
    mapOf(
        "title" to "Barbie",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/iuFNMS8U5cb6xfzi51Dbkovj7vM.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Comedia", "Fantasía", "Aventura"),
        "rating" to 8
    ),
    mapOf(
        "title" to "The Batman",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Acción", "Crimen", "Suspenso"),
        "rating" to 9
    ),
    mapOf(
        "title" to "Avatar: El Camino del Agua",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg",
        "category" to "novedades",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Ciencia ficción", "Aventura", "Acción"),
        "rating" to 8
    ),
    mapOf(
        "title" to "John Wick 4",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/vZloFAK7NmvMGKE7VkF5UHaz0I.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Acción", "Crimen", "Suspenso"),
        "rating" to 8
    ),
    mapOf(
        "title" to "Misión Imposible: Sentencia Mortal",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/NNxYkU70HPurnNCSiCjYAmacwm.jpg",
        "category" to "novedades",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Acción", "Aventura", "Espionaje"),
        "rating" to 8
    ),
    mapOf(
        "title" to "Interestelar",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Ciencia ficción", "Drama", "Aventura"),
        "rating" to 10
    ),
    mapOf(
        "title" to "Gladiator II",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/qbldck0l8DmP48egBLJcL5F1cPh.jpg",
        "category" to "novedades",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Acción", "Histórica", "Drama"),
        "rating" to 8
    ),
    mapOf(
        "title" to "Spider-Man: Cruzando el Multiverso",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/8Vt6mWEReuy4Of61Lnj5Xj704m8.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "animada",
        "genres" to listOf("Acción", "Aventura", "Animación"),
        "rating" to 9
    ),
    mapOf(
        "title" to "Deadpool y Wolverine",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/fzKWwcaam9QSTaMSJlORuSojxio.jpg",
        "category" to "novedades",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Acción", "Comedia", "Superhéroes"),
        "rating" to 8
    ),
    mapOf(
        "title" to "Super Mario Bros. La Película",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/qNBAXBIQlnOThrVvA6mA2B5ggV6.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "animada",
        "genres" to listOf("Aventura", "Comedia", "Fantasia"),
        "rating" to 7
    ),
    mapOf(
        "title" to "Doctor Strange en el Multiverso de la Locura",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/wRnbWt44nKjsFPrqSmwYki5vZtF.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Acción", "Fantasía", "Superhéroes"),
        "rating" to 8
    ),
    mapOf(
        "title" to "Guardianes de la Galaxia Vol. 3",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/r2J02Z2OpNTctfOSN1Ydgii51I3.jpg",
        "category" to "novedades",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Ciencia ficción", "Acción", "Comedia"),
        "rating" to 9
    ),
    mapOf(
        "title" to "Avengers: Endgame",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Acción", "Ciencia ficción", "Superhéroes"),
        "rating" to 10
    ),
    mapOf(
        "title" to "El Caballero Oscuro",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Acción", "Crimen", "Drama"),
        "rating" to 10
    ),
    mapOf(
        "title" to "Tenet",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/aCIFMriQh8rvhxpN1IWGgvH0Tlg.jpg",
        "category" to "novedades",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Ciencia ficción", "Acción", "Suspenso"),
        "rating" to 8
    ),
    mapOf(
        "title" to "Black Panther: Wakanda Forever",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/sv1xJUazXeYqALzczSZ3O6nkH75.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Acción", "Aventura", "Superhéroes"),
        "rating" to 7
    ),
    mapOf(
        "title" to "Joker",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/udDclJoHjfjb8Ekgsd4FDteOkCU.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Drama", "Crimen", "Psicológico"),
        "rating" to 9
    ),
    mapOf(
        "title" to "La La Land",
        "imageUrl" to "https://image.tmdb.org/t/p/w500/uDO8zWDhfWwoFdKS4fzkUJt0Rf0.jpg",
        "category" to "tendencias",
        "isFeatured" to false,
        "type" to "live action",
        "genres" to listOf("Musical", "Romance", "Drama"),
        "rating" to 8
    ),
        mapOf(
            "title" to "The Matrix",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
            "category" to "tendencias",
            "isFeatured" to true,
            "type" to "live action",
            "genres" to listOf("Ciencia ficción", "Acción", "Filosofía"),
            "rating" to 10
        ),
        mapOf(
            "title" to "Inception",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Ciencia ficción", "Acción", "Suspenso"),
            "rating" to 9
        ),
        mapOf(
            "title" to "Fight Club",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/bptfVGEQuv6vDTIMVCHjJ9Dz8PX.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Drama", "Psicológico", "Crimen"),
            "rating" to 9
        ),
        mapOf(
            "title" to "El Señor de los Anillos: La Comunidad del Anillo",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/6oom5QYQ2yQTMJIbnvbkBL9cHo6.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Fantasía", "Aventura", "Acción"),
            "rating" to 10
        ),
        mapOf(
            "title" to "El Señor de los Anillos: Las Dos Torres",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/5VTN0pR8gcqV3EPUHHfMGnJYN9L.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Fantasía", "Aventura", "Acción"),
            "rating" to 9
        ),
        mapOf(
            "title" to "El Señor de los Anillos: El Retorno del Rey",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/rCzpDGLbOoPwLjy3OAm5NUPOTrC.jpg",
            "category" to "tendencias",
            "isFeatured" to true,
            "type" to "live action",
            "genres" to listOf("Fantasía", "Aventura", "Drama"),
            "rating" to 10
        ),
        mapOf(
            "title" to "Matrix Resurrections",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/8c4a8kE7PizaGQQnditMmI1xbRp.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Ciencia ficción", "Acción", "Drama"),
            "rating" to 6
        ),
        mapOf(
            "title" to "Duna",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/d5NXSklXo0qyIYkgV94XAgMIckC.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Ciencia ficción", "Aventura", "Drama"),
            "rating" to 9
        ),
        mapOf(
            "title" to "Mad Max: Furia en la carretera",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/8tZYtuWezp8JbcsvHYO0O46tFbo.jpg",
            "category" to "tendencias",
            "isFeatured" to true,
            "type" to "live action",
            "genres" to listOf("Acción", "Aventura", "Ciencia ficción"),
            "rating" to 9
        ),
        mapOf(
            "title" to "Furiosa: De la saga Mad Max",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/iADOJ8Zymht2JPMoy3R7xceZprc.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Aventura", "Postapocalíptica"),
            "rating" to 8
        ),
        mapOf(
            "title" to "The Flash",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/rktDFPbfHfUbArZ6OOOKsXcv0Bm.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Ciencia ficción", "Superhéroes"),
            "rating" to 6
        ),
        mapOf(
            "title" to "Aquaman y el Reino Perdido",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/9rUSCVzj7LVhc8D3gZ1i22YB1Gm.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Fantasía", "Superhéroes"),
            "rating" to 7
        ),
        mapOf(
            "title" to "El Exorcista: Creyente",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/d07xtqwq1uriQ1hda6qeu8Skt5m.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Terror", "Suspenso", "Sobrenatural"),
            "rating" to 6
        ),
        mapOf(
            "title" to "The Marvels",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/ArBec4lRc9bC9gNQ0K3mwkKkYJP.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Aventura", "Superhéroes"),
            "rating" to 6
        ),
        mapOf(
            "title" to "Wonka",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/qhb1qOilapbapxWQn9jtRCMwXJF.jpg",
            "category" to "novedades",
            "isFeatured" to true,
            "type" to "live action",
            "genres" to listOf("Fantasía", "Aventura", "Comedia"),
            "rating" to 8
        ),
        mapOf(
            "title" to "Napoleón",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/1Z4p5Jc2ijKGHN2ZhUty5RT1lP5.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Histórica", "Drama", "Acción"),
            "rating" to 8
        ),
        mapOf(
            "title" to "Killers of the Flower Moon",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/dB6Krk806zeqd0YNp2ngQ9zXteH.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Crimen", "Drama", "Misterio"),
            "rating" to 9
        ),
        mapOf(
            "title" to "The Creator",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/vBZ0qvaRxqEhZwl6LWmruJqWE8Z.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Ciencia ficción", "Acción", "Drama"),
            "rating" to 7
        ),
        mapOf(
            "title" to "Everything Everywhere All at Once",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/w3LxiVYdWWRvEVdn5RYq6jIqkb1.jpg",
            "category" to "tendencias",
            "isFeatured" to true,
            "type" to "live action",
            "genres" to listOf("Ciencia ficción", "Comedia", "Acción"),
            "rating" to 10
        ),
        mapOf(
            "title" to "Encanto",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/4j0PNHkMr5ax3IA8tjtxcmPU3QT.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "animada",
            "genres" to listOf("Fantasía", "Comedia", "Musical"),
            "rating" to 9
        ),
        mapOf(
            "title" to "Frozen II",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/pjeMs3yqRmFL3giJy4PMXWZTTPa.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "animada",
            "genres" to listOf("Aventura", "Fantasía", "Musical"),
            "rating" to 8
        ),
        mapOf(
            "title" to "Coco",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/gGEsBPAijhVUFoiNpgZXqRVWJt2.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "animada",
            "genres" to listOf("Fantasía", "Drama", "Musical"),
            "rating" to 10
        ),
        mapOf(
            "title" to "Soul",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/hm58Jw4Lw8OIeECIq5qyPYhAeRJ.jpg",
            "category" to "tendencias",
            "isFeatured" to true,
            "type" to "animada",
            "genres" to listOf("Aventura", "Drama", "Fantasía"),
            "rating" to 9
        ),
        mapOf(
            "title" to "Luca",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/jTswp6KyDYKtvC52GbHagrZbGvD.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "animada",
            "genres" to listOf("Aventura", "Comedia", "Infantil"),
            "rating" to 8
        ),
        mapOf(
            "title" to "Turning Red",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/qsdjk9oAKSQMWs0Vt5Pyfh6O4GZ.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "animada",
            "genres" to listOf("Comedia", "Fantasía", "Familiar"),
            "rating" to 7
        ),
        mapOf(
            "title" to "Elemental",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/4Y1WNkd88JXmGfhtWR7dmDAo1T2.jpg",
            "category" to "novedades",
            "isFeatured" to true,
            "type" to "animada",
            "genres" to listOf("Romance", "Fantasía", "Comedia"),
            "rating" to 8
        ),
        mapOf(
            "title" to "Lightyear",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/1JFkK6jXqWQyZ6sHqH82O0nZB5Z.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "animada",
            "genres" to listOf("Aventura", "Acción", "Ciencia ficción"),
            "rating" to 7
        ),
        mapOf(
            "title" to "The Whale",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/jQ0gylJMxWSL490sy0RrPj2InUx.jpg",
            "category" to "tendencias",
            "isFeatured" to true,
            "type" to "live action",
            "genres" to listOf("Drama", "Psicológico", "Independiente"),
            "rating" to 9
        ),
        mapOf(
            "title" to "The Fabelmans",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/d2IywyOPS78vEnJvwVqkVRTiNC1.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Drama", "Biográfica", "Familiar"),
            "rating" to 8
        ),
        mapOf(
            "title" to "Top Gun: Maverick",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/62HCnUTziyWcpDaBO2i1DX17ljH.jpg",
            "category" to "tendencias",
            "isFeatured" to true,
            "type" to "live action",
            "genres" to listOf("Acción", "Drama", "Aventura"),
            "rating" to 9
        ),
        mapOf(
            "title" to "1917",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/iZf0KyrE25z1sage4SYFLCCrMi9.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Guerra", "Drama", "Histórica"),
            "rating" to 9
        ),
        mapOf(
            "title" to "Parasite",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg",
            "category" to "tendencias",
            "isFeatured" to true,
            "type" to "live action",
            "genres" to listOf("Drama", "Suspenso", "Social"),
            "rating" to 10
        ),
        mapOf(
            "title" to "Knives Out",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/pThyQovXQrw2m0s9x82twj48Jq4.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Misterio", "Comedia", "Crimen"),
            "rating" to 8
        ),
        mapOf(
            "title" to "Glass Onion: Un misterio de Knives Out",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/vDGr1YdrlfbU9wxTOdpf3zChmv9.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Misterio", "Comedia", "Crimen"),
            "rating" to 7
        ),
        mapOf(
            "title" to "Bullet Train",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/tVxDe01Zy3kZqaZRNiXFGDICdZk.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Comedia", "Crimen"),
            "rating" to 8
        ),
        mapOf(
            "title" to "Nope",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/AcKVlWaNVVVFQwro3nLXqPljcYA.jpg",
            "category" to "novedades",
            "isFeatured" to true,
            "type" to "live action",
            "genres" to listOf("Ciencia ficción", "Terror", "Suspenso"),
            "rating" to 8
        ),
        mapOf(
            "title" to "The Menu",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/fPtUgMcLIboqlTlPrq0bQpKK8eq.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Suspenso", "Comedia negra", "Drama"),
            "rating" to 8
        ),
        mapOf(
            "title" to "The Northman",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/zhLKlUaF1SEpO58ppHIAyENkwgw.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Histórica", "Drama"),
            "rating" to 8
        ),
        mapOf(
            "title" to "John Wick 3: Parabellum",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/ziEuG1essDuWuC5lpWUaw1uXY2O.jpg",
            "category" to "tendencias",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Crimen", "Suspenso"),
            "rating" to 8
        ),
        mapOf(
            "title" to "The Equalizer 3",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/b0Ej6fnXAP8fK75hlyi2jKqdhHz.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Crimen", "Suspenso"),
            "rating" to 7
        ),
        mapOf(
            "title" to "Gran Turismo",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/51tqzRtKMMZEYUpSYkrUE7v9ehm.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Deportes", "Drama"),
            "rating" to 7
        ),
        mapOf(
            "title" to "Blue Beetle",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/mXLOHHc1Zeuwsl4xYKjKh2280oL.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Aventura", "Superhéroes"),
            "rating" to 7
        ),
        mapOf(
            "title" to "Transformers: El despertar de las bestias",
            "imageUrl" to "https://image.tmdb.org/t/p/w500/gPbM0MK8CP8A174rmUwGsADNYKD.jpg",
            "category" to "novedades",
            "isFeatured" to false,
            "type" to "live action",
            "genres" to listOf("Acción", "Ciencia ficción", "Aventura"),
            "rating" to 7
        )
    )




    for (movie in movies) {
        db.collection("movies").add(movie)
    }

}