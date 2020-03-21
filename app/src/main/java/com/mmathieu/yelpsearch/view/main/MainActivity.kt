package com.mmathieu.yelpsearch.view.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mmathieu.yelpsearch.R
import com.mmathieu.yelpsearch.YelpSearchApp
import com.mmathieu.yelpsearch.model.BusinessCellData
import com.mmathieu.yelpsearch.view.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var mainViewModel: MainViewModel

    private lateinit var adapter: BusinessesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        YelpSearchApp.yelpSearchApp.appComponent.inject(this) // For a production app I would abstract this via dagger-android library.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = BusinessesAdapter()
        businesses.adapter = adapter
        businesses.addOnScrollListener(scrollListener)

        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        mainViewModel.businesses.observe(this, Observer {
            adapter.submitList(it)
        })

        mainViewModel.isLoading.observe(this, Observer {
            progress_bar.visibility = if (it) View.VISIBLE else View.GONE
        })

        mainViewModel.isLoadingMore.observe(this, Observer {
            load_more.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                mainViewModel.onQuerySubmitted(query)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isIconifiedByDefault = false
            isIconified = false
            clearFocus()
            requestFocusFromTouch()
        }

        return true
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        var loading = false

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val llm = recyclerView.layoutManager as LinearLayoutManager
            if (!loading && dy > 0 && llm.findLastVisibleItemPosition() >= llm.itemCount - 5) {
                loading = true
                mainViewModel.onLoadMore(adapter.itemCount)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                loading = false
            }
        }
    }

    private class BusinessesAdapter :
        ListAdapter<BusinessCellData, BusinessViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.business_cell, parent, false) as ViewGroup
            return BusinessViewHolder(view)
        }

        override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
            getItem(position)?.let { holder.bind(it) }
        }

        companion object {
            private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<BusinessCellData>() {
                override fun areItemsTheSame(
                    oldItem: BusinessCellData,
                    newItem: BusinessCellData
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: BusinessCellData,
                    newItem: BusinessCellData
                ): Boolean {
                    return oldItem == newItem
                }
            }
        }
    }

    private class BusinessViewHolder(private val view: ViewGroup) : RecyclerView.ViewHolder(view) {

        private val imageView = view.findViewById<ImageView>(R.id.image)
        private val name = view.findViewById<TextView>(R.id.name)
        private val review = view.findViewById<TextView>(R.id.review)

        fun bind(business: BusinessCellData) {
            if (business.imageUrl.isNotEmpty()) {
                Glide.with(view.context).load(business.imageUrl).into(imageView)
            } else {
                imageView.setImageDrawable(null)
            }

            name.text = business.name
            review.text = business.review
        }
    }
}
