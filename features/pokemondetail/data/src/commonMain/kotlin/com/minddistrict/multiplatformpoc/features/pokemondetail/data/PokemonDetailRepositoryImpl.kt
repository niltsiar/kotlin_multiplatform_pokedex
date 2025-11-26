package com.minddistrict.multiplatformpoc.features.pokemondetail.data

import arrow.core.Either
import arrow.core.raise.catch
import com.minddistrict.multiplatformpoc.features.pokemondetail.PokemonDetailRepository
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.RepoError
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

internal class PokemonDetailRepositoryImpl(
    private val apiService: PokemonDetailApiService
) : PokemonDetailRepository {
    
    override suspend fun getDetail(id: Int): Either<RepoError, PokemonDetail> =
        withContext(Dispatchers.IO) {
            catch({
                val dto = apiService.getPokemonDetail(id)
                Either.Right(dto.asDomain())
            }) { throwable ->
                Either.Left(throwable.toRepoError())
            }
        }
}

// Factory function - public for wiring module
fun PokemonDetailRepository(
    apiService: PokemonDetailApiService
): PokemonDetailRepository = PokemonDetailRepositoryImpl(apiService)

// Error mapping using Ktor's multiplatform exceptions
private fun Throwable.toRepoError(): RepoError = when (this) {
    is ClientRequestException -> RepoError.Http(response.status.value, message)
    is ServerResponseException -> RepoError.Http(response.status.value, message)
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException -> RepoError.Network
    else -> RepoError.Unknown(this)
}
