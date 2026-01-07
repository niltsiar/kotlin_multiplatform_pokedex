package com.minddistrict.multiplatformpoc.features.pokemondetail.data

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.AbilityDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.AbilitySlotDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.PokemonDetailDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.SpritesDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.StatDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.StatInfoDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.TypeDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.TypeSlotDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.RepoError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.forAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class PokemonDetailRepositoryTest : StringSpec({
    
    lateinit var mockApiService: PokemonDetailApiService
    
    beforeTest {
        mockApiService = mockk(relaxed = true)
    }
    
    "getDetailByUrl should return Right with mapped domain object on success" {
        val url = "https://pokeapi.co/api/v2/pokemon/25/"
        val dto = PokemonDetailDto(
            id = 25,
            name = "pikachu",
            height = 4,
            weight = 60,
            baseExperience = 112,
            types = listOf(
                TypeSlotDto(1, TypeDto("electric", "https://pokeapi.co/api/v2/type/10/"))
            ),
            stats = listOf(
                StatDto(35, 0, StatInfoDto("hp", "https://pokeapi.co/api/v2/stat/1/"))
            ),
            abilities = listOf(
                AbilitySlotDto(false, 1, AbilityDto("static", "https://pokeapi.co/api/v2/ability/9/"))
            ),
            sprites = SpritesDto(frontDefault = "https://example.com/pikachu.png")
        )
        
        coEvery { mockApiService.getPokemonDetailByUrl(url) } returns dto
        
        val repository = PokemonDetailRepository(mockApiService)
        
        runTest {
            val result = repository.getDetailByUrl(url)
            
            val pokemon = result.shouldBeRight()
            pokemon.id shouldBe 25
            pokemon.name.first().isUpperCase().shouldBeTrue()
            pokemon.height shouldBe 4
            pokemon.weight shouldBe 60
            pokemon.baseExperience shouldBe 112
            pokemon.imageUrl shouldBe "https://example.com/pikachu.png"
            
            coVerify(exactly = 1) { mockApiService.getPokemonDetailByUrl(url) }
        }
    }
    
    "property: getDetailByUrl returns Right preserving all ID and stats data" {
        checkAll(
            Arb.int(1..10000),
            Arb.int(1..100),
            Arb.int(1..5000),
            Arb.int(0..500)
        ) { id, height, weight, baseExp ->
            val url = "https://pokeapi.co/api/v2/pokemon/$id/"
            val dto = PokemonDetailDto(
                id = id,
                name = "pokemon",
                height = height,
                weight = weight,
                baseExperience = baseExp,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto()
            )
            
            coEvery { mockApiService.getPokemonDetailByUrl(url) } returns dto
            val repository = PokemonDetailRepository(mockApiService)
            
            runTest {
                val result = repository.getDetailByUrl(url)
                
                val pokemon = result.shouldBeRight()
                pokemon.id shouldBe id
                pokemon.height shouldBe height
                pokemon.weight shouldBe weight
                pokemon.baseExperience shouldBe baseExp
            }
        }
    }
    
    "getDetailByUrl should return Left with Network error on connect timeout" {
        val url = "https://pokeapi.co/api/v2/pokemon/25/"
        coEvery { mockApiService.getPokemonDetailByUrl(url) } throws 
            io.ktor.client.network.sockets.ConnectTimeoutException("Connection timeout", null)
        
        val repository = PokemonDetailRepository(mockApiService)
        
        runTest {
            val result = repository.getDetailByUrl(url)
            
            val error = result.shouldBeLeft()
            error shouldBe RepoError.Network
        }
    }
    
    "getDetailByUrl should return Left with Network error on socket timeout" {
        val url = "https://pokeapi.co/api/v2/pokemon/25/"
        coEvery { mockApiService.getPokemonDetailByUrl(url) } throws 
            io.ktor.client.network.sockets.SocketTimeoutException("Socket timeout")
        
        val repository = PokemonDetailRepository(mockApiService)
        
        runTest {
            val result = repository.getDetailByUrl(url)
            
            val error = result.shouldBeLeft()
            error shouldBe RepoError.Network
        }
    }
    
    "getDetailByUrl should return Left with Network error on request timeout" {
        val url = "https://pokeapi.co/api/v2/pokemon/25/"
        coEvery { mockApiService.getPokemonDetailByUrl(url) } throws 
            io.ktor.client.plugins.HttpRequestTimeoutException("Request timeout", null)
        
        val repository = PokemonDetailRepository(mockApiService)
        
        runTest {
            val result = repository.getDetailByUrl(url)
            
            val error = result.shouldBeLeft()
            error shouldBe RepoError.Network
        }
    }
    
    "property: getDetailByUrl maps all HTTP 400-599 error codes to Http error" {
        checkAll(Arb.int(400..599)) { httpCode ->
            val url = "https://pokeapi.co/api/v2/pokemon/25/"
            // Create a proper mock response for testing HTTP errors
            val mockResponse = mockk<io.ktor.client.statement.HttpResponse>(relaxed = true) {
                io.mockk.every { status.value } returns httpCode
                io.mockk.every { status.description } returns "Error"
            }
            
            val exception = io.ktor.client.plugins.ClientRequestException(
                response = mockResponse,
                cachedResponseText = ""
            )
            
            coEvery { mockApiService.getPokemonDetailByUrl(url) } throws exception
            val repository = PokemonDetailRepository(mockApiService)
            
            runTest {
                val result = repository.getDetailByUrl(url)
                
                val error = result.shouldBeLeft()
                (error is RepoError.Http).shouldBeTrue()
                error as RepoError.Http
                error.code shouldBe httpCode
            }
        }
    }
    
    "getDetailByUrl should return Left with Http 404 on client request error" {
        val url = "https://pokeapi.co/api/v2/pokemon/99999/"
        val mockResponse = mockk<io.ktor.client.statement.HttpResponse>(relaxed = true) {
            io.mockk.every { status.value } returns 404
            io.mockk.every { status.description } returns "Not Found"
        }
        
        val exception = io.ktor.client.plugins.ClientRequestException(
            response = mockResponse,
            cachedResponseText = ""
        )
        
        coEvery { mockApiService.getPokemonDetailByUrl(url) } throws exception
        val repository = PokemonDetailRepository(mockApiService)
        
        runTest {
            val result = repository.getDetailByUrl(url)
            
            val error = result.shouldBeLeft()
            (error is RepoError.Http).shouldBeTrue()
            error as RepoError.Http
            error.code shouldBe 404
        }
    }
    
    "getDetailByUrl should return Left with Http 500 on server response error" {
        val url = "https://pokeapi.co/api/v2/pokemon/25/"
        val mockResponse = mockk<io.ktor.client.statement.HttpResponse>(relaxed = true) {
            io.mockk.every { status.value } returns 500
            io.mockk.every { status.description } returns "Internal Server Error"
        }
        
        val exception = io.ktor.client.plugins.ServerResponseException(
            response = mockResponse,
            cachedResponseText = ""
        )
        
        coEvery { mockApiService.getPokemonDetailByUrl(url) } throws exception
        val repository = PokemonDetailRepository(mockApiService)
        
        runTest {
            val result = repository.getDetailByUrl(url)
            
            val error = result.shouldBeLeft()
            (error is RepoError.Http).shouldBeTrue()
            error as RepoError.Http
            error.code shouldBe 500
        }
    }
    
    "getDetailByUrl should return Left with Unknown error on unexpected exception" {
        val url = "https://pokeapi.co/api/v2/pokemon/25/"
        coEvery { mockApiService.getPokemonDetailByUrl(url) } throws 
            RuntimeException("Unexpected error")
        
        val repository = PokemonDetailRepository(mockApiService)
        
        runTest {
            val result = repository.getDetailByUrl(url)
            
            val error = result.shouldBeLeft()
            (error is RepoError.Unknown).shouldBeTrue()
        }
    }
    
    "property: getDetailByUrl with different pokemon IDs returns successful result" {
        checkAll(Arb.int(1..10000)) { id ->
            val url = "https://pokeapi.co/api/v2/pokemon/$id/"
            val dto = PokemonDetailDto(
                id = id,
                name = "pokemon$id",
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto()
            )
            
            coEvery { mockApiService.getPokemonDetailByUrl(url) } returns dto
            val repository = PokemonDetailRepository(mockApiService)
            
            runTest {
                val result = repository.getDetailByUrl(url)
                
                val pokemon = result.shouldBeRight()
                pokemon.id shouldBe id
            }
        }
    }
    
    "property: any repository error produces valid Either.Left" {
        checkAll(
            Arb.int(1..10000),
            arbRepoError()
        ) { pokemonId, error ->
            val url = "https://pokeapi.co/api/v2/pokemon/$pokemonId/"
            
            val throwable: Throwable = when (error) {
                is RepoError.Network -> io.ktor.client.network.sockets.ConnectTimeoutException("timeout", null)
                is RepoError.Http -> {
                    val mockResponse = mockk<io.ktor.client.statement.HttpResponse>(relaxed = true) {
                        io.mockk.every { status.value } returns error.code
                    }
                    io.ktor.client.plugins.ClientRequestException(mockResponse, "")
                }
                is RepoError.Unknown -> error.cause
            }
            
            coEvery { mockApiService.getPokemonDetailByUrl(url) } throws throwable
            val repository = PokemonDetailRepository(mockApiService)
            
            runTest {
                val result = repository.getDetailByUrl(url)
                result.shouldBeLeft()
            }
        }
    }
})

private fun arbRepoError(): Arb<RepoError> = 
    Arb.choice(
        Arb.constant(RepoError.Network),
        Arb.int(400..599).map { code -> RepoError.Http(code, "Error $code") },
        Arb.constant(RepoError.Unknown(RuntimeException("Test error")))
    )
