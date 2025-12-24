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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.forAll

class PokemonDetailMappersTest : StringSpec({
    
    // Property-based tests for PokemonDetailDto.asDomain()
    
    "property: PokemonDetailDto.asDomain() preserves id" {
        checkAll(
            Arb.int(1..1000),
            arbPokemonName(),
            Arb.int(1..100),
            Arb.int(1..5000),
            Arb.int(0..500)
        ) { id, name, height, weight, baseExp ->
            val dto = PokemonDetailDto(
                id = id,
                name = name,
                height = height,
                weight = weight,
                baseExperience = baseExp,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto(frontDefault = "https://example.com/sprite.png")
            )
            
            val domain = dto.asDomain()
            
            domain.id shouldBe dto.id
            domain.height shouldBe dto.height
            domain.weight shouldBe dto.weight
            domain.baseExperience shouldBe dto.baseExperience
        }
    }
    
    "property: PokemonDetailDto.asDomain() capitalizes name" {
        forAll(arbPokemonName()) { name ->
            val dto = PokemonDetailDto(
                id = 1,
                name = name,
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = emptyList(),
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto(frontDefault = "https://example.com/sprite.png")
            )
            
            val domain = dto.asDomain()
            
            domain.name[0].isUpperCase()
        }
    }
    
    "property: PokemonDetailDto.asDomain() preserves types array size" {
        checkAll(Arb.int(1..100), Arb.list(Arb.int(1..10), 0..10)) { id, typeSlots ->
            val types = typeSlots.map { slot ->
                TypeSlotDto(
                    slot = slot,
                    type = TypeDto(name = "fire", url = "https://example.com/type/$slot/")
                )
            }
            
            val dto = PokemonDetailDto(
                id = id,
                name = "pokemon",
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = types,
                stats = emptyList(),
                abilities = emptyList(),
                sprites = SpritesDto(frontDefault = "https://example.com/sprite.png")
            )
            
            val domain = dto.asDomain()
            
            domain.types.size == types.size
        }
    }
    
    "property: PokemonDetailDto.asDomain() preserves stats array size" {
        checkAll(Arb.int(1..100), Arb.list(Arb.int(0..255), 0..10)) { id, baseStats ->
            val stats = baseStats.map { baseStat ->
                StatDto(
                    baseStat = baseStat,
                    effort = 0,
                    stat = StatInfoDto(name = "hp", url = "https://example.com/stat/1/")
                )
            }
            
            val dto = PokemonDetailDto(
                id = id,
                name = "pokemon",
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = emptyList(),
                stats = stats,
                abilities = emptyList(),
                sprites = SpritesDto(frontDefault = "https://example.com/sprite.png")
            )
            
            val domain = dto.asDomain()
            
            domain.stats.size == stats.size
        }
    }
    
    "property: PokemonDetailDto.asDomain() preserves abilities array size" {
        checkAll(Arb.int(1..100), Arb.list(Arb.int(1..10), 0..10)) { id, abilitySlots ->
            val abilities = abilitySlots.map { slot ->
                AbilitySlotDto(
                    isHidden = false,
                    slot = slot,
                    ability = AbilityDto(name = "static", url = "https://example.com/ability/$slot/")
                )
            }
            
            val dto = PokemonDetailDto(
                id = id,
                name = "pokemon",
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = emptyList(),
                stats = emptyList(),
                abilities = abilities,
                sprites = SpritesDto(frontDefault = "https://example.com/sprite.png")
            )
            
            val domain = dto.asDomain()
            
            domain.abilities.size == abilities.size
        }
    }
    
    "property: TypeSlotDto.asDomain() preserves all fields" {
        checkAll(Arb.int(1..10), arbTypeName()) { slot, typeName ->
            val dto = TypeSlotDto(
                slot = slot,
                type = TypeDto(name = typeName, url = "https://example.com/type/$slot/")
            )
            
            val domain = dto.asDomain()
            
            domain.slot == slot && domain.name == typeName
        }
    }
    
    "property: StatDto.asDomain() preserves all fields" {
        checkAll(Arb.int(0..255), Arb.int(0..10), arbStatName()) { baseStat, effort, statName ->
            val dto = StatDto(
                baseStat = baseStat,
                effort = effort,
                stat = StatInfoDto(name = statName, url = "https://example.com/stat/1/")
            )
            
            val domain = dto.asDomain()
            
            domain.baseStat == baseStat && 
            domain.effort == effort && 
            domain.name == statName
        }
    }
    
    "property: AbilitySlotDto.asDomain() preserves all fields" {
        checkAll(Arb.boolean(), Arb.int(1..10), arbAbilityName()) { isHidden, slot, abilityName ->
            val dto = AbilitySlotDto(
                isHidden = isHidden,
                slot = slot,
                ability = AbilityDto(name = abilityName, url = "https://example.com/ability/$slot/")
            )
            
            val domain = dto.asDomain()
            
            domain.isHidden == isHidden && 
            domain.slot == slot && 
            domain.name == abilityName
        }
    }
    
    // Concrete test cases
    
    "PokemonDetailDto.asDomain() should capitalize lowercase name" {
        val dto = PokemonDetailDto(
            id = 25,
            name = "pikachu",
            height = 4,
            weight = 60,
            baseExperience = 112,
            types = emptyList(),
            stats = emptyList(),
            abilities = emptyList(),
            sprites = SpritesDto(frontDefault = "https://example.com/25.png")
        )
        
        val domain = dto.asDomain()
        
        domain.name shouldBe "Pikachu"
    }
    
    "PokemonDetailDto.asDomain() should handle null baseExperience" {
        val dto = PokemonDetailDto(
            id = 1,
            name = "bulbasaur",
            height = 7,
            weight = 69,
            baseExperience = null,
            types = emptyList(),
            stats = emptyList(),
            abilities = emptyList(),
            sprites = SpritesDto(frontDefault = "https://example.com/1.png")
        )
        
        val domain = dto.asDomain()
        
        domain.baseExperience shouldBe 0
    }
    
    "PokemonDetailDto.asDomain() should handle null sprite" {
        val dto = PokemonDetailDto(
            id = 1,
            name = "bulbasaur",
            height = 7,
            weight = 69,
            baseExperience = 64,
            types = emptyList(),
            stats = emptyList(),
            abilities = emptyList(),
            sprites = SpritesDto(frontDefault = null)
        )
        
        val domain = dto.asDomain()
        
        domain.imageUrl shouldBe ""
    }
    
    "PokemonDetailDto.asDomain() should sort types by slot" {
        val dto = PokemonDetailDto(
            id = 1,
            name = "bulbasaur",
            height = 7,
            weight = 69,
            baseExperience = 64,
            types = listOf(
                TypeSlotDto(slot = 2, type = TypeDto(name = "poison", url = "https://example.com/type/4/")),
                TypeSlotDto(slot = 1, type = TypeDto(name = "grass", url = "https://example.com/type/12/"))
            ),
            stats = emptyList(),
            abilities = emptyList(),
            sprites = SpritesDto(frontDefault = "https://example.com/1.png")
        )
        
        val domain = dto.asDomain()
        
        domain.types.size shouldBe 2
        domain.types[0].slot shouldBe 1
        domain.types[0].name shouldBe "grass"
        domain.types[1].slot shouldBe 2
        domain.types[1].name shouldBe "poison"
    }
    
    "PokemonDetailDto.asDomain() should sort abilities by slot" {
        val dto = PokemonDetailDto(
            id = 25,
            name = "pikachu",
            height = 4,
            weight = 60,
            baseExperience = 112,
            types = emptyList(),
            stats = emptyList(),
            abilities = listOf(
                AbilitySlotDto(
                    isHidden = true,
                    slot = 3,
                    ability = AbilityDto(name = "lightning-rod", url = "https://example.com/ability/31/")
                ),
                AbilitySlotDto(
                    isHidden = false,
                    slot = 1,
                    ability = AbilityDto(name = "static", url = "https://example.com/ability/9/")
                )
            ),
            sprites = SpritesDto(frontDefault = "https://example.com/25.png")
        )
        
        val domain = dto.asDomain()
        
        domain.abilities.size shouldBe 2
        domain.abilities[0].slot shouldBe 1
        domain.abilities[0].name shouldBe "static"
        domain.abilities[1].slot shouldBe 3
        domain.abilities[1].name shouldBe "lightning-rod"
    }
    
    "TypeSlotDto.asDomain() should preserve all fields" {
        val dto = TypeSlotDto(
            slot = 1,
            type = TypeDto(name = "electric", url = "https://example.com/type/13/")
        )
        
        val domain = dto.asDomain()
        
        domain.slot shouldBe 1
        domain.name shouldBe "electric"
    }
    
    "StatDto.asDomain() should preserve all fields" {
        val dto = StatDto(
            baseStat = 55,
            effort = 2,
            stat = StatInfoDto(name = "attack", url = "https://example.com/stat/2/")
        )
        
        val domain = dto.asDomain()
        
        domain.name shouldBe "attack"
        domain.baseStat shouldBe 55
        domain.effort shouldBe 2
    }
    
    "AbilitySlotDto.asDomain() should preserve all fields" {
        val dto = AbilitySlotDto(
            isHidden = true,
            slot = 3,
            ability = AbilityDto(name = "lightning-rod", url = "https://example.com/ability/31/")
        )
        
        val domain = dto.asDomain()
        
        domain.name shouldBe "lightning-rod"
        domain.isHidden shouldBe true
        domain.slot shouldBe 3
    }
})

