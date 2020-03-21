package com.mmathieu.yelpsearch.model

data class BusinessResponse(val total: Int, val businesses: List<Business> = emptyList())