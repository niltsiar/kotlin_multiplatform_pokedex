package com.minddistrict.multiplatformpoc.features.pokemondetail

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.RepoError

interface PokemonDetailRepository {
    suspend fun getDetailByUrl(url: String): Either<RepoError, PokemonDetail>
}
