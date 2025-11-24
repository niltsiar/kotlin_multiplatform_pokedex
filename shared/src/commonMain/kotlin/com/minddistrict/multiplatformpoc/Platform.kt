package com.minddistrict.multiplatformpoc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform