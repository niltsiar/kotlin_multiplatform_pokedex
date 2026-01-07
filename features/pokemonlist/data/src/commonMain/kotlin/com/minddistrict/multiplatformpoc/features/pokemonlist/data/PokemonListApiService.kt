package com.minddistrict.multiplatformpoc.features.pokemonlist.data

import com.minddistrict.multiplatformpoc.features.pokemonlist.data.dto.PokemonListDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

interface PokemonListApiService {
    suspend fun getPokemonList(limit: Int, offset: Int): PokemonListDto
    suspend fun getPokemonListByUrl(url: String): PokemonListDto
}

internal class PokemonListApiServiceImpl(
    private val httpClient: HttpClient,
    private val initialBaseUrl: String = "https://pokeapi.co/api/v2"
) : PokemonListApiService {
    
    override suspend fun getPokemonList(limit: Int, offset: Int): PokemonListDto {
        return httpClient.get("$initialBaseUrl/pokemon/") {
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
    }
    
    override suspend fun getPokemonListByUrl(url: String): PokemonListDto {
        return httpClient.get(url).body()
    }
}

fun PokemonListApiService(
    httpClient: HttpClient,
    baseUrl: String = "https://pokeapi.co/api/v2"
): PokemonListApiService = PokemonListApiServiceImpl(httpClient, baseUrl)
