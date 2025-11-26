package com.minddistrict.multiplatformpoc.features.pokemondetail.domain

sealed interface RepoError {
    data object Network : RepoError
    data class Http(val code: Int, val message: String?) : RepoError
    data class Unknown(val cause: Throwable) : RepoError
}
