package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.ui.AppMode
import com.example.ui.CustomerTab
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopyHeader(
    currentMode: AppMode,
    onModeChange: (AppMode) -> Unit,
    cartCount: Int,
    wishlistCount: Int,
    notificationCount: Int,
    onWishlistClick: () -> Unit,
    onCartClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & App Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onModeChange(AppMode.CUSTOMER) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShopyCrimson),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "shopy",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = ShopyCrimson,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = when (currentMode) {
                                AppMode.CUSTOMER -> "Marketplace"
                                AppMode.SELLER -> "Seller Studio"
                                AppMode.ADMIN -> "Admin Portal"
                            },
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Action icons: Notifications, Wishlist, Cart
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.testTag("notification_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (notificationCount > 0) {
                                    Badge(containerColor = ShopyCrimson) {
                                        Text("$notificationCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = onWishlistClick,
                        modifier = Modifier.testTag("wishlist_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (wishlistCount > 0) {
                                    Badge(containerColor = ShopyCrimson) {
                                        Text("$wishlistCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.testTag("cart_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(containerColor = ShopyCrimson) {
                                        Text("$cartCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = "Cart",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Role Switcher Chips for effortless verification of all 3 portals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentMode == AppMode.CUSTOMER,
                    onClick = { onModeChange(AppMode.CUSTOMER) },
                    label = { Text("🛍️ Shopping App", fontSize = 12.sp) },
                    modifier = Modifier.testTag("mode_customer")
                )
                FilterChip(
                    selected = currentMode == AppMode.SELLER,
                    onClick = { onModeChange(AppMode.SELLER) },
                    label = { Text("💼 Seller Studio", fontSize = 12.sp) },
                    modifier = Modifier.testTag("mode_seller")
                )
                FilterChip(
                    selected = currentMode == AppMode.ADMIN,
                    onClick = { onModeChange(AppMode.ADMIN) },
                    label = { Text("⚡ Admin", fontSize = 12.sp) },
                    modifier = Modifier.testTag("mode_admin")
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isWishlisted: Boolean,
    onProductClick: (Product) -> Unit,
    onWishlistToggle: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ShopyOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) }
            .testTag("product_card_${product.id}")
    ) {
        Column {
            // Product Image with Wishlist Button & Discount Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .background(ShopySurfaceVariant)
            ) {
                val imageUrl = product.getImageList().firstOrNull() ?: ""
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Discount Badge
                if (product.discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ShopyGreenSuccess)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${product.discountPercent}% OFF",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Wishlist Icon
                IconButton(
                    onClick = { onWishlistToggle(product) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .testTag("wishlist_toggle_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) ShopyCrimson else ShopyTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Rating Pill (Bottom Start of Image)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.92f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "★ ${product.rating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = ShopyGreenSuccess
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "(${product.reviewCount})",
                        fontSize = 10.sp,
                        color = ShopyTextSecondary
                    )
                }
            }

            // Info Section
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "₹${product.sellingPrice.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (product.originalPrice > product.sellingPrice) {
                        Text(
                            text = "₹${product.originalPrice.toInt()}",
                            fontSize = 12.sp,
                            color = ShopyTextMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Badges: Free Delivery
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ShopyTealLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Free Delivery",
                            fontSize = 10.sp,
                            color = ShopyTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShopyBottomBar(
    selectedTab: CustomerTab,
    onTabSelected: (CustomerTab) -> Unit,
    cartBadgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = selectedTab == CustomerTab.HOME,
            onClick = { onTabSelected(CustomerTab.HOME) },
            icon = { Icon(if (selectedTab == CustomerTab.HOME) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            modifier = Modifier.testTag("tab_home")
        )
        NavigationBarItem(
            selected = selectedTab == CustomerTab.CATEGORIES,
            onClick = { onTabSelected(CustomerTab.CATEGORIES) },
            icon = { Icon(if (selectedTab == CustomerTab.CATEGORIES) Icons.Filled.Category else Icons.Outlined.Category, contentDescription = "Categories") },
            label = { Text("Categories", fontSize = 11.sp) },
            modifier = Modifier.testTag("tab_categories")
        )
        NavigationBarItem(
            selected = selectedTab == CustomerTab.CART,
            onClick = { onTabSelected(CustomerTab.CART) },
            icon = {
                BadgedBox(badge = {
                    if (cartBadgeCount > 0) {
                        Badge(containerColor = ShopyCrimson) { Text("$cartBadgeCount") }
                    }
                }) {
                    Icon(if (selectedTab == CustomerTab.CART) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart, contentDescription = "Cart")
                }
            },
            label = { Text("Cart", fontSize = 11.sp) },
            modifier = Modifier.testTag("tab_cart")
        )
        NavigationBarItem(
            selected = selectedTab == CustomerTab.ORDERS,
            onClick = { onTabSelected(CustomerTab.ORDERS) },
            icon = { Icon(if (selectedTab == CustomerTab.ORDERS) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping, contentDescription = "Orders") },
            label = { Text("Orders", fontSize = 11.sp) },
            modifier = Modifier.testTag("tab_orders")
        )
        NavigationBarItem(
            selected = selectedTab == CustomerTab.PROFILE,
            onClick = { onTabSelected(CustomerTab.PROFILE) },
            icon = { Icon(if (selectedTab == CustomerTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person, contentDescription = "Profile") },
            label = { Text("Account", fontSize = 11.sp) },
            modifier = Modifier.testTag("tab_profile")
        )
    }
}

@Composable
fun OrderTimeline(status: OrderStatus, modifier: Modifier = Modifier) {
    val steps = listOf(
        OrderStatus.PLACED to "Order Placed",
        OrderStatus.ACCEPTED to "Accepted by Seller",
        OrderStatus.PACKED to "Packed",
        OrderStatus.DISPATCHED to "Dispatched / Shipped",
        OrderStatus.DELIVERED to "Delivered"
    )

    val currentStepIndex = when (status) {
        OrderStatus.PLACED -> 0
        OrderStatus.ACCEPTED -> 1
        OrderStatus.PACKED -> 2
        OrderStatus.DISPATCHED -> 3
        OrderStatus.DELIVERED -> 4
        OrderStatus.CANCELLED -> -1
    }

    if (status == OrderStatus.CANCELLED) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("This order has been cancelled.", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, (stepStatus, label) ->
            val isCompleted = index <= currentStepIndex
            val isCurrent = index == currentStepIndex

            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> ShopyGreenSuccess
                                    else -> ShopyOutline
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
                        }
                    }

                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(32.dp)
                                .background(if (index < currentStepIndex) ShopyGreenSuccess else ShopyOutline)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = label,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface else ShopyTextMuted,
                        fontSize = 14.sp
                    )
                    if (isCurrent) {
                        Text(
                            text = "Current Status",
                            fontSize = 11.sp,
                            color = ShopyGreenSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
