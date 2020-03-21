package com.mmathieu.yelpsearch.api

import com.mmathieu.yelpsearch.model.BusinessResponse
import com.mmathieu.yelpsearch.model.ReviewsResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YelpService {

    @GET("businesses/search")
    fun searchBusinesses(
        @Query("location") location: String,
        @Query("term") term: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Single<BusinessResponse>

    @GET("businesses/{id}/reviews")
    fun getBusinessReviews(
        @Path("id") businessId: String
    ): Single<ReviewsResponse>


    companion object {
        const val BASE_URL = "https://api.yelp.com/v3/"
        const val API_KEY =
            "i68rhh0WPoMKRIh_h2wBdxHuysIirmV2WNKcVmWG2eHd9oAKZ6Dz3HfRiHgoFIte3oYCHEw0X8iFxnUnERU_pbhWTGPfSetqoIQjkFK_mAsNcaQtjOFE7JvvyMFyXnYx"
    }
}