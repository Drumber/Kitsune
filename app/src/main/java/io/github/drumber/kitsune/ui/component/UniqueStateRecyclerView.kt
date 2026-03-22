package io.github.drumber.kitsune.ui.component

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView that uses a unique ID to save and restore its state.
 */
class UniqueStateRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.recyclerview.R.attr.recyclerViewStyle
) : RecyclerView(context, attrs, defStyleAttr) {

    var uniqueId: Int = View.NO_ID

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        if (uniqueId == View.NO_ID) {
            return super.dispatchSaveInstanceState(container)
        }

        val stateId = uniqueId xor id
        val state = onSaveInstanceState()
        if (state != null) {
            container.put(stateId, state)
        }
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable?>?) {
        if (uniqueId == View.NO_ID) {
            return super.dispatchRestoreInstanceState(container)
        }

        val stateId = uniqueId xor id
        val state = container?.get(stateId)
        if (state != null) {
            onRestoreInstanceState(state)
        }
    }
}