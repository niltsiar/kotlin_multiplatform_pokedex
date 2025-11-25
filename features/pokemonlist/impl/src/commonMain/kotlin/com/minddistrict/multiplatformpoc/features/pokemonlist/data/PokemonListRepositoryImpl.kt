package com.minddistrict.multiplatformpoc.features.pokemonlist.data

import arrow.core.Either
import arrow.core.raise.catch
import com.minddistrict.multiplatformpoc.features.pokemonlist.PokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.mappers.toDomain
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.PokemonPage
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.RepoError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class PokemonListRepositoryImpl(
    private val apiService: PokemonListApiService
) : PokemonListRepository {
    
    override suspend fun loadPage(limit: Int, offset: Int): Either<RepoError, PokemonPage> =
        withContext(Dispatchers.IO) {
            catch({
                val dto = apiService.getPokemonList(limit, offset)
                Either.Right(dto.toDomain())
            }) { throwable ->
                Either.Left(throwable.toRepoError())
            }
        }
}

// Factory function - public for wiring module
fun PokemonListRepository(
    apiService: PokemonListApiService
): PokemonListRepository = PokemonListRepositoryImpl(apiService)

// Error mapping
private fun Throwable.toRepoError(): RepoError = when (this) {
    is ClientRequestException -> {
        if (response.status.isSuccess()) {
            RepoError.Unknown(this)
        } else {
            RepoError.Http(response.status.value, message)
        }
    }
    is java.io.IOException -> RepoError.Network
    else -> RepoError.Unknown(this)
}
