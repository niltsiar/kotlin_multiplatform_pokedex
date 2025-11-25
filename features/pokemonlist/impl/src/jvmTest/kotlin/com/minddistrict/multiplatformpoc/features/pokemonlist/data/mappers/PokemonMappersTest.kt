package com.minddistrict.multiplatformpoc.features.pokemonlist.data.mappers

import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonListDto
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonSummaryDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.kotest.property.forAll
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class PokemonMappersTest : StringSpec({
    
    // Property-based tests for extractIdFromUrl
    
    "property: extractIdFromUrl works for any valid Pokemon ID" {
        checkAll(Arb.int(1..10000)) { id ->
            val urlWithSlash = "https://pokeapi.co/api/v2/pokemon/$id/"
            val urlWithoutSlash = "https://pokeapi.co/api/v2/pokemon/$id"
            
            extractIdFromUrl(urlWithSlash) shouldBe id
            extractIdFromUrl(urlWithoutSlash) shouldBe id
        }
    }
    
    "property: extractIdFromUrl throws for invalid URLs" {
        checkAll(Arb.string(1..20).filter { !it.contains('/') }) { invalidSegment ->
            shouldThrow<IllegalArgumentException> {
                extractIdFromUrl(invalidSegment)
            }
        }
    }
    
    "extractIdFromUrl should extract ID from standard URL with trailing slash" {
        val id = extractIdFromUrl("https://pokeapi.co/api/v2/pokemon/25/")
        id shouldBe 25
    }
    
    "extractIdFromUrl should extract ID from URL without trailing slash" {
        val id = extractIdFromUrl("https://pokeapi.co/api/v2/pokemon/25")
        id shouldBe 25
    }
    
    "extractIdFromUrl should handle various IDs" {
        val testCases = listOf(
            "https://pokeapi.co/api/v2/pokemon/1/" to 1,
            "https://pokeapi.co/api/v2/pokemon/150/" to 150,
            "https://pokeapi.co/api/v2/pokemon/999/" to 999
        )
        
        testCases.forEach { (url, expectedId) ->
            extractIdFromUrl(url) shouldBe expectedId
        }
    }
    
    "extractIdFromUrl should throw on invalid URL without ID" {
        shouldThrow<IllegalArgumentException> {
            extractIdFromUrl("https://pokeapi.co/api/v2/pokemon/")
        }
    }
    
    "extractIdFromUrl should throw on completely invalid URL" {
        shouldThrow<IllegalArgumentException> {
            extractIdFromUrl("invalid-url")
        }
    }
    
    "extractIdFromUrl should throw on URL with non-numeric ID" {
        shouldThrow<IllegalArgumentException> {
            extractIdFromUrl("https://pokeapi.co/api/v2/pokemon/abc/")
        }
    }
    
    // Property-based tests for toDomain mapping
    
    "property: toDomain always capitalizes first letter of name" {
        checkAll(Arb.string(1..50)) { name ->
            val dto = PokemonSummaryDto(
                name = name.lowercase(),
                url = "https://pokeapi.co/api/v2/pokemon/1/"
            )
            
            val domain = dto.toDomain()
            
            if (name.isNotEmpty()) {
                domain.name.first().isUpperCase() shouldBe true
                domain.name.drop(1) shouldBe name.lowercase().drop(1)
            }
        }
    }
    
    "property: toDomain generates correct image URL for any ID" {
        checkAll(Arb.int(1..10000)) { id ->
            val dto = PokemonSummaryDto(
                name = "pokemon",
                url = "https://pokeapi.co/api/v2/pokemon/$id/"
            )
            
            val domain = dto.toDomain()
            
            domain.imageUrl shouldBe "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
        }
    }
    
    "property: toDomain preserves ID regardless of name" {
        checkAll(Arb.int(1..10000), Arb.string(1..50)) { id, name ->
            val dto = PokemonSummaryDto(
                name = name,
                url = "https://pokeapi.co/api/v2/pokemon/$id/"
            )
            
            val domain = dto.toDomain()
            
            domain.id shouldBe id
        }
    }
    
    "PokemonSummaryDto.toDomain should capitalize first letter of name" {
        val dto = PokemonSummaryDto(
            name = "pikachu",
            url = "https://pokeapi.co/api/v2/pokemon/25/"
        )
        
        val domain = dto.toDomain()
        
        domain.name shouldBe "Pikachu"
    }
    
    "PokemonSummaryDto.toDomain should preserve already capitalized names" {
        val dto = PokemonSummaryDto(
            name = "Bulbasaur",
            url = "https://pokeapi.co/api/v2/pokemon/1/"
        )
        
        val domain = dto.toDomain()
        
        domain.name shouldBe "Bulbasaur"
    }
    
    "PokemonSummaryDto.toDomain should generate correct image URL" {
        val dto = PokemonSummaryDto(
            name = "pikachu",
            url = "https://pokeapi.co/api/v2/pokemon/25/"
        )
        
        val domain = dto.toDomain()
        
        domain.imageUrl shouldBe "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
    }
    
    // Property-based tests for PokemonListDto mapping
    
    "property: PokemonListDto.toDomain preserves hasMore flag correctly" {
        checkAll(Arb.int(0..10000), Arb.boolean()) { count, hasNext ->
            val dto = PokemonListDto(
                count = count,
                next = if (hasNext) "https://pokeapi.co/api/v2/pokemon?offset=20" else null,
                previous = null,
                results = emptyList()
            )
            
            val page = dto.toDomain()
            
            page.hasMore shouldBe hasNext
        }
    }
    
    "property: PokemonListDto.toDomain maps all results correctly" {
        checkAll(Arb.list(Arb.int(1..1000), 0..20)) { ids ->
            val results = ids.mapIndexed { index, id ->
                PokemonSummaryDto(
                    name = "pokemon$index",
                    url = "https://pokeapi.co/api/v2/pokemon/$id/"
                )
            }
            
            val dto = PokemonListDto(
                count = results.size,
                next = null,
                previous = null,
                results = results
            )
            
            val page = dto.toDomain()
            
            page.pokemons.size shouldBe results.size
            page.pokemons.forEachIndexed { index, pokemon ->
                pokemon.id shouldBe ids[index]
                pokemon.name shouldBe "Pokemon$index"
            }
        }
    }
    
    "PokemonListDto.toDomain should map all pokemons" {
        val dto = PokemonListDto(
            count = 1292,
            next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
            previous = null,
            results = listOf(
                PokemonSummaryDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonSummaryDto("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/"),
                PokemonSummaryDto("venusaur", "https://pokeapi.co/api/v2/pokemon/3/")
            )
        )
        
        val page = dto.toDomain()
        
        page.pokemons.size shouldBe 3
        page.pokemons[0].name shouldBe "Bulbasaur"
        page.pokemons[1].name shouldBe "Ivysaur"
        page.pokemons[2].name shouldBe "Venusaur"
    }
    
    "PokemonListDto.toDomain should set hasMore to true when next is not null" {
        val dto = PokemonListDto(
            count = 1292,
            next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
            previous = null,
            results = listOf(
                PokemonSummaryDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")
            )
        )
        
        val page = dto.toDomain()
        
        page.hasMore shouldBe true
    }
    
    "PokemonListDto.toDomain should set hasMore to false when next is null" {
        val dto = PokemonListDto(
            count = 20,
            next = null,
            previous = "https://pokeapi.co/api/v2/pokemon?offset=0&limit=20",
            results = listOf(
                PokemonSummaryDto("mew", "https://pokeapi.co/api/v2/pokemon/151/")
            )
        )
        
        val page = dto.toDomain()
        
        page.hasMore shouldBe false
    }
    
    "PokemonListDto.toDomain should handle empty results" {
        val dto = PokemonListDto(
            count = 0,
            next = null,
            previous = null,
            results = emptyList()
        )
        
        val page = dto.toDomain()
        
        page.pokemons.size shouldBe 0
        page.hasMore shouldBe false
    }
    
    // Enhanced round-trip tests with property-based testing
    
    "property: JSON round-trip preserves PokemonSummaryDto for any valid data" {
        checkAll(Arb.int(1..10000), Arb.string(1..100).filter { it.isNotBlank() }) { id, name ->
            val original = PokemonSummaryDto(
                name = name,
                url = "https://pokeapi.co/api/v2/pokemon/$id/"
            )
            
            val json = Json.encodeToString(original)
            val decoded = Json.decodeFromString<PokemonSummaryDto>(json)
            
            decoded shouldBe original
        }
    }
    
    "property: JSON round-trip preserves PokemonListDto with varying list sizes" {
        checkAll(
            Arb.int(0..10000), // count
            Arb.list(Arb.int(1..1000), 0..50), // pokemon IDs
            Arb.string(0..200).orNull(), // next
            Arb.string(0..200).orNull()  // previous
        ) { count, ids, next, previous ->
            val results = ids.mapIndexed { index, id ->
                PokemonSummaryDto(
                    name = "pokemon$index",
                    url = "https://pokeapi.co/api/v2/pokemon/$id/"
                )
            }
            
            val original = PokemonListDto(
                count = count,
                next = next,
                previous = previous,
                results = results
            )
            
            val json = Json.encodeToString(original)
            val decoded = Json.decodeFromString<PokemonListDto>(json)
            
            decoded shouldBe original
            decoded.results.size shouldBe results.size
        }
    }
    
    "property: DTO to domain to DTO maintains consistency" {
        forAll(Arb.int(1..10000), Arb.string(1..50).filter { it.isNotBlank() }) { id, name ->
            val dto1 = PokemonSummaryDto(
                name = name.lowercase(),
                url = "https://pokeapi.co/api/v2/pokemon/$id/"
            )
            
            val domain = dto1.toDomain()
            
            // Create another DTO with same data
            val dto2 = PokemonSummaryDto(
                name = name.lowercase(),
                url = "https://pokeapi.co/api/v2/pokemon/$id/"
            )
            
            val domain2 = dto2.toDomain()
            
            // Same input should produce same output
            domain == domain2
        }
    }
    
    // Original round-trip tests (kept for specific edge cases)
    
    "round-trip: JSON to PokemonSummaryDto to JSON preserves data" {
        val json = """{"name":"pikachu","url":"https://pokeapi.co/api/v2/pokemon/25/"}"""
        
        val dto = Json.decodeFromString<PokemonSummaryDto>(json)
        val encodedJson = Json.encodeToString(dto)
        
        // Decode again to verify round-trip
        val decoded = Json.decodeFromString<PokemonSummaryDto>(encodedJson)
        decoded shouldBe dto
        decoded.name shouldBe "pikachu"
        decoded.url shouldBe "https://pokeapi.co/api/v2/pokemon/25/"
    }
    
    "round-trip: PokemonSummaryDto to JSON to PokemonSummaryDto is equal" {
        val dto = PokemonSummaryDto(
            name = "bulbasaur",
            url = "https://pokeapi.co/api/v2/pokemon/1/"
        )
        
        val json = Json.encodeToString(dto)
        val decoded = Json.decodeFromString<PokemonSummaryDto>(json)
        
        decoded shouldBe dto
    }
    
    "round-trip: PokemonListDto with null fields through JSON" {
        val dto = PokemonListDto(
            count = 20,
            next = null,
            previous = null,
            results = emptyList()
        )
        
        val json = Json.encodeToString(dto)
        val decoded = Json.decodeFromString<PokemonListDto>(json)
        
        decoded shouldBe dto
        decoded.next shouldBe null
        decoded.previous shouldBe null
    }
})
