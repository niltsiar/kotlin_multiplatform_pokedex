package com.minddistrict.multiplatformpoc.features.pokemondetail

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.RepoError

interface PokemonDetailRepository {
    /**
     * Get detailed information about a specific Pokemon.
     * @param id Pokemon ID
     */
    suspend fun getDetail(id: Int): Either<RepoError, PokemonDetail>
}
