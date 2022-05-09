package com.anfema.ionclient

/**
 * Defines strategies, when to fetch data from cache and when to download it from internet.
 */
enum class CachingStrategy {
    /**
     * strategy:
     * 1. fetch current version from cache
     * 2. download current version (if connected to internet)
     * 3. fetch possibly outdated version from cache (if it exists)
     * 4. error (because no version in cache exists and no internet connection)
     */
    NORMAL,

    /**
     * strategy:
     * 1. fetch (possibly outdated) version from cache (if it exists)
     * 2. error (because no version in cache exists and downloading is prohibited with this mode)
     */
    STRICT_OFFLINE
}
