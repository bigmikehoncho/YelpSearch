package com.mmathieu.yelpsearch.repository

import com.mmathieu.yelpsearch.TestSchedulerRule
import com.mmathieu.yelpsearch.api.YelpService
import com.mmathieu.yelpsearch.common.Lce
import com.mmathieu.yelpsearch.model.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test

class YelpRepositoryTest {

    @Rule
    @JvmField
    val scheduler = TestSchedulerRule()

    private val yelpService = mockk<YelpService> {
        every {
            searchBusinesses(
                YelpRepository.LOCATION,
                "American",
                YelpRepository.LIMIT,
                0
            )
        } returns Single.just(
            BusinessResponse(total = 2, businesses = businesses)
        )

        every { getBusinessReviews(any()) } returns Single.just(ReviewsResponse(emptyList()))
    }

    private val yelpRepository = YelpRepository(yelpService)

    @Test
    fun `execute search request for business data`() {
        val testObserver = yelpRepository.searchBusinesses("American", 0)
            .test()

        verify(exactly = 1) {
            yelpService.searchBusinesses(
                YelpRepository.LOCATION,
                "American",
                YelpRepository.LIMIT,
                0
            )
            yelpService.getBusinessReviews("1")
            yelpService.getBusinessReviews("2")
        }

        testObserver.assertValueAt(0) { it is Lce.Loading }
        testObserver.assertValueAt(1) {
            it == Lce.Content(
                BusinessData(
                    businesses = businesses,
                    reviewsByBusinessId = businesses
                        .map { business -> business.id to emptyList<Review>() }
                        .toMap()
                )
            )
        }
    }

    companion object {
        private fun business(id: String) = Business(
            id = id,
            name = "1",
            image_url = ""
        )

        private val businesses = listOf(business("1"), business("2"))
    }
}