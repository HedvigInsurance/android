package com.hedvig.app.feature.referrals

import android.os.Bundle
import android.os.PersistableBundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.extensions.inflate
import kotlinx.android.synthetic.debug.activity_mock_referrals.*

class ReferralsMockActivity : AppCompatActivity(R.layout.activity_mock_referrals) {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        root.adapter = Adapter(
            listOf(
                
            )
        )
    }

    data class ReferralsMockActivityItem(
        val title: String,
        val open: () -> Unit
    )

    class Adapter(
        private val items: List<ReferralsMockActivityItem>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
        override fun getItemCount() = items.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        class ViewHolder(parent: ViewGroup) :
            RecyclerView.ViewHolder(parent.inflate(R.layout.development_row)) {
            fun bind(data: ReferralsMockActivityItem) = Unit
        }
    }
}
