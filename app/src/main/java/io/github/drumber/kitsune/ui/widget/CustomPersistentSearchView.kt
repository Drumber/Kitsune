package io.github.drumber.kitsune.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.paulrybitskyi.persistentsearchview.PersistentSearchView
import com.paulrybitskyi.persistentsearchview.listeners.OnSearchConfirmedListener
import com.paulrybitskyi.persistentsearchview.widgets.AdvancedEditText
import io.github.drumber.kitsune.util.extensions.getResourceId

@SuppressLint("ClickableViewAccessibility")
class CustomPersistentSearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : PersistentSearchView(context, attrs) {

    var customOnSearchConfirmedListener: OnSearchConfirmedListener? = null
    set(value) {
        field = value
        setOnSearchConfirmedListener(value)
    }

    private var expandStateListener: ((Boolean) -> Unit)? = null

    fun setOnExpandStateChangeListener(listener: (expanded: Boolean) -> Unit) {
        expandStateListener = listener
    }

    init {
        val inputEt: AdvancedEditText = findViewById(com.paulrybitskyi.persistentsearchview.R.id.inputEt)
        val cardView: CardView = findViewById(com.paulrybitskyi.persistentsearchview.R.id.cardView)
        val suggestionsRV: RecyclerView = findViewById(com.paulrybitskyi.persistentsearchview.R.id.suggestionsRecyclerView)

        inputEt.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                customOnSearchConfirmedListener?.onSearchConfirmed(this, inputQuery)
            }
            true
        }
        inputEt.setTouchEventInterceptor { _, motionEvent ->
            if(!isExpanded) {
                cardView.onTouchEvent(motionEvent)
            } else {
                false
            }
        }

        cardView.foreground = ContextCompat.getDrawable(context, context.theme.getResourceId(android.R.attr.selectableItemBackground))
        cardView.setOnClickListener {
            expand()
        }

        suggestionsRV.isNestedScrollingEnabled = false

        initExpandListener()
    }

    private fun initExpandListener() {
        var wasExpanded = isExpanded
        val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            if(isExpanded != wasExpanded) {
                wasExpanded = isExpanded
                expandStateListener?.invoke(wasExpanded)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        post {
            doOnDetach {
                viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            }
        }
    }

    /**
     * Disable dragging for the specified app bar layout while the search view is expanded.
     */
    fun setAppBarLayout(appBarLayout: AppBarLayout) {
        appBarLayout.post {
            val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior as AppBarLayout.Behavior

            val appBarDragCallback = object : AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return !isExpanded
                }
            }
            behavior.setDragCallback(appBarDragCallback)
            appBarLayout.doOnDetach {
                behavior.setDragCallback(null)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        expandStateListener = null
        customOnSearchConfirmedListener = null
    }

}