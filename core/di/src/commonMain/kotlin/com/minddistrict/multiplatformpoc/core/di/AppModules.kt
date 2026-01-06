package com.minddistrict.multiplatformpoc.core.di

import com.minddistrict.multiplatformpoc.core.httpclient.createHttpClient
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Core application module providing base dependencies.
 *
 * Idiomatic Koin pattern: modules are defined as top-level functions/properties.
 *
 * @param baseUrl The base URL for API calls (e.g., "https://pokeapi.co/api/v2")
 */
fun coreModule(baseUrl: String) = module {
    // Provide baseUrl as a named dependency
    single(qualifier = named("baseUrl")) { baseUrl }
}

/**
 * Shared networking module exposing a single HttpClient instance that all
 * features reuse via DI.
 */
fun httpClientModule() = module {
    single<HttpClient> { createHttpClient() }
}
