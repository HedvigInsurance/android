package com.hedvig.app.util.extensions

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.hedvig.app.util.safeLet
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline binder: (View) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        binder(findViewById<ViewGroup>(android.R.id.content).getChildAt(0))
    }

inline fun <T : ViewBinding> RecyclerView.ViewHolder.viewBinding(crossinline binder: (View) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        binder(itemView)
    }

inline fun <T : ViewBinding> ViewGroup.viewBinding(crossinline binder: (View) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        binder(this)
    }

class FragmentViewBindingDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    private val viewBindingFactory: (View) -> T,
    private val cleanup: (T.() -> Unit)? = null
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            safeLet(binding, cleanup) { binding, cleanup -> cleanup(binding) }
                            binding = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T, cleanup: (T.() -> Unit)? = null) =
    FragmentViewBindingDelegate(this, viewBindingFactory, cleanup)
