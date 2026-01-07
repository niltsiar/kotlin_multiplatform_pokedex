package com.minddistrict.multiplatformpoc.features.pokemonlist.data

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonListDto
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonSummaryDto
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.RepoError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.forAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest

class PokemonListRepositoryTest : StringSpec({
    
    lateinit var mockApiService: PokemonListApiService
    
    beforeTest {
        mockApiService = mockk(relaxed = true)
    }
    
    "loadPage should return Right with mapped domain object on success" {
        val pokemonSummaries = listOf(
            PokemonSummaryDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
            PokemonSummaryDto("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
        )
        val dto = PokemonListDto(
            count = 2,
            next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
            previous = null,
            results = pokemonSummaries
        )
        
        coEvery { mockApiService.getPokemonList(20, 0) } returns dto
        val repository = PokemonListRepository(mockApiService)
        
        runTest {
            val result = repository.loadPage(20, 0)
            
            val page = result.shouldBeRight()
            page.pokemons.size shouldBe 2
            page.pokemons[0].id shouldBe 1
            page.pokemons[1].id shouldBe 2
            page.hasMore.shouldBeTrue()
            
            coVerify(exactly = 1) { mockApiService.getPokemonList(20, 0) }
        }
    }
    
    "loadPageByUrl should return Right with mapped domain object on success" {
        val url = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20"
        val pokemonSummaries = listOf(
            PokemonSummaryDto("pikachu", "https://pokeapi.co/api/v2/pokemon/25/")
        )
        val dto = PokemonListDto(
            count = 100,
            next = null,
            previous = "https://pokeapi.co/api/v2/pokemon?offset=0&limit=20",
            results = pokemonSummaries
        )
        
        coEvery { mockApiService.getPokemonListByUrl(url) } returns dto
        val repository = PokemonListRepository(mockApiService)
        
        runTest {
            val result = repository.loadPageByUrl(url)
            
            val page = result.shouldBeRight()
            page.pokemons.size shouldBe 1
            page.pokemons[0].id shouldBe 25
            
            coVerify(exactly = 1) { mockApiService.getPokemonListByUrl(url) }
        }
    }
    
    "property: loadPage returns Right with correct pokemon count for any valid range" {
        checkAll(
            Arb.list(Arb.int(1..10000), 1..50),
            Arb.int(0..10000)
        ) { ids, offset ->
            val pokemonSummaries = ids.mapIndexed { idx, id ->
                PokemonSummaryDto(
                    name = "pokemon$idx",
                    url = "https://pokeapi.co/api/v2/pokemon/$id/"
                )
            }
            val dto = PokemonListDto(
                count = 10000,
                next = "https://pokeapi.co/api/v2/pokemon?offset=${offset + ids.size}&limit=20",
                previous = null,
                results = pokemonSummaries
            )
            
            coEvery { mockApiService.getPokemonList(any(), any()) } returns dto
            val repository = PokemonListRepository(mockApiService)
            
            runTest {
                val result = repository.loadPage(ids.size, offset)
                
                val page = result.shouldBeRight()
                page.pokemons.size shouldBe ids.size
                ids.forEachIndexed { idx, expectedId ->
                    page.pokemons[idx].id shouldBe expectedId
                }
            }
        }
    }
    
    "loadPage should return Left with Network error on network exception" {
        coEvery { mockApiService.getPokemonList(any(), any()) } throws 
            io.ktor.client.network.sockets.ConnectTimeoutException("Connection timeout", null)
        
        val repository = PokemonListRepository(mockApiService)
        
        runTest {
            val result = repository.loadPage(20, 0)
            
            val error = result.shouldBeLeft()
            error shouldBe RepoError.Network
        }
    }
    
    "loadPage should return Left with Network error on socket timeout" {
        coEvery { mockApiService.getPokemonList(any(), any()) } throws 
            io.ktor.client.network.sockets.SocketTimeoutException("Socket timeout")
        
        val repository = PokemonListRepository(mockApiService)
        
        runTest {
            val result = repository.loadPage(20, 0)
            
            val error = result.shouldBeLeft()
            error shouldBe RepoError.Network
        }
    }
    
    "loadPage should return Left with Network error on request timeout" {
        coEvery { mockApiService.getPokemonList(any(), any()) } throws 
            io.ktor.client.plugins.HttpRequestTimeoutException("Request timeout", null)
        
        val repository = PokemonListRepository(mockApiService)
        
        runTest {
            val result = repository.loadPage(20, 0)
            
            val error = result.shouldBeLeft()
            error shouldBe RepoError.Network
        }
    }
    
    "property: loadPage maps all HTTP 400-599 error codes to Http error" {
        checkAll(Arb.int(400..599)) { httpCode ->
            val mockResponse = mockk<io.ktor.client.statement.HttpResponse>(relaxed = true) {
                io.mockk.every { status.value } returns httpCode
            }
            
            val exception = io.ktor.client.plugins.ClientRequestException(
                response = mockResponse,
                cachedResponseText = ""
            )
            
            coEvery { mockApiService.getPokemonList(any(), any()) } throws exception
            val repository = PokemonListRepository(mockApiService)
            
            runTest {
                val result = repository.loadPage(20, 0)
                
                val error = result.shouldBeLeft()
                (error is RepoError.Http).shouldBeTrue()
                error as RepoError.Http
                error.code shouldBe httpCode
            }
        }
    }
    
    "loadPage should return Left with Http error on client request error" {
        val mockResponse = mockk<io.ktor.client.statement.HttpResponse>(relaxed = true) {
            io.mockk.every { status.value } returns 404
        }
        
        val exception = io.ktor.client.plugins.ClientRequestException(
            response = mockResponse,
            cachedResponseText = ""
        )
        
        coEvery { mockApiService.getPokemonList(any(), any()) } throws exception
        val repository = PokemonListRepository(mockApiService)
        
        runTest {
            val result = repository.loadPage(20, 0)
            
            val error = result.shouldBeLeft()
            (error is RepoError.Http).shouldBeTrue()
            error as RepoError.Http
            error.code shouldBe 404
        }
    }
    
    "loadPage should return Left with Http error on server response error" {
        val mockResponse = mockk<io.ktor.client.statement.HttpResponse>(relaxed = true) {
            io.mockk.every { status.value } returns 500
        }
        
        val exception = io.ktor.client.plugins.ServerResponseException(
            response = mockResponse,
            cachedResponseText = ""
        )
        
        coEvery { mockApiService.getPokemonList(any(), any()) } throws exception
        val repository = PokemonListRepository(mockApiService)
        
        runTest {
            val result = repository.loadPage(20, 0)
            
            val error = result.shouldBeLeft()
            (error is RepoError.Http).shouldBeTrue()
            error as RepoError.Http
            error.code shouldBe 500
        }
    }
    
    "loadPage should return Left with Unknown error on unexpected exception" {
        coEvery { mockApiService.getPokemonList(any(), any()) } throws 
            RuntimeException("Unexpected error")
        
        val repository = PokemonListRepository(mockApiService)
        
        runTest {
            val result = repository.loadPage(20, 0)
            
            val error = result.shouldBeLeft()
            (error is RepoError.Unknown).shouldBeTrue()
        }
    }
    
    "loadPageByUrl should return Left with Network error on network exception" {
        coEvery { mockApiService.getPokemonListByUrl(any()) } throws 
            io.ktor.client.network.sockets.ConnectTimeoutException("Connection timeout", null)
        
        val repository = PokemonListRepository(mockApiService)
        
        runTest {
            val result = repository.loadPageByUrl("https://pokeapi.co/api/v2/pokemon?offset=20")
            
            val error = result.shouldBeLeft()
            error shouldBe RepoError.Network
        }
    }
    
    "property: loadPageByUrl maps all HTTP 400-599 error codes to Http error" {
        checkAll(Arb.int(400..599)) { httpCode ->
            val mockResponse = mockk<io.ktor.client.statement.HttpResponse>(relaxed = true) {
                io.mockk.every { status.value } returns httpCode
            }
            
            val exception = io.ktor.client.plugins.ClientRequestException(
                response = mockResponse,
                cachedResponseText = ""
            )
            
            coEvery { mockApiService.getPokemonListByUrl(any()) } throws exception
            val repository = PokemonListRepository(mockApiService)
            
            runTest {
                val result = repository.loadPageByUrl("https://pokeapi.co/api/v2/pokemon?offset=20")
                
                val error = result.shouldBeLeft()
                (error is RepoError.Http).shouldBeTrue()
                error as RepoError.Http
                error.code shouldBe httpCode
            }
        }
    }
    
    "loadPageByUrl should return Left with Unknown error on unexpected exception" {
        coEvery { mockApiService.getPokemonListByUrl(any()) } throws 
            RuntimeException("Unexpected error")
        
        val repository = PokemonListRepository(mockApiService)
        
        runTest {
            val result = repository.loadPageByUrl("https://pokeapi.co/api/v2/pokemon?offset=20")
            
            val error = result.shouldBeLeft()
            (error is RepoError.Unknown).shouldBeTrue()
        }
    }
    
    "property: repository preserves all pokemon data from API response" {
        checkAll(
            Arb.list(Arb.int(1..10000), 1..50),
            Arb.list(
                Arb.string(3..20).filter { it.all { c -> c.isLetterOrDigit() } },
                1..50
            )
        ) { ids, names ->
            val pokemonSummaries = ids.zip(names).map { (id, name) ->
                PokemonSummaryDto(
                    name = name,
                    url = "https://pokeapi.co/api/v2/pokemon/$id/"
                )
            }
            val dto = PokemonListDto(
                count = ids.size * 2,
                next = null,
                previous = null,
                results = pokemonSummaries
            )
            
            coEvery { mockApiService.getPokemonList(any(), any()) } returns dto
            val repository = PokemonListRepository(mockApiService)
            
            runTest {
                val result = repository.loadPage(ids.size, 0)
                
                val page = result.shouldBeRight()
                page.pokemons.forEachIndexed { idx, pokemon ->
                    pokemon.id shouldBe ids[idx]
                }
            }
        }
    }
})
