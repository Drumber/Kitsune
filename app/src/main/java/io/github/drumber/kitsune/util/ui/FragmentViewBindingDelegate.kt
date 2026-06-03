package io.github.drumber.kitsune.util.ui

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A lifecycle-aware [ViewBinding] property delegate for [Fragment]s that use the
 * `Fragment(layoutId)` constructor together with `XBinding.bind(view)`.
 *
 * The binding is created lazily from the fragment's view and automatically cleared when the
 * view lifecycle is destroyed, removing the need for the manual `_binding`/`_binding = null`
 * boilerplate and the associated "access after onDestroyView" crashes.
 *
 * Usage:
 * ```
 * class MyFragment : Fragment(R.layout.fragment_my) {
 *     private val binding by viewBinding(FragmentMyBinding::bind)
 * }
 * ```
 */
class FragmentViewBindingDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    private val bind: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            binding = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        binding?.let { return it }

        val view = thisRef.view
            ?: error("Cannot access view binding: the fragment's view is null.")
        val newBinding = bind(view)

        // Only cache the binding while the view lifecycle is alive. When the binding is accessed
        // during onDestroyView() (after ON_DESTROY has already cleared the cached value), we return
        // a transient binding instead of caching it again, which would leak the destroyed view
        // until the next view is created.
        val viewLifecycleState = thisRef.viewLifecycleOwner.lifecycle.currentState
        if (viewLifecycleState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            binding = newBinding
        }
        return newBinding
    }
}

/**
 * Creates a [FragmentViewBindingDelegate] for the given [bind] function reference
 * (e.g. `FragmentMyBinding::bind`).
 */
fun <T : ViewBinding> Fragment.viewBinding(bind: (View) -> T): FragmentViewBindingDelegate<T> =
    FragmentViewBindingDelegate(this, bind)
