package io.github.drumber.kitsune.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment
import io.github.drumber.kitsune.util.initPaddingWindowInsetsListener

class LibrariesFragment : LibsSupportFragment(), NavigationBarView.OnItemReselectedListener {

    private var recyclerView: RecyclerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(com.mikepenz.aboutlibraries.R.id.cardListView)
        recyclerView?.initPaddingWindowInsetsListener(left = true, top = true, right = true)
        recyclerView?.clipToPadding = false
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        recyclerView?.smoothScrollToPosition(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        recyclerView = null
    }

}