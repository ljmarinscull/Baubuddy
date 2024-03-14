package com.ljmarinscull.baubuddy.ui.home


enum class FilterType(val pattern: String){
    AVAILABLE("${CompoundQuery.KEY}=true"){
        override fun value() = true
    },
    NOT_AVAILABLE("${CompoundQuery.KEY}=false"){
        override fun value() = false
    },
    SIMPLE(""){
        override fun value() = null
    };

    abstract fun value(): Boolean?
    companion object {
        fun filterFrom(value: String): FilterType {
            FilterType.entries.forEach {
                if(it.pattern == value){
                    return it
                }
            }
            return SIMPLE
        }
    }
}


data class CompoundQuery(
    val text: String = "",
    val filterType: FilterType = FilterType.SIMPLE
){
    companion object {
        const val KEY = "able"
        const val SEPARATOR = '+'
    }
}