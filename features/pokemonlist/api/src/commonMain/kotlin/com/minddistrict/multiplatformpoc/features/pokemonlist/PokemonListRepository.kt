package com.minddistrict.multiplatformpoc.features.pokemonlist

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.PokemonPage
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.RepoError

interface PokemonListRepository {
    /**
     * Load a page of Pokemon.
     * @param limit Number of items per page
     * @param offset Starting index
     */
    suspend fun loadPage(limit: Int = 20, offset: Int = 0): Either<RepoError, PokemonPage>
}
