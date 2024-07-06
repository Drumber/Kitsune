package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.Filter
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.data.presentation.model.media.MediaSelector
import io.github.drumber.kitsune.data.presentation.model.media.RequestType
import io.github.drumber.kitsune.databinding.FragmentHomeExploreBinding
import io.github.drumber.kitsune.databinding.SectionMainExploreBinding
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.adapter.OnItemClickListener
import io.github.drumber.kitsune.ui.base.BaseFragment
import io.github.drumber.kitsune.ui.widget.ExploreSection
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.network.ResponseData
import org.koin.androidx.navigation.koinNavGraphViewModel

class HomeExploreFragment : BaseFragment(R.layout.fragment_home_explore),
    OnItemClickListener<Media> {

    private val binding: FragmentHomeExploreBinding by viewBinding()

    private val viewModel: MainFragmentViewModel by koinNavGraphViewModel(R.id.main_nav_graph)

    companion object {
        const val BUNDLE_MEDIA_TYPE = "bundle_media_type"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        val mediaType = arguments?.takeIf { it.containsKey(BUNDLE_MEDIA_TYPE) }?.let {
            it.getSerializable(BUNDLE_MEDIA_TYPE) as? MediaType
        }

        if (mediaType == MediaType.Anime) {
            initAnimeExploreSections()
        } else if (mediaType == MediaType.Manga) {
            initMangaExploreSections()
        }
    }

    private fun initAnimeExploreSections() {
        // trending
        buildExploreSectionView(
            MediaType.Anime,
            R.string.section_trending,
            Filter().limit(30),
            RequestType.TRENDING,
            binding.sectionTrending,
            viewModel.getAnimeExploreLiveData(MainFragmentViewModel.TRENDING) as LiveData<ResponseData<List<Media>>>
        )

        // top airing
        buildExploreSectionView(
            MediaType.Anime,
            R.string.section_top_airing_anime,
            MainFragmentViewModel.FILTER_TOP_AIRING,
            RequestType.ALL,
            binding.sectionTopAiring,
            viewModel.getAnimeExploreLiveData(MainFragmentViewModel.TOP_AIRING) as LiveData<ResponseData<List<Media>>>
        )

        // top upcoming
        buildExploreSectionView(
            MediaType.Anime,
            R.string.section_top_upcoming_anime,
            MainFragmentViewModel.FILTER_TOP_UPCOMING,
            RequestType.ALL,
            binding.sectionTopUpcoming,
            viewModel.getAnimeExploreLiveData(MainFragmentViewModel.TOP_UPCOMING) as LiveData<ResponseData<List<Media>>>
        )

        // highest rated
        buildExploreSectionView(
            MediaType.Anime,
            R.string.section_highest_rated_anime,
            MainFragmentViewModel.FILTER_HIGHEST_RATED,
            RequestType.ALL,
            binding.sectionHighestRated,
            viewModel.getAnimeExploreLiveData(MainFragmentViewModel.HIGHEST_RATED) as LiveData<ResponseData<List<Media>>>
        )

        // most popular
        buildExploreSectionView(
            MediaType.Anime,
            R.string.section_most_popular_anime,
            MainFragmentViewModel.FILTER_MOST_POPULAR,
            RequestType.ALL,
            binding.sectionMostPopular,
            viewModel.getAnimeExploreLiveData(MainFragmentViewModel.MOST_POPULAR) as LiveData<ResponseData<List<Media>>>
        )
    }

    private fun initMangaExploreSections() {
        // trending
        buildExploreSectionView(
            MediaType.Manga,
            R.string.section_trending,
            Filter().limit(30),
            RequestType.TRENDING,
            binding.sectionTrending,
            viewModel.getMangaExploreLiveData(MainFragmentViewModel.TRENDING) as LiveData<ResponseData<List<Media>>>
        )

        // top airing
        buildExploreSectionView(
            MediaType.Manga,
            R.string.section_top_airing_manga,
            MainFragmentViewModel.FILTER_TOP_AIRING,
            RequestType.ALL,
            binding.sectionTopAiring,
            viewModel.getMangaExploreLiveData(MainFragmentViewModel.TOP_AIRING) as LiveData<ResponseData<List<Media>>>
        )

        // top upcoming
        buildExploreSectionView(
            MediaType.Manga,
            R.string.section_top_upcoming_manga,
            MainFragmentViewModel.FILTER_TOP_UPCOMING,
            RequestType.ALL,
            binding.sectionTopUpcoming,
            viewModel.getMangaExploreLiveData(MainFragmentViewModel.TOP_UPCOMING) as LiveData<ResponseData<List<Media>>>
        )

        // highest rated
        buildExploreSectionView(
            MediaType.Manga,
            R.string.section_highest_rated_manga,
            MainFragmentViewModel.FILTER_HIGHEST_RATED,
            RequestType.ALL,
            binding.sectionHighestRated,
            viewModel.getMangaExploreLiveData(MainFragmentViewModel.HIGHEST_RATED) as LiveData<ResponseData<List<Media>>>
        )

        // most popular
        buildExploreSectionView(
            MediaType.Manga,
            R.string.section_most_popular_manga,
            MainFragmentViewModel.FILTER_MOST_POPULAR,
            RequestType.ALL,
            binding.sectionMostPopular,
            viewModel.getMangaExploreLiveData(MainFragmentViewModel.MOST_POPULAR) as LiveData<ResponseData<List<Media>>>
        )
    }

    private fun buildExploreSectionView(
        mediaType: MediaType,
        @StringRes titleRes: Int,
        filter: Filter,
        requestType: RequestType,
        sectionBinding: SectionMainExploreBinding,
        liveData: LiveData<ResponseData<List<Media>>>
    ): ExploreSection {
        sectionBinding.apply {
            rvMedia.isVisible = false
            layoutLoading.apply {
                root.layoutParams.height =
                    resources.getDimensionPixelSize(KitsunePref.mediaItemSize.heightRes)
                tvError.isVisible = false
                btnRetry.isVisible = false
                root.isVisible = true
            }
        }

        val mediaSelector = MediaSelector(mediaType, filter.options, requestType)
        val section = createExploreSection(titleRes, mediaSelector, sectionBinding.root)

        liveData.observe(viewLifecycleOwner) { response ->
            if (response is ResponseData.Success) {
                section.setData(response.data)
                sectionBinding.apply {
                    layoutLoading.root.isVisible = false
                    rvMedia.isVisible = true
                }
            } else {
                sectionBinding.apply {
                    rvMedia.isVisible = false
                    layoutLoading.apply {
                        root.isVisible = true
                        tvError.isVisible = true
                        progressBar.isVisible = false
                    }
                }
            }

        }
        return section
    }

    private fun createExploreSection(
        @StringRes titleRes: Int,
        mediaSelector: MediaSelector,
        view: View
    ): ExploreSection {
        val title = getString(titleRes)
        val glide = Glide.with(this)

        val section = ExploreSection(glide, title, null, this) {
            val action =
                MainFragmentDirections.actionMainFragmentToMediaListFragment(mediaSelector, title)
            findNavController().navigateSafe(R.id.main_fragment, action)
        }
        section.bindView(view)
        return section
    }

    override fun onItemClick(view: View, item: Media) {
        val action = MainFragmentDirections.actionMainFragmentToDetailsFragment(item.toMediaDto())
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.main_fragment, action, extras)
    }

}