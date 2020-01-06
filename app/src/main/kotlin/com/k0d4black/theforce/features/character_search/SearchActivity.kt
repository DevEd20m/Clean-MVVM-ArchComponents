package com.k0d4black.theforce.features.character_search

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.k0d4black.theforce.R
import com.k0d4black.theforce.domain.utils.Error
import com.k0d4black.theforce.domain.utils.Loading
import com.k0d4black.theforce.domain.utils.Success
import com.k0d4black.theforce.features.character_details.CharacterDetailActivity
import com.k0d4black.theforce.models.SearchedCharacterPresentationModel
import com.k0d4black.theforce.utils.*
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_search.*
import javax.inject.Inject


class SearchActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val characterSearchViewModel: CharacterSearchViewModel by viewModels { viewModelFactory }

    private val searchResultAdapter: SearchResultAdapter by lazy {
        SearchResultAdapter { characterId ->
            Intent(this, CharacterDetailActivity::class.java).apply {
                putExtra(CHARACTER_ID_KEY, characterId)
            }.also { startActivity(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        observeSearchResults()
    }

    private fun observeSearchResults() {
        characterSearchViewModel.searchResults.observe(this, Observer {
            when (it) {
                is Success -> displayDataLoadedState(it.data)
                is Error -> displayErrorState(it.exception)
                is Loading -> displayLoadingState()
            }
        })
    }

    private fun displayLoadingState() {
        loading_search_results_progress_bar.show()
        search_tip_text_view.hide()
    }

    private fun displayDataLoadedState(searchResults: List<SearchedCharacterPresentationModel>) {
        loading_search_results_progress_bar.hide()
        if (searchResults.isNotEmpty()) {
            if (search_tip_text_view.isVisible) search_tip_text_view.hide()
            search_results_recycler_view.show()
            search_results_recycler_view.apply {
                adapter = searchResultAdapter.apply { submitList(searchResults) }
                initRecyclerViewWithLineDecoration(this@SearchActivity)
            }
        } else displayEmptyDataState()
    }

    private fun displayEmptyDataState() {
        search_tip_text_view.show()
        search_results_recycler_view.hide()
        showSnackbar(
            search_results_recycler_view,
            getString(R.string.info_no_results)
        )
    }

    private fun displayErrorState(error: Throwable) {
        search_results_recycler_view.hide()
        loading_search_results_progress_bar.hide()
        search_tip_text_view.show()
        showSnackbar(search_results_recycler_view, "${error.message}")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(
            SearchQueryListener(characterSearchViewModel)
        )
        return super.onCreateOptionsMenu(menu)
    }

}
