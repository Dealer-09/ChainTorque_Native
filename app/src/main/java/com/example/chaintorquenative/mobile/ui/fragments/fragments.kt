// UI Fragments for ChainTorque Mobile App
// Main screens implementing MVVM pattern with Material Design

package com.chaintorque.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.chaintorque.mobile.ui.viewmodels.MarketplaceViewModel
import com.chaintorque.mobile.ui.viewmodels.WalletViewModel
import com.chaintorque.mobile.ui.viewmodels.UserProfileViewModel
import com.chaintorque.mobile.ui.adapters.MarketplaceAdapter
import com.chaintorque.mobile.ui.adapters.UserNFTAdapter
import com.chaintorque.mobile.data.api.MarketplaceItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MarketplaceFragment : Fragment() {

    private val viewModel: MarketplaceViewModel by viewModels()
    private val walletViewModel: WalletViewModel by activityViewModels()

    private lateinit var adapter: MarketplaceAdapter
    private var _binding: FragmentMarketplaceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketplaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MarketplaceAdapter(
            onItemClick = { item ->
                // Navigate to item details
                viewModel.selectItem(item)
                showItemDetails(item)
            },
            onPurchaseClick = { item ->
                handlePurchase(item)
            }
        )

        binding.recyclerViewMarketplace.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@MarketplaceFragment.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.searchItems(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchItems(it) }
                return true
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadMarketplaceItems()
        }
    }

    private fun observeViewModel() {
        // Observe marketplace items
        viewModel.filteredItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.textViewEmptyState.isVisible = items.isEmpty()
        }

        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
            binding.progressBar.isVisible = isLoading && adapter.itemCount == 0
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                    .setAction("Retry") {
                        viewModel.loadMarketplaceItems()
                    }
                    .show()
                viewModel.clearError()
            }
        }

        // Handle purchase success
        viewModel.purchaseSuccess.observe(viewLifecycleOwner) { transactionHash ->
            transactionHash?.let {
                Snackbar.make(
                    binding.root,
                    "Purchase successful! Transaction: ${it.take(10)}...",
                    Snackbar.LENGTH_LONG
                ).show()
                viewModel.clearPurchaseSuccess()
            }
        }
    }

    private fun handlePurchase(item: MarketplaceItem) {
        val walletAddress = walletViewModel.walletAddress.value

        if (walletAddress == null) {
            showWalletConnectionDialog()
            return
        }

        // Show purchase confirmation dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Purchase")
            .setMessage("Purchase ${item.name} for ${item.price} ETH?")
            .setPositiveButton("Buy") { _, _ ->
                viewModel.purchaseItem(item.tokenId, walletAddress, item.price)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showItemDetails(item: MarketplaceItem) {
        val bottomSheetFragment = ItemDetailsBottomSheet.newInstance(item)
        bottomSheetFragment.show(parentFragmentManager, "item_details")
    }

    private fun showWalletConnectionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Wallet Required")
            .setMessage("Please connect your wallet to make purchases")
            .setPositiveButton("Connect") { _, _ ->
                // Navigate to wallet fragment
                (activity as? MainActivity)?.navigateToWallet()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private val viewModel: UserProfileViewModel by viewModels()
    private val walletViewModel: WalletViewModel by activityViewModels()

    private lateinit var nftAdapter: UserNFTAdapter
    private lateinit var purchasesAdapter: MarketplaceAdapter
    private lateinit var salesAdapter: MarketplaceAdapter

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabLayout()
        setupRecyclerViews()
        observeViewModel()
        observeWallet()
    }

    private fun setupTabLayout() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Owned"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Purchases"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Sales"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.setCurrentTab(UserProfileViewModel.ProfileTab.OWNED)
                    1 -> viewModel.setCurrentTab(UserProfileViewModel.ProfileTab.PURCHASES)
                    2 -> viewModel.setCurrentTab(UserProfileViewModel.ProfileTab.SALES)
                }
                updateRecyclerViewVisibility()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerViews() {
        // NFT Adapter
        nftAdapter = UserNFTAdapter { nft ->
            // Handle NFT click
            showNFTDetails(nft)
        }

        // Purchases Adapter
        purchasesAdapter = MarketplaceAdapter(
            onItemClick = { item -> showItemDetails(item) },
            onPurchaseClick = null // No purchase button for owned items
        )

        // Sales Adapter
        salesAdapter = MarketplaceAdapter(
            onItemClick = { item -> showItemDetails(item) },
            onPurchaseClick = null
        )

        binding.recyclerViewOwned.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = nftAdapter
        }

        binding.recyclerViewPurchases.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = purchasesAdapter
        }

        binding.recyclerViewSales.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = salesAdapter
        }
    }

    private fun observeViewModel() {
        // Observe user profile
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.textViewUsername.text = it.username ?: "Anonymous"
                binding.textViewAddress.text = "${it.address.take(6)}...${it.address.takeLast(4)}"
                // Load profile image if available
            }
        }

        // Observe user NFTs
        viewModel.userNFTs.observe(viewLifecycleOwner) { nfts ->
            nftAdapter.submitList(nfts)
            updateEmptyState(nfts.isEmpty(), "No NFTs owned")
        }

        // Observe purchases
        viewModel.userPurchases.observe(viewLifecycleOwner) { purchases ->
            purchasesAdapter.submitList(purchases)
            updateEmptyState(purchases.isEmpty(), "No purchases yet")
        }

        // Observe sales
        viewModel.userSales.observe(viewLifecycleOwner) { sales ->
            salesAdapter.submitList(sales)
            updateEmptyState(sales.isEmpty(), "No sales yet")
        }

        // Observe wallet balance
        viewModel.walletBalance.observe(viewLifecycleOwner) { balance ->
            binding.textViewBalance.text = "$balance ETH"
        }

        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observe current tab
        viewModel.currentTab.observe(viewLifecycleOwner) { tab ->
            updateRecyclerViewVisibility()
            loadDataForTab(tab)
        }
    }

    private fun observeWallet() {
        walletViewModel.walletAddress.observe(viewLifecycleOwner) { address ->
            if (address != null) {
                viewModel.loadUserData(address)
                binding.layoutProfile.isVisible = true
                binding.layoutConnectWallet.isVisible = false
            } else {
                binding.layoutProfile.isVisible = false
                binding.layoutConnectWallet.isVisible = true
            }
        }

        binding.buttonConnectWallet.setOnClickListener {
            (activity as? MainActivity)?.navigateToWallet()
        }
    }

    private fun updateRecyclerViewVisibility() {
        val currentTab = viewModel.currentTab.value ?: UserProfileViewModel.ProfileTab.OWNED

        binding.recyclerViewOwned.isVisible = currentTab == UserProfileViewModel.ProfileTab.OWNED
        binding.recyclerViewPurchases.isVisible = currentTab == UserProfileViewModel.ProfileTab.PURCHASES
        binding.recyclerViewSales.isVisible = currentTab == UserProfileViewModel.ProfileTab.SALES
    }

    private fun loadDataForTab(tab: UserProfileViewModel.ProfileTab) {
        val address = walletViewModel.walletAddress.value ?: return

        when (tab) {
            UserProfileViewModel.ProfileTab.OWNED -> viewModel.loadUserNFTs(address)
            UserProfileViewModel.ProfileTab.PURCHASES -> viewModel.loadUserPurchases(address)
            UserProfileViewModel.ProfileTab.SALES -> viewModel.loadUserSales(address)
        }
    }

    private fun updateEmptyState(isEmpty: Boolean, message: String) {
        binding.textViewEmptyState.apply {
            isVisible = isEmpty
            text = message
        }
    }

    private fun showNFTDetails(nft: UserNFT) {
        // Show NFT details bottom sheet
    }

    private fun showItemDetails(item: MarketplaceItem) {
        // Show item details bottom sheet
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@AndroidEntryPoint
class WalletFragment : Fragment() {

    private val viewModel: WalletViewModel by activityViewModels()

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.buttonConnect.setOnClickListener {
            val address = binding.editTextAddress.text.toString().trim()
            if (address.isNotEmpty()) {
                viewModel.connectWallet(address)
            } else {
                binding.editTextAddress.error = "Please enter wallet address"
            }
        }

        binding.buttonDisconnect.setOnClickListener {
            viewModel.disconnectWallet()
        }

        binding.buttonRefreshBalance.setOnClickListener {
            viewModel.loadBalance()
        }

        binding.buttonScanQr.setOnClickListener {
            // Implement QR code scanner
            startQRScanner()
        }
    }

    private fun observeViewModel() {
        // Observe connection status
        viewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            binding.layoutConnect.isVisible = !isConnected
            binding.layoutConnected.isVisible = isConnected
        }

        // Observe wallet address
        viewModel.walletAddress.observe(viewLifecycleOwner) { address ->
            address?.let {
                binding.textViewAddress.text = "${it.take(6)}...${it.takeLast(4)}"
                binding.textViewFullAddress.text = it
            }
        }

        // Observe balance
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.textViewBalance.text = "$balance ETH"
        }

        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.buttonConnect.isEnabled = !isLoading
        }

        // Observe connection status
        viewModel.connectionStatus.observe(viewLifecycleOwner) { status ->
            val statusText = when (status) {
                WalletViewModel.ConnectionStatus.DISCONNECTED -> "Disconnected"
                WalletViewModel.ConnectionStatus.CONNECTING -> "Connecting..."
                WalletViewModel.ConnectionStatus.CONNECTED -> "Connected"
                WalletViewModel.ConnectionStatus.ERROR -> "Connection Error"
                null -> "Unknown"
            }
            binding.textViewStatus.text = statusText
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun startQRScanner() {
        // Implement QR code scanner for wallet address
        Toast.makeText(context, "QR Scanner not implemented yet", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/*
Additional Helper Classes:

// Bottom Sheet for Item Details
class ItemDetailsBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(item: MarketplaceItem): ItemDetailsBottomSheet {
            val fragment = ItemDetailsBottomSheet()
            val args = Bundle()
            args.putParcelable("item", item)
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: BottomSheetItemDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetItemDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = arguments?.getParcelable<MarketplaceItem>("item")
        item?.let { setupItemDetails(it) }
    }

    private fun setupItemDetails(item: MarketplaceItem) {
        binding.textViewName.text = item.name
        binding.textViewDescription.text = item.description
        binding.textViewPrice.text = "${item.price} ETH"
        binding.textViewCreator.text = "By ${item.creator}"

        // Load item image
        Glide.with(this)
            .load(item.imageUrl)
            .placeholder(R.drawable.placeholder_nft)
            .into(binding.imageViewItem)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// MainActivity navigation methods
fun MainActivity.navigateToWallet() {
    // Navigate to wallet fragment
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, WalletFragment())
        .addToBackStack(null)
        .commit()
}
*/
