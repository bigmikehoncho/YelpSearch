package com.mmathieu.yelpsearch.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mmathieu.yelpsearch.TestSchedulerRule
import com.mmathieu.yelpsearch.common.Lce
import com.mmathieu.yelpsearch.repository.BusinessData
import com.mmathieu.yelpsearch.repository.YelpRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @Rule
    @JvmField
    val scheduler = TestSchedulerRule()

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val yelpRepository = mockk<YelpRepository> {
        every { searchBusinesses(any(), any()) } returns Observable.just(
            Lce.Loading(),
            Lce.Content(BusinessData(emptyList(), emptyMap()))
        )
    }

    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setup() {
        mainViewModel = MainViewModel(yelpRepository)
    }

    @Test
    fun `should search when query fired`() {
        mainViewModel.onQuerySubmitted("American")

        verify {
            yelpRepository.searchBusinesses("American")
        }
    }

    @Test
    fun `should load more businesses when load more fired`() {
        mainViewModel.onQuerySubmitted("American")
        mainViewModel.onLoadMore(20)

        verify {
            yelpRepository.searchBusinesses("American", 20)
        }
    }
}