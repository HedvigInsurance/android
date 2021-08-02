package com.hedvig.app

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.databinding.DevelopmentFooterBinding
import com.hedvig.app.databinding.DevelopmentHeaderBinding
import com.hedvig.app.databinding.DevelopmentRowBinding
import com.hedvig.app.mocks.mockModule
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class DevelopmentScreenAdapter(private val authenticationTokenService: AuthenticationTokenService) : ListAdapter<DevelopmentScreenAdapter.DevelopmentScreenItem, DevelopmentScreenAdapter.ViewHolder>(
    GenericDiffUtilItemCallback()
) {
    override fun getItemViewType(position: Int) = when (getItem(position)) {
        DevelopmentScreenItem.Header -> R.layout.development_header
        is DevelopmentScreenItem.Row -> R.layout.development_row
        DevelopmentScreenItem.Footer -> R.layout.development_footer
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.development_header -> ViewHolder.Header(parent)
        R.layout.development_footer -> ViewHolder.Footer(parent, authenticationTokenService)
        R.layout.development_row -> ViewHolder.Row(parent)
        else -> throw Error("Invalid viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: DevelopmentScreenItem)
        class Header(parent: ViewGroup) : ViewHolder(parent.inflate(R.layout.development_header)) {
            private val binding by viewBinding(DevelopmentHeaderBinding::bind)
            override fun bind(data: DevelopmentScreenItem) {
                with(binding) {
                    mockPersona.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
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

                    useMockData.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            unloadKoinModules(REAL_MODULES)
                            loadKoinModules(mockModule)
                            useMockData.context.getSharedPreferences(
                                DEVELOPMENT_PREFERENCES,
                                Context.MODE_PRIVATE
                            )
                                .edit()
                                .putBoolean("useMockData", true)
                                .apply()
                        } else {
                            unloadKoinModules(mockModule)
                            loadKoinModules(REAL_MODULES)
                            useMockData.context.getSharedPreferences(
                                DEVELOPMENT_PREFERENCES,
                                Context.MODE_PRIVATE
                            )
                                .edit()
                                .putBoolean("useMockData", false)
                                .apply()
                        }
                    }
                    useMockData.isChecked = useMockData.context.getSharedPreferences(
                        DEVELOPMENT_PREFERENCES,
                        Context.MODE_PRIVATE
                    )
                        .getBoolean("useMockData", false)
                }
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
            private val binding by viewBinding(DevelopmentRowBinding::bind)
            override fun bind(data: DevelopmentScreenItem) {
                with(binding) {
                    (data as DevelopmentScreenItem.Row).let { data ->
                        root.text = data.title
                        root.setHapticClickListener {
                            data.open()
                        }
                    }
                }
            }
        }

        class Footer(parent: ViewGroup, private val authenticationTokenService: AuthenticationTokenService) : ViewHolder(parent.inflate(R.layout.development_footer)) {
            private val binding by viewBinding(DevelopmentFooterBinding::bind)
            override fun bind(data: DevelopmentScreenItem) {
                with(binding) {
                    token.setText(authenticationTokenService.authenticationToken)
                    saveToken.setHapticClickListener {
                        authenticationTokenService.authenticationToken = saveToken.text.toString()
                        saveToken.context.makeToast("Token saved")
                    }
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
