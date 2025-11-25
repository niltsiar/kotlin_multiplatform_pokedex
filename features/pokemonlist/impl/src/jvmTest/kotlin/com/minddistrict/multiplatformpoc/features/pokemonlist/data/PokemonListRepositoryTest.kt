package com.minddistrict.multiplatformpoc.features.pokemonlist.data

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonListDto
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonSummaryDto
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.RepoError
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk

class PokemonListRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonListApiService
    lateinit var repository: com.minddistrict.multiplatformpoc.features.pokemonlist.PokemonListRepository
    
    beforeTest {
        mockApi = mockk()
        repository = PokemonListRepository(mockApi)
    }
    
    "should return Right with mapped domain on success" {
        val dto = PokemonListDto(
            count = 1292,
            next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
            previous = null,
            results = listOf(
                PokemonSummaryDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonSummaryDto("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
            )
        )
        
        coEvery { mockApi.getPokemonList(20, 0) } returns dto
        
        val result = repository.loadPage(limit = 20, offset = 0)
        
        result.shouldBeInstanceOf<Either.Right<*>>()
        val page = (result as Either.Right).value
        page.pokemons.size shouldBe 2
        page.pokemons[0].id shouldBe 1
        page.pokemons[0].name shouldBe "Bulbasaur"
        page.pokemons[1].id shouldBe 2
        page.pokemons[1].name shouldBe "Ivysaur"
        page.hasMore shouldBe true
    }
    
    "should return Network error on ConnectTimeoutException" {
        coEvery { mockApi.getPokemonList(any(), any()) } throws 
            ConnectTimeoutException("timeout")
        
        val result = repository.loadPage()
        
        result.shouldBeInstanceOf<Either.Left<*>>()
        val error = (result as Either.Left).value
        error shouldBe RepoError.Network
    }
    
    "should return Network error on SocketTimeoutException" {
        coEvery { mockApi.getPokemonList(any(), any()) } throws 
            SocketTimeoutException("timeout")
        
        val result = repository.loadPage()
        
        result.shouldBeInstanceOf<Either.Left<*>>()
        val error = (result as Either.Left).value
        error shouldBe RepoError.Network
    }
    
    "should return Http error on ClientRequestException (4xx)" {
        val mockResponse = mockk<HttpResponse> {
            coEvery { status } returns HttpStatusCode.NotFound
        }
        
        coEvery { mockApi.getPokemonList(any(), any()) } throws 
            ClientRequestException(mockResponse, "Not found")
        
        val result = repository.loadPage()
        
        result.shouldBeInstanceOf<Either.Left<*>>()
        val error = (result as Either.Left).value as RepoError.Http
        error.code shouldBe 404
    }
    
    "should return Http error on ServerResponseException (5xx)" {
        val mockResponse = mockk<HttpResponse> {
            coEvery { status } returns HttpStatusCode.InternalServerError
        }
        
        coEvery { mockApi.getPokemonList(any(), any()) } throws 
            ServerResponseException(mockResponse, "Internal server error")
        
        val result = repository.loadPage()
        
        result.shouldBeInstanceOf<Either.Left<*>>()
        val error = (result as Either.Left).value as RepoError.Http
        error.code shouldBe 500
    }
    
    "should return Unknown error on unexpected exception" {
        val unexpectedException = IllegalStateException("Unexpected error")
        
        coEvery { mockApi.getPokemonList(any(), any()) } throws unexpectedException
        
        val result = repository.loadPage()
        
        result.shouldBeInstanceOf<Either.Left<*>>()
        val error = (result as Either.Left).value as RepoError.Unknown
        error.cause shouldBe unexpectedException
    }
    
    "should map hasMore to false when next is null" {
        val dto = PokemonListDto(
            count = 20,
            next = null,
            previous = "https://pokeapi.co/api/v2/pokemon?offset=0&limit=20",
            results = listOf(
                PokemonSummaryDto("pikachu", "https://pokeapi.co/api/v2/pokemon/25/")
            )
        )
        
        coEvery { mockApi.getPokemonList(20, 1272) } returns dto
        
        val result = repository.loadPage(limit = 20, offset = 1272)
        
        result.shouldBeInstanceOf<Either.Right<*>>()
        val page = (result as Either.Right).value
        page.hasMore shouldBe false
    }
    
    "should use default parameters when not specified" {
        val dto = PokemonListDto(
            count = 1292,
            next = null,
            previous = null,
            results = emptyList()
        )
        
        coEvery { mockApi.getPokemonList(20, 0) } returns dto
        
        val result = repository.loadPage()
        
        result.shouldBeInstanceOf<Either.Right<*>>()
    }
    
    // Property-based tests for repository behavior
    
    "property: repository maps any valid DTO to domain successfully" {
        checkAll(
            Arb.int(0..10000), // count
            Arb.list(Arb.int(1..1000), 0..50), // pokemon IDs
            Arb.string(0..200).orNull(), // next
            Arb.int(0..1000), // limit
            Arb.int(0..10000)  // offset
        ) { count, ids, next, limit, offset ->
            val results = ids.mapIndexed { index, id ->
                PokemonSummaryDto(
                    name = "pokemon$index",
                    url = "https://pokeapi.co/api/v2/pokemon/$id/"
                )
            }
            
            val dto = PokemonListDto(
                count = count,
                next = next,
                previous = null,
                results = results
            )
            
            coEvery { mockApi.getPokemonList(limit, offset) } returns dto
            
            val result = repository.loadPage(limit, offset)
            
            result.shouldBeInstanceOf<Either.Right<*>>()
            val page = (result as Either.Right).value
            page.pokemons.size shouldBe results.size
            page.hasMore shouldBe (next != null)
        }
    }
    
    "property: repository maps all HTTP error codes to RepoError.Http" {
        checkAll(Arb.int(400..599)) { statusCode ->
            val mockResponse = mockk<HttpResponse> {
                coEvery { status } returns HttpStatusCode.fromValue(statusCode)
            }
            
            val exception = if (statusCode < 500) {
                ClientRequestException(mockResponse, "Client error")
            } else {
                ServerResponseException(mockResponse, "Server error")
            }
            
            coEvery { mockApi.getPokemonList(any(), any()) } throws exception
            
            val result = repository.loadPage()
            
            result.shouldBeInstanceOf<Either.Left<*>>()
            val error = (result as Either.Left).value as RepoError.Http
            error.code shouldBe statusCode
        }
    }
    
    "property: repository preserves pagination parameters" {
        checkAll(Arb.int(1..100), Arb.int(0..10000)) { limit, offset ->
            val dto = PokemonListDto(
                count = 1000,
                next = null,
                previous = null,
                results = emptyList()
            )
            
            coEvery { mockApi.getPokemonList(limit, offset) } returns dto
            
            val result = repository.loadPage(limit, offset)
            
            result.shouldBeInstanceOf<Either.Right<*>>()
        }
    }
})
