package com.hedvig.app

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.mocks.mockModule
import com.hedvig.app.util.extensions.getAuthenticationToken
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.debug.development_footer.view.*
import kotlinx.android.synthetic.debug.development_header.view.*
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class DevelopmentScreenAdapter(
    private val items: List<DevelopmentScreenItem>
) : RecyclerView.Adapter<DevelopmentScreenAdapter.ViewHolder>() {
    override fun getItemViewType(position: Int) = when (items[position]) {
        DevelopmentScreenItem.Header -> R.layout.development_header
        is DevelopmentScreenItem.Row -> R.layout.development_row
        DevelopmentScreenItem.Footer -> R.layout.development_footer
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.development_header -> ViewHolder.Header(parent)
        R.layout.development_footer -> ViewHolder.Footer(parent)
        R.layout.development_row -> ViewHolder.Row(parent)
        else -> throw Error("Invalid viewType")
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: DevelopmentScreenItem)
        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.development_header)) {
            private val mockPersona = itemView.mockPersona
            private val checkbox = itemView.useMockData
            override fun bind(data: DevelopmentScreenItem) {
                mockPersona.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        mockPersona.context.getSharedPreferences(
                            DEVELOPMENT_PREFERENCES,
                            Context.MODE_PRIVATE
                        )
                            .edit()
                            .putInt("mockPersona", position)
                            .apply()
                    }
                }
                val persona = mockPersona.context.getSharedPreferences(
                    DEVELOPMENT_PREFERENCES,
                    Context.MODE_PRIVATE
                )
                    .getInt("mockPersona", 0)
                mockPersona.setSelection(persona)

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        unloadKoinModules(REAL_MODULES)
                        loadKoinModules(mockModule)
                        checkbox.context.getSharedPreferences(
                            DEVELOPMENT_PREFERENCES,
                            Context.MODE_PRIVATE
                        )
                            .edit()
                            .putBoolean("useMockData", true)
                            .apply()
                    } else {
                        unloadKoinModules(mockModule)
                        loadKoinModules(REAL_MODULES)
                        checkbox.context.getSharedPreferences(
                            DEVELOPMENT_PREFERENCES,
                            Context.MODE_PRIVATE
                        )
                            .edit()
                            .putBoolean("useMockData", false)
                            .apply()
                    }
                }
                checkbox.isChecked = checkbox.context.getSharedPreferences(
                    DEVELOPMENT_PREFERENCES,
                    Context.MODE_PRIVATE
                )
                    .getBoolean("useMockData", false)
            }

            companion object {
                const val DEVELOPMENT_PREFERENCES = "DevelopmentPreferences"
                private val REAL_MODULES =
                    listOf(
                        insuranceModule,
                        marketingModule,
                        offerModule,
                        profileModule,
                        paymentModule,
                        keyGearModule,
                        adyenModule
                    )
            }
        }

        class Row(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.development_row)) {
            override fun bind(data: DevelopmentScreenItem) {
                (itemView as TextView).apply {
                    (data as DevelopmentScreenItem.Row).let { data ->
                        text = data.title
                        setHapticClickListener {
                            data.open()
                        }
                    }
                }
            }
        }

        class Footer(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.development_footer)) {
            private val token = itemView.token
            private val save = itemView.saveToken
            override fun bind(data: DevelopmentScreenItem) {
                token.setText(token.context.getAuthenticationToken())
                save.setHapticClickListener {
                    save.context.setAuthenticationToken(token.text.toString())
                    save.context.makeToast("Token saved")
                }
            }
        }
    }

    sealed class DevelopmentScreenItem {
        object Header : DevelopmentScreenItem()
        data class Row(
            val title: String,
            val open: () -> Unit
        ) : DevelopmentScreenItem()

        object Footer : DevelopmentScreenItem()
    }
}
