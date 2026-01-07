package com.minddistrict.multiplatformpoc.features.pokemonlist

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.PokemonPage
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.RepoError

interface PokemonListRepository {
    suspend fun loadPage(limit: Int = 20, offset: Int = 0): Either<RepoError, PokemonPage>
    suspend fun loadPageByUrl(url: String): Either<RepoError, PokemonPage>
}
