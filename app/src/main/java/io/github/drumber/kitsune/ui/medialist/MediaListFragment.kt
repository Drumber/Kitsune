package io.github.drumber.kitsune.ui.medialist

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialSharedAxis
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.common.media.MediaType
import io.github.drumber.kitsune.data.presentation.dto.toMediaDto
import io.github.drumber.kitsune.data.presentation.model.media.Media
import io.github.drumber.kitsune.databinding.FragmentMediaListBinding
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.ui.base.MediaCollectionFragment
import io.github.drumber.kitsune.ui.base.MediaCollectionViewModel
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaListFragment : MediaCollectionFragment(R.layout.fragment_media_list) {

    private val args: MediaListFragmentArgs by navArgs()

    private val binding: FragmentMediaListBinding by viewBinding()

    private val viewModel: MediaListViewModel by viewModel()

    override val collectionViewModel: MediaCollectionViewModel
        get() = viewModel

    override val recyclerView: RecyclerView
        get() = binding.rvMedia

    override val resourceLoadingBinding: LayoutResourceLoadingBinding
        get() = binding.layoutLoading

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        val colorBackground = MaterialColors.getColor(view, android.R.attr.colorBackground)
        view.setBackgroundColor(colorBackground)

        binding.rvMedia.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            bottom = true,
            consume = false
        )

        viewModel.setMediaSelector(args.mediaSelector)

        binding.collapsingToolbar.initWindowInsetsListener(consume = false)
        binding.toolbar.apply {
            initWindowInsetsListener(consume = false)
            title = args.title
            setNavigationOnClickListener { findNavController().navigateUp() }
        }
    }

    override fun onMediaClicked(view: View, model: Media) {
        val action = MediaListFragmentDirections.actionMediaListFragmentToDetailsFragment(model.toMediaDto())
        val detailsTransitionName = getString(R.string.details_poster_transition_name)
        val extras = FragmentNavigatorExtras(view to detailsTransitionName)
        findNavController().navigateSafe(R.id.media_list_fragment, action, extras)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (recyclerView.canScrollVertically(-1)) {
            binding.appBarLayout.setExpanded(true)
            super.onNavigationItemReselected(item)
        } else {
            findNavController().navigateUp()
        }
    }

    override fun getMediaType(): MediaType {
        return args.mediaSelector.mediaType
    }
}