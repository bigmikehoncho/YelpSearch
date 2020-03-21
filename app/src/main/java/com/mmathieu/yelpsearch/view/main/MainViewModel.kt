package com.mmathieu.yelpsearch.view.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mmathieu.yelpsearch.model.BusinessCellData
import com.mmathieu.yelpsearch.model.Lce
import com.mmathieu.yelpsearch.repository.BusinessData
import com.mmathieu.yelpsearch.repository.YelpRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


class MainViewModel @Inject constructor(private val yelpRepository: YelpRepository) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val querySub = PublishSubject.create<String>()
    private val loadMoreSub = PublishSubject.create<Int>()

    val businesses = MutableLiveData<List<BusinessCellData>>()
    val isLoading = MutableLiveData<Boolean>()
    val isLoadingMore = MutableLiveData<Boolean>()

    init {
        isLoading.value = false
        isLoadingMore.value = false

        disposables.add(
            querySub.switchMap { yelpRepository.searchBusinesses(it) }
                .subscribe { response: Lce<BusinessData> ->
                    when (response) {
                        is Lce.Loading -> {
                            isLoading.postValue(true)
                            businesses.postValue(emptyList())
                        }
                        is Lce.Content -> {
                            isLoading.postValue(false)
                            val cells = response.content.createBusinessCells()
                            businesses.postValue(cells)
                        }
                        is Lce.Error -> {
                            isLoading.postValue(false)
                        }
                    }
                }
        )

        disposables.add(
            loadMoreSub.withLatestFrom(
                    querySub,
                    BiFunction<Int, String, Pair<Int, String>> { a, b -> Pair(a, b) })
                .switchMap { (size, query) -> yelpRepository.searchBusinesses(query, size) }
                .subscribe { response: Lce<BusinessData> ->
                    when (response) {
                        is Lce.Loading -> {
                            isLoadingMore.postValue(true)
                        }
                        is Lce.Content -> {
                            isLoadingMore.postValue(false)
                            val cells = response.content.createBusinessCells()
                            businesses.postValue((businesses.value ?: emptyList()) + cells)
                        }
                        is Lce.Error -> {
                            isLoadingMore.postValue(false)
                        }
                    }
                }
        )
    }

    fun onQuerySubmitted(query: String) {
        querySub.onNext(query)
    }

    fun onLoadMore(size: Int) {
        if (isLoadingMore.value != true) {
            loadMoreSub.onNext(size)
        }
    }

    private fun BusinessData.createBusinessCells() = businesses.map { business ->
        BusinessCellData(
            id = business.id,
            name = business.name,
            imageUrl = business.image_url,
            review = reviews[business.id]?.firstOrNull()?.text ?: ""
        )
    }

    override fun onCleared() {
        disposables.dispose()
        super.onCleared()
    }
}