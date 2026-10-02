package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.admin.AdminPortalScreen
import com.example.ui.components.ShopyBottomBar
import com.example.ui.components.ShopyHeader
import com.example.ui.customer.*
import com.example.ui.seller.AddProductScreen
import com.example.ui.seller.SellerScreen
import com.example.ui.theme.ShopyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShopyTheme {
                val viewModel: ShopyViewModel = viewModel()
                ShopyApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ShopyApp(viewModel: ShopyViewModel) {
    val currentMode by viewModel.currentMode.collectAsState()
    val customerTab by viewModel.customerTab.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var isAddingProduct by remember { mutableStateOf(false) }
    var showingWishlist by remember { mutableStateOf(false) }
    var showingNotifications by remember { mutableStateOf(false) }

    // Hardware Back Handling
    BackHandler(
        enabled = selectedProduct != null || isAddingProduct || showingWishlist || showingNotifications || currentMode != AppMode.CUSTOMER || customerTab != CustomerTab.HOME
    ) {
        when {
            selectedProduct != null -> viewModel.selectProduct(null)
            isAddingProduct -> isAddingProduct = false
            showingWishlist -> showingWishlist = false
            showingNotifications -> showingNotifications = false
            currentMode != AppMode.CUSTOMER -> viewModel.setMode(AppMode.CUSTOMER)
            customerTab != CustomerTab.HOME -> viewModel.setCustomerTab(CustomerTab.HOME)
        }
    }

    Scaffold(
        topBar = {
            if (selectedProduct == null && !isAddingProduct && !showingNotifications) {
                ShopyHeader(
                    currentMode = currentMode,
                    onModeChange = { newMode ->
                        showingWishlist = false
                        viewModel.setMode(newMode)
                    },
                    cartCount = cartItems.sumOf { it.quantity },
                    wishlistCount = wishlistItems.size,
                    notificationCount = notifications.count { !it.isRead },
                    onWishlistClick = {
                        showingWishlist = true
                        viewModel.setMode(AppMode.CUSTOMER)
                    },
                    onCartClick = {
                        showingWishlist = false
                        viewModel.setMode(AppMode.CUSTOMER)
                        viewModel.setCustomerTab(CustomerTab.CART)
                    },
                    onNotificationClick = {
                        showingNotifications = true
                    }
                )
            }
        },
        bottomBar = {
            if (selectedProduct == null && !isAddingProduct && !showingWishlist && !showingNotifications && currentMode == AppMode.CUSTOMER) {
                ShopyBottomBar(
                    selectedTab = customerTab,
                    onTabSelected = { tab ->
                        viewModel.setCustomerTab(tab)
                    },
                    cartBadgeCount = cartItems.sumOf { it.quantity }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when {
            // Notifications Overlay
            showingNotifications -> {
                NotificationsScreen(
                    viewModel = viewModel,
                    onBack = { showingNotifications = false },
                    modifier = screenModifier
                )
            }

            // Wishlist Overlay
            showingWishlist -> {
                WishlistScreen(
                    viewModel = viewModel,
                    onProductClick = { prod ->
                        showingWishlist = false
                        viewModel.selectProduct(prod)
                    },
                    onShopNow = {
                        showingWishlist = false
                        viewModel.setCustomerTab(CustomerTab.HOME)
                    },
                    modifier = screenModifier
                )
            }

            // Product Detail Screen
            selectedProduct != null -> {
                ProductDetailScreen(
                    product = selectedProduct!!,
                    viewModel = viewModel,
                    onBack = { viewModel.selectProduct(null) },
                    onBuyNow = { prod, size, color ->
                        viewModel.addToCart(prod, size, color)
                        viewModel.selectProduct(null)
                        viewModel.setCustomerTab(CustomerTab.CART)
                    },
                    modifier = screenModifier
                )
            }

            // Seller Mode
            currentMode == AppMode.SELLER -> {
                if (isAddingProduct) {
                    AddProductScreen(
                        viewModel = viewModel,
                        onBack = { isAddingProduct = false },
                        modifier = screenModifier
                    )
                } else {
                    SellerScreen(
                        viewModel = viewModel,
                        onAddNewProduct = { isAddingProduct = true },
                        modifier = screenModifier
                    )
                }
            }

            // Admin Mode
            currentMode == AppMode.ADMIN -> {
                AdminPortalScreen(
                    viewModel = viewModel,
                    modifier = screenModifier
                )
            }

            // Customer Mode
            else -> {
                when (customerTab) {
                    CustomerTab.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onProductClick = { prod -> viewModel.selectProduct(prod) },
                            modifier = screenModifier
                        )
                    }
                    CustomerTab.CATEGORIES -> {
                        CategoriesScreen(
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                    }
                    CustomerTab.CART -> {
                        CartScreen(
                            viewModel = viewModel,
                            onContinueShopping = { viewModel.setCustomerTab(CustomerTab.HOME) },
                            onTrackOrder = { order ->
                                viewModel.setCustomerTab(CustomerTab.ORDERS)
                                viewModel.viewOrderTracking(order)
                            },
                            modifier = screenModifier
                        )
                    }
                    CustomerTab.ORDERS -> {
                        OrdersScreen(
                            viewModel = viewModel,
                            onShopNow = { viewModel.setCustomerTab(CustomerTab.HOME) },
                            modifier = screenModifier
                        )
                    }
                    CustomerTab.PROFILE -> {
                        ProfileScreen(
                            viewModel = viewModel,
                            modifier = screenModifier
                        )
                    }
                }
            }
        }
    }
}
