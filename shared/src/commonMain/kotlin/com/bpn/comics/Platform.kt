package com.bpn.comics

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

