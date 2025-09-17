package com.example.chaintorquenative.mobile.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chaintorquenative.databinding.FragmentMarketplaceBinding // <-- Add this import
import com.example.chaintorquenative.R // Assuming your layout file will be in res/layout
import com.example.chaintorquenative.mobile.data.api.MarketplaceItem


class MarketplaceAdapter(
    private val onItemClick: (MarketplaceItem) -> Unit,
    private val onPurchaseClick: ((MarketplaceItem) -> Unit)? // Nullable if not always present
) : ListAdapter<MarketplaceItem, MarketplaceAdapter.ViewHolder>(MarketplaceItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marketplace, parent, false) // Replace with your actual layout file
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClick, onPurchaseClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TODO: Replace with actual views from your 'item_marketplace.xml'
        private val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        private val itemPrice: TextView = itemView.findViewById(R.id.textViewItemPrice)
        private val itemImage: ImageView = itemView.findViewById(R.id.imageViewItem)
        private val purchaseButton: Button = itemView.findViewById(R.id.buttonPurchase) // Example

        fun bind(
            item: MarketplaceItem,
            onItemClick: (MarketplaceItem) -> Unit,
            onPurchaseClick: ((MarketplaceItem) -> Unit)?
        ) {
            itemName.text = item.name
            itemPrice.text = "${item.price} ETH" // Format as needed
            // TODO: Load image into itemImage using a library like Glide or Coil
            // For example, with Glide:
            // Glide.with(itemView.context).load(item.imageUrl).into(itemImage)

            itemView.setOnClickListener {
                onItemClick(item)
            }

            if (onPurchaseClick != null) {
                purchaseButton.visibility = View.VISIBLE
                purchaseButton.setOnClickListener {
                    onPurchaseClick(item)
                }
            } else {
                purchaseButton.visibility = View.GONE
            }
        }
    }
}

class MarketplaceItemDiffCallback : DiffUtil.ItemCallback<MarketplaceItem>() {
    override fun areItemsTheSame(oldItem: MarketplaceItem, newItem: MarketplaceItem): Boolean {
        return oldItem.tokenId == newItem.tokenId // Assuming tokenId is a unique identifier
    }

    override fun areContentsTheSame(oldItem: MarketplaceItem, newItem: MarketplaceItem): Boolean {
        return oldItem == newItem
    }
}
