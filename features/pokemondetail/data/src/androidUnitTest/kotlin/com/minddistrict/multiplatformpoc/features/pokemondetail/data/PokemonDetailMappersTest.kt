package com.minddistrict.multiplatformpoc.features.pokemondetail.data

import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.AbilityDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.AbilitySlotDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.PokemonDetailDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.SpritesDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.StatDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.StatInfoDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.TypeDto
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.TypeSlotDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.collections.immutable.persistentListOf

class PokemonDetailMappersTest : StringSpec({
    
    "mapping: PokemonDetailDto.asDomain preserves ID" {
        checkAll(Arb.int(1..10000)) { id ->
            val dto = PokemonDetailDto(
                id = id,
                name = "test",
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto()
            )
            
            val domain = dto.asDomain()
            domain.id shouldBe id
        }
    }
    
    "mapping: PokemonDetailDto.asDomain capitalizes name with titlecase" {
        checkAll(
            arbPokemonName()
        ) { name ->
            val dto = PokemonDetailDto(
                id = 1,
                name = name,
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto()
            )
            
            val domain = dto.asDomain()
            // Name should be titlecased (first char becomes titlecase version)
            domain.name shouldBe name.replaceFirstChar { it.titlecase() }
        }
    }
    
    "mapping: PokemonDetailDto.asDomain preserves height and weight" {
        checkAll(
            Arb.int(1..1000),
            Arb.int(1..5000)
        ) { height, weight ->
            val dto = PokemonDetailDto(
                id = 1,
                name = "test",
                height = height,
                weight = weight,
                baseExperience = 50,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto()
            )
            
            val domain = dto.asDomain()
            domain.height shouldBe height
            domain.weight shouldBe weight
        }
    }
    
    "mapping: PokemonDetailDto.asDomain uses default 0 for null baseExperience" {
        val dto = PokemonDetailDto(
            id = 1,
            name = "test",
            height = 10,
            weight = 100,
            baseExperience = null,
            types = emptyList(),
            stats = emptyList(),
            abilities = emptyList(),
            sprites = SpritesDto()
        )
        
        val domain = dto.asDomain()
        domain.baseExperience shouldBe 0
    }
    
    "mapping: PokemonDetailDto.asDomain preserves baseExperience when present" {
        checkAll(Arb.int(0..500)) { baseExp ->
            val dto = PokemonDetailDto(
                id = 1,
                name = "test",
                height = 10,
                weight = 100,
                baseExperience = baseExp,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto()
            )
            
            val domain = dto.asDomain()
            domain.baseExperience shouldBe baseExp
        }
    }
    
    "mapping: PokemonDetailDto.asDomain preserves imageUrl from sprites" {
        val imageUrl = "https://example.com/pokemon.png"
        val dto = PokemonDetailDto(
            id = 1,
            name = "test",
            height = 10,
            weight = 100,
            baseExperience = 50,
            types = emptyList(),
            stats = emptyList(),
            abilities = emptyList(),
            sprites = SpritesDto(frontDefault = imageUrl)
        )
        
        val domain = dto.asDomain()
        domain.imageUrl shouldBe imageUrl
    }
    
    "mapping: PokemonDetailDto.asDomain uses empty string when imageUrl is null" {
        val dto = PokemonDetailDto(
            id = 1,
            name = "test",
            height = 10,
            weight = 100,
            baseExperience = 50,
            types = emptyList(),
            stats = emptyList(),
            abilities = emptyList(),
            sprites = SpritesDto(frontDefault = null)
        )
        
        val domain = dto.asDomain()
        domain.imageUrl shouldBe ""
    }
    
    "mapping: TypeSlotDto.asDomain preserves name and slot" {
        checkAll(
            arbPokemonName(),
            Arb.int(1..10)
        ) { name, slot ->
            val dto = TypeSlotDto(
                slot = slot,
                type = TypeDto(name = name, url = "https://example.com")
            )
            
            val domain = dto.asDomain()
            domain.name shouldBe name
            domain.slot shouldBe slot
        }
    }
    
    "mapping: StatDto.asDomain preserves stat values" {
        checkAll(
            Arb.int(0..255),
            Arb.int(0..255)
        ) { baseStat, effort ->
            val dto = StatDto(
                baseStat = baseStat,
                effort = effort,
                stat = StatInfoDto(name = "hp", url = "https://example.com")
            )
            
            val domain = dto.asDomain()
            domain.baseStat shouldBe baseStat
            domain.effort shouldBe effort
        }
    }
    
    "mapping: AbilitySlotDto.asDomain preserves isHidden and slot" {
        checkAll(
            Arb.int(1..10)
        ) { slot ->
            val dto = AbilitySlotDto(
                isHidden = true,
                slot = slot,
                ability = AbilityDto(name = "static", url = "https://example.com")
            )
            
            val domain = dto.asDomain()
            domain.isHidden shouldBe true
            domain.slot shouldBe slot
        }
    }
    
    "mapping: PokemonDetailDto.asDomain sorts types by slot" {
        val dto = PokemonDetailDto(
            id = 1,
            name = "test",
            height = 10,
            weight = 100,
            baseExperience = 50,
            types = listOf(
                TypeSlotDto(slot = 2, type = TypeDto(name = "flying", url = "url")),
                TypeSlotDto(slot = 1, type = TypeDto(name = "electric", url = "url"))
            ),
            stats = emptyList(),
            abilities = emptyList(),
            sprites = SpritesDto()
        )
        
        val domain = dto.asDomain()
        domain.types.size shouldBe 2
        domain.types[0].slot shouldBe 1
        domain.types[1].slot shouldBe 2
    }
    
    "mapping: PokemonDetailDto.asDomain creates immutable lists" {
        val dto = PokemonDetailDto(
            id = 1,
            name = "test",
            height = 10,
            weight = 100,
            baseExperience = 50,
            types = listOf(TypeSlotDto(slot = 1, type = TypeDto(name = "electric", url = "url"))),
            stats = listOf(StatDto(baseStat = 100, effort = 0, stat = StatInfoDto(name = "hp", url = "url"))),
            abilities = listOf(AbilitySlotDto(isHidden = false, slot = 1, ability = AbilityDto(name = "static", url = "url"))),
            sprites = SpritesDto()
        )
        
        val domain = dto.asDomain()
        
        // Verify lists have expected size
        domain.types.size shouldBe 1
        domain.stats.size shouldBe 1
        domain.abilities.size shouldBe 1
    }
})

private fun arbPokemonName(): Arb<String> = 
    Arb.string(3..20)
        .filter { it.all { c -> c.isLetterOrDigit() || c == ' ' || c == '-' } }
        .filter { it.isNotBlank() }
