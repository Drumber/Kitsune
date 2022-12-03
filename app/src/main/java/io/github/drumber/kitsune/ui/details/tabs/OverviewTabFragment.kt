package io.github.drumber.kitsune.ui.details.tabs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.chip.Chip
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.constants.MediaItemSize
import io.github.drumber.kitsune.constants.SortFilter
import io.github.drumber.kitsune.data.model.MediaSelector
import io.github.drumber.kitsune.data.model.MediaType
import io.github.drumber.kitsune.data.model.category.Category
import io.github.drumber.kitsune.data.model.media.Anime
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.data.service.Filter
import io.github.drumber.kitsune.databinding.FragmentDetailsOverviewBinding
import io.github.drumber.kitsune.ui.adapter.MediaRecyclerViewAdapter
import io.github.drumber.kitsune.ui.adapter.MediaViewHolder
import io.github.drumber.kitsune.ui.adapter.StreamingLinkAdapter
import io.github.drumber.kitsune.ui.details.DetailsFragmentDirections
import io.github.drumber.kitsune.ui.details.DetailsViewModel
import io.github.drumber.kitsune.ui.widget.chart.BarChartStyle
import io.github.drumber.kitsune.ui.widget.chart.BarChartStyle.applyStyle
import io.github.drumber.kitsune.ui.widget.chart.StepAxisValueFormatter
import io.github.drumber.kitsune.util.extensions.navigateSafe
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.CopyOnWriteArrayList

class OverviewTabFragment : Fragment() {

    private var _binding: FragmentDetailsOverviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailsViewModel by viewModel(ownerProducer = { requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.mediaAdapter.observe(viewLifecycleOwner) { model ->
            binding.data = model

            showCategoryChips(model)
            showFranchise(model)
            showStreamingLinks(model)
            showRatingChart(model)
        }

        binding.apply {
            btnMediaUnits.setOnClickListener {
                val media = viewModel.mediaAdapter.value?.media ?: return@setOnClickListener
                val libraryEntry = viewModel.libraryEntry.value
                val action = DetailsFragmentDirections.actionDetailsFragmentToEpisodesFragment(
                    media,
                    libraryEntry?.id
                )
                findNavController().navigate(action)
            }
            btnCharacters.setOnClickListener {
                val media = viewModel.mediaAdapter.value?.media ?: return@setOnClickListener
                val action = DetailsFragmentDirections.actionDetailsFragmentToCharactersFragment(
                    media.id,
                    media is Anime
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun showCategoryChips(mediaAdapter: MediaAdapter) {
        if (!mediaAdapter.categories.isNullOrEmpty()) {
            binding.chipGroupCategories.removeAllViews()

            mediaAdapter.categories.orEmpty()
                .sortedBy { it.title }
                .forEach { category ->
                    val chip = Chip(requireContext())
                    chip.text = category.title
                    chip.setOnClickListener {
                        onCategoryChipClicked(category, mediaAdapter)
                    }
                    binding.chipGroupCategories.addView(chip)
                }
        }
    }

    private fun onCategoryChipClicked(category: Category, mediaAdapter: MediaAdapter) {
        val categorySlug = category.slug ?: return
        val title = category.title ?: getString(R.string.no_information)

        val mediaSelector = MediaSelector(
            if (mediaAdapter.isAnime()) MediaType.Anime else MediaType.Manga,
            Filter()
                .filter("categories", categorySlug)
                .sort(SortFilter.POPULARITY_DESC.queryParam)
        )

        val action =
            DetailsFragmentDirections.actionDetailsFragmentToMediaListFragment(mediaSelector, title)
        findNavController().navigate(action)
    }

    private fun showFranchise(mediaAdapter: MediaAdapter) {
        val data = mediaAdapter.media.mediaRelationships?.sortedBy {
            it.role?.ordinal
        }?.mapNotNull {
            it.media?.let { media -> MediaAdapter.fromMedia(media, it.role) }
        } ?: emptyList()

        if (binding.rvFranchise.adapter !is MediaRecyclerViewAdapter) {
            val glide = GlideApp.with(this)
            val adapter = MediaRecyclerViewAdapter(
                CopyOnWriteArrayList(data),
                glide,
                MediaViewHolder.TagData.RelationshipRole
            ) { media ->
                onFranchiseItemClicked(media)
            }
            adapter.overrideItemSize = MediaItemSize.SMALL
            binding.rvFranchise.adapter = adapter
        } else {
            val adapter = binding.rvFranchise.adapter as MediaRecyclerViewAdapter
            adapter.dataSet.addAll(0, data)
            adapter.notifyDataSetChanged()
        }
    }

    private fun onFranchiseItemClicked(mediaAdapter: MediaAdapter) {
        val action = DetailsFragmentDirections.actionDetailsFragmentSelf(mediaAdapter)
        findNavController().navigateSafe(R.id.details_fragment, action)
    }

    private fun showStreamingLinks(mediaAdapter: MediaAdapter) {
        val data = (mediaAdapter.media as? Anime)?.streamingLinks ?: emptyList()

        if (binding.rvStreamer.adapter !is StreamingLinkAdapter) {
            val glide = GlideApp.with(this)
            val adapter = StreamingLinkAdapter(CopyOnWriteArrayList(data), glide) { streamingLink ->
                streamingLink.url?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }
            binding.rvStreamer.adapter = adapter
        } else {
            val adapter = binding.rvStreamer.adapter as StreamingLinkAdapter
            adapter.dataSet.addAll(0, data)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showRatingChart(mediaAdapter: MediaAdapter) {
        val ratings = mediaAdapter.media.ratingFrequencies ?: return


        val displayWidth = resources.displayMetrics.widthPixels
        // full chart shows advanced ratings (1-10); reduced chart with shows regular rating (0.5-5)
        val isFullChart =
            displayWidth >= resources.getDimensionPixelSize(R.dimen.details_rating_chart_full_threshold)

        val ratingList = with(ratings) {
            val list = listOf(
                r2,
                r3,
                r4,
                r5,
                r6,
                r7,
                r8,
                r9,
                r10,
                r11,
                r12,
                r13,
                r14,
                r15,
                r16,
                r17,
                r18,
                r19,
                r20
            )

            if (isFullChart) {
                list
            } else {
                var previous = 0
                list.mapIndexedNotNull { index, s ->
                    if (index % 2 == 0) {
                        (previous + (s?.toInt() ?: 0)).toString()
                    } else {
                        previous = s?.toInt() ?: 0
                        null
                    }
                }
            }
        }

        val chartEntries = ratingList.mapIndexed { index, s ->
            BarEntry(index.toFloat(), s?.toFloat() ?: 0f)
        }

        val dataSet = BarDataSet(chartEntries, "Ratings")
        val chartColorArray = BarChartStyle
            .getColorArray(requireContext(), R.array.ratings_chart_colors)
            .filterIndexed { index, _ ->
                isFullChart || index % 2 == 0
            }
        dataSet.applyStyle(requireContext(), chartColorArray)

        val barData = BarData(dataSet)
        barData.applyStyle(requireContext())

        binding.chartRatings.apply {
            data = barData
            applyStyle(requireContext())
            setFitBars(true)
            xAxis.valueFormatter = StepAxisValueFormatter(if (isFullChart) 1f else 0.5f, 0.5f)
            xAxis.labelCount = if (isFullChart) 19 else 10
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}