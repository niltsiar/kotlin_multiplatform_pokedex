package com.minddistrict.multiplatformpoc.features.pokemonlist.data.mappers

import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonListDto
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonSummaryDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.forAll

class PokemonMappersTest : StringSpec({
    
    "mapping: PokemonSummaryDto.toDomain preserves name and URL" {
        checkAll(
            arbPokemonName(),
            Arb.int(1..10000)
        ) { name, id ->
            val dto = PokemonSummaryDto(
                name = name,
                url = "https://pokeapi.co/api/v2/pokemon/$id/"
            )
            
            val domain = dto.toDomain()
            domain.detailUrl shouldBe dto.url
        }
    }
    
    "mapping: PokemonSummaryDto.toDomain capitalizes first character of name" {
        checkAll(arbPokemonName()) { name ->
            val dto = PokemonSummaryDto(
                name = name,
                url = "https://pokeapi.co/api/v2/pokemon/1/"
            )
            
            val domain = dto.toDomain()
            domain.name shouldBe name.replaceFirstChar { it.uppercase() }
        }
    }
    
    "mapping: Pokemon.id property extracts ID from detailUrl correctly" {
        checkAll(Arb.int(1..10000)) { expectedId ->
            val dto = PokemonSummaryDto(
                name = "pokemon",
                url = "https://pokeapi.co/api/v2/pokemon/$expectedId/"
            )
            
            val domain = dto.toDomain()
            domain.id shouldBe expectedId
        }
    }
    
    "property: Pokemon.id extraction works for any URL format" {
        forAll(
            Arb.int(1..10000)
        ) { id ->
            // Test various URL formats
            val urls = listOf(
                "https://pokeapi.co/api/v2/pokemon/$id/",
                "http://pokeapi.co/api/v2/pokemon/$id/",
                "https://pokeapi.co/api/v2/pokemon/$id"
            )
            
            urls.all { url ->
                val dto = PokemonSummaryDto("pokemon", url)
                val domain = dto.toDomain()
                domain.id == id
            }
        }
    }
    
    "mapping: Pokemon.imageUrl generates correct sprite URL from ID" {
        checkAll(Arb.int(1..10000)) { id ->
            val dto = PokemonSummaryDto(
                name = "pokemon",
                url = "https://pokeapi.co/api/v2/pokemon/$id/"
            )
            
            val domain = dto.toDomain()
            domain.imageUrl shouldBe "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
        }
    }
    
    "mapping: PokemonListDto.toDomain maps all pokemons" {
        checkAll(
            Arb.list(
                Arb.int(1..10000),
                1..50
            )
        ) { ids ->
            val pokemonSummaries = ids.mapIndexed { idx, id ->
                PokemonSummaryDto(
                    name = "pokemon$idx",
                    url = "https://pokeapi.co/api/v2/pokemon/$id/"
                )
            }
            
            val dto = PokemonListDto(
                count = ids.size,
                next = null,
                previous = null,
                results = pokemonSummaries
            )
            
            val domain = dto.toDomain()
            domain.pokemons.size shouldBe ids.size
            domain.pokemons.forEachIndexed { idx, pokemon ->
                pokemon.id shouldBe ids[idx]
            }
        }
    }
    
    "mapping: PokemonPage.hasMore reflects nextUrl presence" {
        val nextUrl = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20"
        
        val dtoWithNext = PokemonListDto(
            count = 100,
            next = nextUrl,
            previous = null,
            results = emptyList()
        )
        
        val dtoWithoutNext = PokemonListDto(
            count = 100,
            next = null,
            previous = null,
            results = emptyList()
        )
        
        dtoWithNext.toDomain().hasMore.shouldBeTrue()
        dtoWithoutNext.toDomain().hasMore.shouldBeFalse()
    }
    
    "mapping: PokemonPage.hasPrevious reflects previousUrl presence" {
        val previousUrl = "https://pokeapi.co/api/v2/pokemon?offset=0&limit=20"
        
        val dtoWithPrevious = PokemonListDto(
            count = 100,
            next = null,
            previous = previousUrl,
            results = emptyList()
        )
        
        val dtoWithoutPrevious = PokemonListDto(
            count = 100,
            next = null,
            previous = null,
            results = emptyList()
        )
        
        dtoWithPrevious.toDomain().hasPrevious.shouldBeTrue()
        dtoWithoutPrevious.toDomain().hasPrevious.shouldBeFalse()
    }
    
    "property: PokemonPage pagination properties match DTO" {
        forAll(
            Arb.boolean(),
            Arb.boolean(),
            Arb.list(Arb.int(1..10000), 0..100)
        ) { hasNext, hasPrevious, ids ->
            val nextUrl = if (hasNext) "https://pokeapi.co/api/v2/pokemon?offset=${ids.size}&limit=20" else null
            val previousUrl = if (hasPrevious) "https://pokeapi.co/api/v2/pokemon?offset=0&limit=20" else null
            
            val pokemonSummaries = ids.mapIndexed { idx, id ->
                PokemonSummaryDto("pokemon$idx", "https://pokeapi.co/api/v2/pokemon/$id/")
            }
            
            val dto = PokemonListDto(
                count = ids.size * 2,
                next = nextUrl,
                previous = previousUrl,
                results = pokemonSummaries
            )
            
            val domain = dto.toDomain()
            (domain.nextUrl != null) == hasNext &&
            (domain.previousUrl != null) == hasPrevious &&
            domain.pokemons.size == ids.size
        }
    }
    
    "mapping: empty PokemonListDto maps to empty domain list" {
        val dto = PokemonListDto(
            count = 0,
            next = null,
            previous = null,
            results = emptyList()
        )
        
        val domain = dto.toDomain()
        domain.pokemons.shouldBe(emptyList())
        domain.hasMore.shouldBeFalse()
        domain.hasPrevious.shouldBeFalse()
    }
    
    "mapping: PokemonPage nextUrl and previousUrl preserve original URLs" {
        val nextUrl = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20"
        val previousUrl = "https://pokeapi.co/api/v2/pokemon?offset=0&limit=20"
        
        val dto = PokemonListDto(
            count = 100,
            next = nextUrl,
            previous = previousUrl,
            results = listOf(
                PokemonSummaryDto("pokemon1", "https://pokeapi.co/api/v2/pokemon/1/")
            )
        )
        
        val domain = dto.toDomain()
        domain.nextUrl shouldBe nextUrl
        domain.previousUrl shouldBe previousUrl
    }
    
    "mapping: PokemonListDto with null URLs maps correctly" {
        val dto = PokemonListDto(
            count = 0,
            next = null,
            previous = null,
            results = emptyList()
        )
        
        val domain = dto.toDomain()
        domain.nextUrl.shouldBeNull()
        domain.previousUrl.shouldBeNull()
    }
})

private fun arbPokemonName(): Arb<String> = 
    Arb.string(3..20)
        .filter { it.all { c -> c.isLetterOrDigit() || c == ' ' || c == '-' } }
        .filter { it.isNotBlank() }
