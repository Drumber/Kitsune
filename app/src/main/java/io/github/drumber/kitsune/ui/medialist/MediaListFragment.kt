package io.github.drumber.kitsune.ui.medialist

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.model.media.MediaAdapter
import io.github.drumber.kitsune.databinding.FragmentMediaListBinding
import io.github.drumber.kitsune.databinding.LayoutResourceLoadingBinding
import io.github.drumber.kitsune.ui.base.MediaCollectionFragment
import io.github.drumber.kitsune.ui.base.MediaCollectionViewModel
import io.github.drumber.kitsune.util.extensions.navigateSafe
import io.github.drumber.kitsune.util.initWindowInsetsListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaListFragment : MediaCollectionFragment(R.layout.fragment_media_list) {

    private val args: MediaListFragmentArgs by navArgs()

    private val binding: FragmentMediaListBinding by viewBinding()

    private val viewModel: MediaListViewModel by viewModel()

    override val collectionViewModel: MediaCollectionViewModel
        get() = viewModel

    override val recyclerView: RecyclerView
        get() = binding.rvMedia

    override val resourceLoadingBinding: LayoutResourceLoadingBinding?
        get() = binding.layoutLoading

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setMediaSelector(args.mediaSelector)

        binding.toolbar.apply {
            initWindowInsetsListener(false)
            title = args.title
            setNavigationOnClickListener { findNavController().navigateUp() }
        }
    }

    override fun onMediaClicked(model: MediaAdapter) {
        val action = MediaListFragmentDirections.actionMediaListFragmentToDetailsFragment(model)
        findNavController().navigateSafe(R.id.media_list_fragment, action)
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        if (recyclerView.canScrollVertically(-1)) {
            binding.appBarLayout.setExpanded(true)
            super.onNavigationItemReselected(item)
        } else {
            findNavController().navigateUp()
        }
    }

}