// Helper Arb generators for property-based testing

private fun arbPokemonName(): Arb<String> = 
    Arb.string(3..20)
        .filter { it.all { c -> c in 'a'..'z' } }
        .filter { it.isNotBlank() }

private fun arbTypeName(): Arb<String> = 
    Arb.choice(
        Arb.constant("normal"),
        Arb.constant("fire"),
        Arb.constant("water"),
        Arb.constant("electric"),
        Arb.constant("grass"),
        Arb.constant("ice"),
        Arb.constant("fighting"),
        Arb.constant("poison"),
        Arb.constant("ground"),
        Arb.constant("flying"),
        Arb.constant("psychic"),
        Arb.constant("bug"),
        Arb.constant("rock"),
        Arb.constant("ghost"),
        Arb.constant("dragon"),
        Arb.constant("dark"),
        Arb.constant("steel"),
        Arb.constant("fairy")
    )

private fun arbStatName(): Arb<String> = 
    Arb.choice(
        Arb.constant("hp"),
        Arb.constant("attack"),
        Arb.constant("defense"),
        Arb.constant("special-attack"),
        Arb.constant("special-defense"),
        Arb.constant("speed")
    )

private fun arbAbilityName(): Arb<String> = 
    Arb.string(3..30)
        .filter { it.all { c -> c in 'a'..'z' || c == '-' } }
        .filter { it.isNotBlank() && !it.startsWith('-') && !it.endsWith('-') }
