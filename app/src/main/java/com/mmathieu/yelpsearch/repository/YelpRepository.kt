package com.mmathieu.yelpsearch.repository

import com.mmathieu.yelpsearch.api.YelpService
import com.mmathieu.yelpsearch.model.Business
import com.mmathieu.yelpsearch.model.Lce
import com.mmathieu.yelpsearch.model.Review
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import javax.inject.Inject

data class BusinessData(val businesses: List<Business>, val reviews: Map<String, List<Review>>)

class YelpRepository @Inject constructor(private val yelpService: YelpService) {

    private val executors =
        Executors.newFixedThreadPool(1) // Potentially run requests in parallel

    private fun getReviewsForBusiness(id: String): Single<List<Review>> {
        return yelpService.getBusinessReviews(id)
            .map { it.reviews }
            .subscribeOn(Schedulers.from(executors)) // Initially ran all review requests in parallel but started getting 429 errors from yelp so had to run them sequentially
    }

    private fun getReviewsForEachBusiness(businesses: List<Business>): Single<BusinessData> {
        return Observable.fromIterable(businesses)
            .flatMapSingle { business ->
                getReviewsForBusiness(business.id)
                    .map { business.id to it }
            }.toMap({ it.first }, { it.second })
            .map { BusinessData(businesses, it) }
    }

    @Suppress("USELESS_CAST")
    fun searchBusinesses(
        term: String,
        offset: Int = 0
    ): Observable<Lce<BusinessData>> {
        return yelpService.searchBusinesses(LOCATION, term, LIMIT, offset)
            .map { it.businesses }
            .flatMap { getReviewsForEachBusiness(it) }
            .map { Lce.Content(it) as Lce<BusinessData> }
            .onErrorReturn { Lce.Error(it.message ?: "") }
            .toObservable()
            .subscribeOn(Schedulers.from(executors))
            .startWith(Lce.Loading())
    }

    companion object {
        const val LOCATION = "Irvine"
        const val LIMIT = 20
    }
}