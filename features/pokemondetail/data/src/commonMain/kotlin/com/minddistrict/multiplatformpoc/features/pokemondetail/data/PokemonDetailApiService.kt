package com.minddistrict.multiplatformpoc.features.pokemondetail.data

import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.PokemonDetailDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface PokemonDetailApiService {
    suspend fun getPokemonDetailByUrl(url: String): PokemonDetailDto
}

internal class PokemonDetailApiServiceImpl(
    private val httpClient: HttpClient
) : PokemonDetailApiService {
    
    override suspend fun getPokemonDetailByUrl(url: String): PokemonDetailDto {
        return httpClient.get(url).body()
    }
}

fun PokemonDetailApiService(
    httpClient: HttpClient
): PokemonDetailApiService = PokemonDetailApiServiceImpl(httpClient)
