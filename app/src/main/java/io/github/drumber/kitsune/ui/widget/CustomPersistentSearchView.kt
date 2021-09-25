package io.github.drumber.kitsune.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout
import com.paulrybitskyi.persistentsearchview.PersistentSearchView
import com.paulrybitskyi.persistentsearchview.listeners.OnSearchConfirmedListener
import com.paulrybitskyi.persistentsearchview.widgets.AdvancedEditText
import io.github.drumber.kitsune.util.getResourceId

class CustomPersistentSearchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : PersistentSearchView(context, attrs) {

    private val inputEt: AdvancedEditText = findViewById(com.paulrybitskyi.persistentsearchview.R.id.inputEt)
    private val cardView: CardView = findViewById(com.paulrybitskyi.persistentsearchview.R.id.cardView)

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
        inputEt.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                customOnSearchConfirmedListener?.onSearchConfirmed(this, inputQuery)
            }
            true
        }
        inputEt.setTouchEventInterceptor { view, motionEvent ->
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

        initExpandListener()
    }

    private fun initExpandListener() {
        var wasExpanded = isExpanded
        viewTreeObserver.addOnGlobalLayoutListener {
            if(isExpanded != wasExpanded) {
                wasExpanded = isExpanded
                expandStateListener?.invoke(wasExpanded)
            }
        }
    }

    /**
     * Disable dragging for the specified app bar layout while the search view is expanded.
     */
    fun setAppBarLayout(appBarLayout: AppBarLayout) {
        var isDragCallbackAdded = false
        appBarLayout.viewTreeObserver.addOnGlobalLayoutListener {
            if(!isDragCallbackAdded && ViewCompat.isLaidOut(appBarLayout)) {
                isDragCallbackAdded = true
                val params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
                val behavior = params.behavior as AppBarLayout.Behavior
                behavior.setDragCallback(appBarDragCallback)
            }
        }
    }

    private val appBarDragCallback = object : AppBarLayout.Behavior.DragCallback() {
        override fun canDrag(appBarLayout: AppBarLayout): Boolean {
            return !isExpanded
        }
    }

}