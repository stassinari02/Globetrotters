package com.example.globetrotters.api

data class WikiResponse(
    val query: Query?
)

data class Query(
    val pages: Map<String, Page>?
)

data class Page(
    val extract: String?
)
