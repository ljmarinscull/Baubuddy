package com.ljmarinscull.baubuddy.ui.home

sealed class FilterType {
    data class Query(val query: String = "") : FilterType()
    data class Availability(val isAvailable: Boolean) : FilterType()
    data class CompoundQuery(val query: String, val isAvailable: Boolean) : FilterType()

    companion object {
        fun filterAvailabilityFrom(value: String): Availability? {
            if (AVAILABLE_RESOURCE == value)
                return Availability(true)
            if (NOT_AVAILABLE_RESOURCE == value)
                return Availability(false)
            return null
        }

        private const val AVAILABLE_RESOURCE = "able=true"
        private const val NOT_AVAILABLE_RESOURCE = "able=false"
        const val SEPARATOR = '+'
    }
}