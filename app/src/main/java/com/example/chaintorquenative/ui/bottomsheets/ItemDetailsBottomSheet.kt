package com.example.chaintorquenative.mobile.ui.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.semantics.text
import com.example.chaintorquenative.R // Assuming your R file is here
import com.example.chaintorquenative.databinding.BottomSheetItemDetailsBinding // You will need to create this layout file
import com.example.chaintorquenative.mobile.data.api.MarketplaceItem // Ensure this import is correct
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
// If you use Glide or another image loading library
// import com.bumptech.glide.Glide

class ItemDetailsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetItemDetailsBinding? = null
    private val binding get() = _binding!!

    // This property will hold the item passed to the bottom sheet
    // It's set by the newInstance method.
    private lateinit var marketplaceItem: MarketplaceItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetItemDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate the views with the marketplaceItem details
        // Example: (Ensure your BottomSheetItemDetailsBinding has these IDs)
        binding.textViewItemName.text = marketplaceItem.name
        binding.textViewItemDescription.text = "Description: Sample description here. You'd replace this with actual item description." // Add actual description if available
        binding.textViewItemPrice.text = "Price: ${marketplaceItem.price} ETH"
        binding.textViewTokenId.text = "Token ID: ${marketplaceItem.tokenId}"

        // Example for loading an image if your MarketplaceItem has an imageUrl
        // if (marketplaceItem.imageUrl.isNotEmpty()) {
        //     Glide.with(this)
        //         .load(marketplaceItem.imageUrl)
        //         .placeholder(R.drawable.ic_placeholder_image) // Add a placeholder drawable
        //         .error(R.drawable.ic_error_image) // Add an error drawable
        //         .into(binding.imageViewItem)
        // }

        // Add any click listeners or other UI logic here
        // binding.buttonClose.setOnClickListener {
        //     dismiss()
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Important to avoid memory leaks
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param item The MarketplaceItem to display details for.
         * @return A new instance of fragment ItemDetailsBottomSheet.
         */
        @JvmStatic
        fun newInstance(item: MarketplaceItem): ItemDetailsBottomSheet {
            val fragment = ItemDetailsBottomSheet()
            // Set the item directly. For more complex data or if you need to
            // handle process death and recreation robustly, consider using arguments with Parcelable.
            // However, for simplicity and directness if the bottom sheet is always shown
            // from an active fragment context, this direct setting is often sufficient.
            fragment.marketplaceItem = item
            return fragment
        }
    }
}
