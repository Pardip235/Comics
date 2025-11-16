package com.bpn.comics.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

