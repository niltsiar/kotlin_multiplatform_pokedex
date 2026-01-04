package com.minddistrict.multiplatformpoc.core.designsystem.core

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list

/**
 * Custom Kotest Arb generators for property-based testing.
 * 
 * These generators produce random test data for Pokémon-specific types,
 * enabling property-based tests to validate invariants across thousands of scenarios.
 */

/**
 * Generate random Pokémon type names.
 * Returns one of the 18 official Pokémon types.
 */
fun Arb.Companion.pokemonType(): Arb<String> = Arb.choice(
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

/**
 * Generate random Pokémon IDs.
 * Valid range: 1-10000 (covers all generations including potential future releases).
 */
fun Arb.Companion.pokemonId(): Arb<Int> = Arb.int(1..10000)

/**
 * Generate random Pokémon stat values.
 * Valid range: 0-255 (official stat range in Pokémon games).
 */
fun Arb.Companion.statValue(): Arb<Int> = Arb.int(0..255)

/**
 * Generate random lists of Pokémon types (1-2 types, as Pokémon can be dual-type).
 * Returns lists with 1 or 2 unique types.
 */
fun Arb.Companion.pokemonTypes(): Arb<List<String>> = arbitrary {
    val type1 = Arb.pokemonType().bind()
    val hasDualType = (Arb.int(0..1).bind() == 1)
    
    if (hasDualType) {
        // Generate second type that's different from first
        val allTypes = listOf(
            "normal", "fire", "water", "electric", "grass", "ice",
            "fighting", "poison", "ground", "flying", "psychic", "bug",
            "rock", "ghost", "dragon", "dark", "steel", "fairy"
        )
        val type2 = allTypes.filter { it != type1 }.random()
        listOf(type1, type2)
    } else {
        listOf(type1)
    }
}

private fun Arb.Companion.constant(value: String): Arb<String> = 
    arbitrary { value }
