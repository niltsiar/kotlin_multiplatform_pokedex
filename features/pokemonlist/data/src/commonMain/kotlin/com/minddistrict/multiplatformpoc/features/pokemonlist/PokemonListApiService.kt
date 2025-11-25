package com.minddistrict.multiplatformpoc.features.pokemonlist.data

import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonListDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

interface PokemonListApiService {
    suspend fun getPokemonList(limit: Int, offset: Int): PokemonListDto
}

internal class PokemonListApiServiceImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://pokeapi.co/api/v2"
) : PokemonListApiService {
    
    override suspend fun getPokemonList(limit: Int, offset: Int): PokemonListDto {
        return httpClient.get("$baseUrl/pokemon/") {
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
    }
}

// Factory function - public for wiring module
fun PokemonListApiService(
    httpClient: HttpClient,
    baseUrl: String = "https://pokeapi.co/api/v2"
): PokemonListApiService = PokemonListApiServiceImpl(httpClient, baseUrl)
