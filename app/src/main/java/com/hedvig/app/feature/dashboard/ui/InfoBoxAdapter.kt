package com.hedvig.app.feature.dashboard.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.payment.connect.ConnectPaymentActivity
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.dashboard_info_card.view.*
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

class InfoBoxAdapter : RecyclerView.Adapter<InfoBoxAdapter.ViewHolder>() {
    var items: List<InfoBoxModel> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.dashboard_info_card, parent, false)
    ) {
        private val root = itemView.root
        private val title = itemView.title
        private val body = itemView.body
        private val action = itemView.action

        fun bind(data: InfoBoxModel) {
            when (data) {
                is InfoBoxModel.ImportantInformation -> {
                    title.text = data.title
                    body.text = data.body
                    action.text = data.actionLabel
                    val maybeLinkUri = runCatching {
                        Uri.parse(data.actionLink)
                    }
                    action.setHapticClickListener {
                        maybeLinkUri.getOrNull()?.let { uri ->
                            if (action.context.canOpenUri(uri)) {
                                action.context.openUri(uri)
                            }
                        }
                    }
                }
                is InfoBoxModel.Renewal -> {
                    title.text =
                        title.resources.getString(R.string.DASHBOARD_RENEWAL_PROMPTER_TITLE)
                    body.text = interpolateTextKey(
                        body.resources.getString(R.string.DASHBOARD_RENEWAL_PROMPTER_BODY),
                        "DAYS_UNTIL_RENEWAL" to ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            data.renewalDate
                        )
                    )
                    action.text =
                        action.resources.getString(R.string.DASHBOARD_RENEWAL_PROMPTER_CTA)
                    val maybeLinkUri = runCatching {
                        Uri.parse(data.draftCertificateUrl)
                    }
                    action.setHapticClickListener {
                        maybeLinkUri.getOrNull()?.let { uri ->
                            if (action.context.canOpenUri(uri)) {
                                action.context.openUri(uri)
                            }
                        }
                    }
                }
                is InfoBoxModel.ConnectPayin -> {
                    title.text =
                        title.resources.getString(R.string.DASHBOARD_SETUP_DIRECT_DEBIT_TITLE)
                    body.text =
                        body.resources.getString(R.string.DASHBOARD_DIRECT_DEBIT_STATUS_NEED_SETUP_DESCRIPTION)
                    action.text =
                        body.resources.getString(R.string.DASHBOARD_DIRECT_DEBIT_STATUS_NEED_SETUP_BUTTON_LABEL)
                    action.setHapticClickListener {
                        action.context.startActivity(ConnectPaymentActivity.newInstance(action.context))
                    }
                }
            }
        }
    }
}

sealed class InfoBoxModel {
    data class ImportantInformation(
        val title: String,
        val body: String,
        val actionLabel: String,
        val actionLink: String
    ) : InfoBoxModel()

    data class Renewal(
        val renewalDate: LocalDate,
        val draftCertificateUrl: String
    ) : InfoBoxModel()

    object ConnectPayin : InfoBoxModel()
}
