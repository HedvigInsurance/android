package com.hedvig.android.feature.home.claims.commonclaim.bulletpoint

import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.core.common.android.GenericDiffUtilItemCallback
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.home.claims.commonclaim.BulletPoint

class BulletPointsAdapter(
  private val imageLoader: ImageLoader,
) : ListAdapter<BulletPoint, BulletPointsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(ComposeView(parent.context))

  override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
    viewHolder.bind(getItem(position), imageLoader)
  }

  class ViewHolder(
    private val composeView: ComposeView,
  ) : RecyclerView.ViewHolder(composeView) {
    fun bind(item: BulletPoint, imageLoader: ImageLoader) {
      composeView.setContent {
        HedvigTheme {
          HedvigCard(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp).padding(top = 8.dp),
          ) {
            Row(Modifier.padding(horizontal = 8.dp, vertical = 16.dp)) {
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(item.iconUrls.themedIcon)
                  .build(),
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier.size(24.dp),
              )
              Spacer(Modifier.width(8.dp))
              Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text(item.description, style = MaterialTheme.typography.bodyMedium)
              }
            }
          }
        }
      }
    }
  }
}
