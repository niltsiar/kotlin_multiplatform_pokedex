package com.minddistrict.multiplatformpoc.features.pokemondetail.data

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemondetail.PokemonDetailRepository
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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk

class PokemonDetailRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonDetailApiService
    lateinit var repository: PokemonDetailRepository
    
    beforeTest {
        mockApi = mockk()
        repository = PokemonDetailRepository(mockApi)
    }
    
    "should return Right with mapped domain on success" {
        val dto = PokemonDetailDto(
            id = 25,
            name = "pikachu",
            height = 4,
            weight = 60,
            baseExperience = 112,
            types = listOf(
                TypeSlotDto(
                    slot = 1,
                    type = TypeDto(name = "electric", url = "https://pokeapi.co/api/v2/type/13/")
                )
            ),
            stats = listOf(
                StatDto(
                    baseStat = 35,
                    effort = 0,
                    stat = StatInfoDto(name = "hp", url = "https://pokeapi.co/api/v2/stat/1/")
                ),
                StatDto(
                    baseStat = 55,
                    effort = 0,
                    stat = StatInfoDto(name = "attack", url = "https://pokeapi.co/api/v2/stat/2/")
                )
            ),
            abilities = listOf(
                AbilitySlotDto(
                    isHidden = false,
                    slot = 1,
                    ability = AbilityDto(name = "static", url = "https://pokeapi.co/api/v2/ability/9/")
                ),
                AbilitySlotDto(
                    isHidden = true,
                    slot = 3,
                    ability = AbilityDto(name = "lightning-rod", url = "https://pokeapi.co/api/v2/ability/31/")
                )
            ),
            sprites = SpritesDto(
                frontDefault = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
                frontShiny = null,
                backDefault = null,
                backShiny = null
            )
        )
        
        coEvery { mockApi.getPokemonDetail(25) } returns dto
        
        val result = repository.getDetail(25)
        
        val pokemon = result.shouldBeRight()
        pokemon.id shouldBe 25
        pokemon.name shouldBe "Pikachu"
        pokemon.height shouldBe 4
        pokemon.weight shouldBe 60
        pokemon.baseExperience shouldBe 112
        pokemon.types.size shouldBe 1
        pokemon.types[0].name shouldBe "electric"
        pokemon.stats.size shouldBe 2
        pokemon.stats[0].name shouldBe "hp"
        pokemon.stats[0].baseStat shouldBe 35
        pokemon.abilities.size shouldBe 2
        pokemon.abilities[0].name shouldBe "static"
        pokemon.abilities[0].isHidden shouldBe false
        pokemon.abilities[1].name shouldBe "lightning-rod"
        pokemon.abilities[1].isHidden shouldBe true
        pokemon.imageUrl shouldBe "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
    }
    
    "should return Network error on ConnectTimeoutException" {
        coEvery { mockApi.getPokemonDetail(any()) } throws 
            ConnectTimeoutException("timeout")
        
        val result = repository.getDetail(25)
        
        val error = result.shouldBeLeft()
        error shouldBe RepoError.Network
    }
    
    "should return Network error on SocketTimeoutException" {
        coEvery { mockApi.getPokemonDetail(any()) } throws 
            SocketTimeoutException("timeout")
        
        val result = repository.getDetail(25)
        
        val error = result.shouldBeLeft()
        error shouldBe RepoError.Network
    }
    
    "should return Unknown error on unexpected exceptions" {
        coEvery { mockApi.getPokemonDetail(any()) } throws 
            RuntimeException("Unexpected error")
        
        val result = repository.getDetail(25)
        
        val error = result.shouldBeLeft()
        error.shouldBeInstanceOf<RepoError.Unknown>()
    }
    
    // Property-based tests for repository behavior
    
    "property: repository maps any valid DTO to domain successfully" {
        checkAll(
            Arb.int(1..1000), // pokemonId
            Arb.string(1..50), // name
            Arb.int(1..100), // height
            Arb.int(1..5000), // weight
            Arb.int(0..500).orNull() // baseExperience
        ) { id, name, height, weight, baseExp ->
            val dto = PokemonDetailDto(
                id = id,
                name = name,
                height = height,
                weight = weight,
                baseExperience = baseExp,
                types = listOf(
                    TypeSlotDto(
                        slot = 1,
                        type = TypeDto(name = "fire", url = "https://pokeapi.co/api/v2/type/10/")
                    )
                ),
                stats = listOf(
                    StatDto(
                        baseStat = 50,
                        effort = 0,
                        stat = StatInfoDto(name = "hp", url = "https://pokeapi.co/api/v2/stat/1/")
                    )
                ),
                abilities = listOf(
                    AbilitySlotDto(
                        isHidden = false,
                        slot = 1,
                        ability = AbilityDto(name = "blaze", url = "https://pokeapi.co/api/v2/ability/66/")
                    )
                ),
                sprites = SpritesDto(
                    frontDefault = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
                )
            )
            
            coEvery { mockApi.getPokemonDetail(id) } returns dto
            
            val result = repository.getDetail(id)
            
            val pokemon = result.shouldBeRight()
            pokemon.id shouldBe id
            pokemon.height shouldBe height
            pokemon.weight shouldBe weight
            pokemon.baseExperience shouldBe (baseExp ?: 0)
            pokemon.types.size shouldBe 1
            pokemon.stats.size shouldBe 1
            pokemon.abilities.size shouldBe 1
        }
    }
    
    "property: repository maps all HTTP error codes to RepoError.Http" {
        checkAll(Arb.int(400..599)) { statusCode ->
            val mockResponse = mockk<HttpResponse>(relaxed = true) {
                coEvery { status } returns HttpStatusCode.fromValue(statusCode)
            }
            
            val exception = if (statusCode < 500) {
                ClientRequestException(mockResponse, "Client error")
            } else {
                ServerResponseException(mockResponse, "Server error")
            }
            
            coEvery { mockApi.getPokemonDetail(any()) } throws exception
            
            val result = repository.getDetail(25)
            
            val error = result.shouldBeLeft()
            error.shouldBeInstanceOf<RepoError.Http>()
            error.code shouldBe statusCode
        }
    }
    
    "property: repository preserves pokemon ID in requests" {
        checkAll(Arb.int(1..1000)) { pokemonId ->
            val dto = PokemonDetailDto(
                id = pokemonId,
                name = "test",
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto(frontDefault = "https://example.com/sprite.png")
            )
            
            coEvery { mockApi.getPokemonDetail(pokemonId) } returns dto
            
            val result = repository.getDetail(pokemonId)
            
            val pokemon = result.shouldBeRight()
            pokemon.id shouldBe pokemonId
        }
    }
    
    "property: repository handles null baseExperience consistently" {
        checkAll(Arb.int(1..1000)) { pokemonId ->
            val dto = PokemonDetailDto(
                id = pokemonId,
                name = "test",
                height = 10,
                weight = 100,
                baseExperience = null,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto(frontDefault = "https://example.com/sprite.png")
            )
            
            coEvery { mockApi.getPokemonDetail(pokemonId) } returns dto
            
            val result = repository.getDetail(pokemonId)
            
            val pokemon = result.shouldBeRight()
            pokemon.baseExperience shouldBe 0
        }
    }
    
    "property: repository handles null sprite consistently" {
        checkAll(Arb.int(1..1000)) { pokemonId ->
            val dto = PokemonDetailDto(
                id = pokemonId,
                name = "test",
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto(frontDefault = null)
            )
            
            coEvery { mockApi.getPokemonDetail(pokemonId) } returns dto
            
            val result = repository.getDetail(pokemonId)
            
            val pokemon = result.shouldBeRight()
            pokemon.imageUrl shouldBe ""
        }
    }
})
