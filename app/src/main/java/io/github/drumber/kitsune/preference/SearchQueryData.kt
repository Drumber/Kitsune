package io.github.drumber.kitsune.preference

class SearchQueryData(
    initialQueries: List<String>,
    val maxSavedQueries: Int = 30,
    private val callback: (List<String>) -> Unit
) {

    private val _searchQueries = initialQueries.toMutableList()

    fun getSearchQueries() = _searchQueries.toList()

    fun addQuery(query: String) {
        _searchQueries.remove(query)
        _searchQueries.add(0, query)
        checkSize()
        callback(getSearchQueries())
    }

    fun removeQuery(query: String) {
        if(_searchQueries.remove(query)) {
            callback(getSearchQueries())
        }
    }

    fun findSuggestions(query: String): List<String> {
        return getSearchQueries().filter {
            query.isBlank() || it.startsWith(query, ignoreCase = true)
        }
    }

    private fun checkSize() {
        if(_searchQueries.size > maxSavedQueries) {
            _searchQueries.subList(0, maxSavedQueries)
        }
    }

}