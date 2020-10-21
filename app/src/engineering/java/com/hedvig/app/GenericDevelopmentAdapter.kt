package com.hedvig.app

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener

class GenericDevelopmentAdapter(
    private val items: List<Item>
) : RecyclerView.Adapter<GenericDevelopmentAdapter.ViewHolder>() {

    override fun getItemViewType(position: Int) = when (items[position]) {
        is Item.Header -> R.layout.development_header_row
        is Item.ClickableItem -> R.layout.development_row
        is Item.MarketSpinner -> R.layout.development_market_spinner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.development_header_row -> ViewHolder.HeaderViewHolder(parent)
        R.layout.development_row -> ViewHolder.ClickableViewHolder(parent)
        R.layout.development_market_spinner -> ViewHolder.MarketSpinner(parent)
        else -> throw Error("Invalid viewType")
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: Item)

        class HeaderViewHolder(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.development_header_row)) {
            private val root = itemView as TextView
            override fun bind(data: Item) {
                (data as? Item.Header)?.let {
                    root.text = data.title
                }
            }
        }

        class ClickableViewHolder(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.development_row)) {
            private val root = itemView as TextView

            override fun bind(data: Item) {
                (data as? Item.ClickableItem)?.let {
                    root.text = data.title
                    root.setHapticClickListener { data.open() }
                }
            }
        }

        class MarketSpinner(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.development_market_spinner)) {
            private val root = itemView as Spinner

            init {
                root.adapter = ArrayAdapter<String>(
                    root.context,
                    android.R.layout.simple_spinner_dropdown_item,
                    Market.values().map(Market::toString)
                )
            }

            override fun bind(data: Item) {
                (data as? Item.MarketSpinner)?.let {
                    root.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                data.select(Market.valueOf(parent.getItemAtPosition(position) as String))
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                }
            }
        }
    }

    sealed class Item {
        data class Header(
            val title: String
        ) : Item()

        data class ClickableItem(
            val title: String,
            val open: () -> Unit
        ) : Item()

        data class MarketSpinner(
            val select: (Market) -> Unit
        ) : Item()
    }
}

class GenericDevelopmentAdapterBuilder {
    val items = mutableListOf<GenericDevelopmentAdapter.Item>()

    fun header(label: String) {
        items.add(GenericDevelopmentAdapter.Item.Header(label))
    }

    fun clickableItem(label: String, onClick: () -> Unit) {
        items.add(GenericDevelopmentAdapter.Item.ClickableItem(label, onClick))
    }

    fun marketSpinner(select: (Market) -> Unit) {
        items.add(GenericDevelopmentAdapter.Item.MarketSpinner(select))
    }
}

inline fun genericDevelopmentAdapter(
    crossinline function: GenericDevelopmentAdapterBuilder.() -> Unit
): GenericDevelopmentAdapter {
    val builder = GenericDevelopmentAdapterBuilder()
    function(builder)
    return GenericDevelopmentAdapter(builder.items)
}
