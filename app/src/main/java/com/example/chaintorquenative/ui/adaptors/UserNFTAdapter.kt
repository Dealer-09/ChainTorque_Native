package com.example.chaintorquenative.mobile.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chaintorquenative.R // Assuming your layout file will be in res/layout
import com.example.chaintorquenative.mobile.data.api.UserNFT

// TODO: Create a layout file named 'item_user_nft.xml' in your res/layout directory

class UserNFTAdapter(
    private val onItemClick: (UserNFT) -> Unit
) : ListAdapter<UserNFT, UserNFTAdapter.ViewHolder>(UserNFTDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_nft, parent, false) // Replace with your actual layout file
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClick)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TODO: Replace with actual views from your 'item_user_nft.xml'
        private val nftName: TextView = itemView.findViewById(R.id.textViewNftName)
        private val nftImage: ImageView = itemView.findViewById(R.id.imageViewNft)
        // Add other views as needed, e.g., collection name, token ID

        fun bind(item: UserNFT, onItemClick: (UserNFT) -> Unit) {
            nftName.text = item.name // Assuming UserNFT has a 'name' property
            // TODO: Load image into nftImage using a library like Glide or Coil
            // Glide.with(itemView.context).load(item.imageUrl).into(nftImage)

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}

class UserNFTDiffCallback : DiffUtil.ItemCallback<UserNFT>() {
    override fun areItemsTheSame(oldItem: UserNFT, newItem: UserNFT): Boolean {
        return oldItem.tokenId == newItem.tokenId // Assuming tokenId is a unique identifier
    }

    override fun areContentsTheSame(oldItem: UserNFT, newItem: UserNFT): Boolean {
        return oldItem == newItem
    }
}
