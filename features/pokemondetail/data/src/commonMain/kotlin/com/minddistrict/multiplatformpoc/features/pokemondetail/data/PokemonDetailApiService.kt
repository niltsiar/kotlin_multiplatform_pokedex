package com.minddistrict.multiplatformpoc.features.pokemondetail.data

import com.minddistrict.multiplatformpoc.features.pokemondetail.data.dto.PokemonDetailDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface PokemonDetailApiService {
    suspend fun getPokemonDetail(id: Int): PokemonDetailDto
}

internal class PokemonDetailApiServiceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://pokeapi.co/api/v2"
) : PokemonDetailApiService {
    
    override suspend fun getPokemonDetail(id: Int): PokemonDetailDto {
        return httpClient.get("$baseUrl/pokemon/$id/").body()
    }
}

// Factory function - public for wiring module
fun PokemonDetailApiService(
    httpClient: HttpClient,
    baseUrl: String = "https://pokeapi.co/api/v2"
): PokemonDetailApiService = PokemonDetailApiServiceImpl(httpClient, baseUrl)
