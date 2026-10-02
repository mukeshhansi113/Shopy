package com.example.ui.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.ui.AppMode
import com.example.ui.ShopyViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerScreen(
    viewModel: ShopyViewModel,
    onAddNewProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val sellerOrders by viewModel.sellerOrders.collectAsState()
    val sellerProducts by viewModel.sellerProducts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Orders, 1: Products

    val totalEarnings = remember(sellerOrders) {
        sellerOrders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalPrice }
    }
    val pendingCount = remember(sellerOrders) {
        sellerOrders.count { it.status == OrderStatus.PLACED || it.status == OrderStatus.ACCEPTED || it.status == OrderStatus.PACKED }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNewProduct,
                icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                text = { Text("Add Product (AI Studio)") },
                containerColor = ShopyCrimson,
                contentColor = Color.White,
                modifier = Modifier.testTag("seller_add_product_fab")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ShopyBackground)
        ) {
            // Seller Header & Metrics
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    currentUser.businessName.ifBlank { "Seller Studio" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (currentUser.isVerified) {
                                    Icon(Icons.Filled.Verified, contentDescription = "Verified Seller", tint = ShopyTeal, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(
                                "GST: ${currentUser.gstNumber.ifBlank { "07AAAAA0000A1Z5" }}",
                                fontSize = 11.sp,
                                color = ShopyTextSecondary
                            )
                        }

                        Button(
                            onClick = onAddNewProduct,
                            colors = ButtonDefaults.buttonColors(containerColor = ShopyPinkContainer),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = ShopyCrimson, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Listing", color = ShopyCrimson, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = "Total Sales",
                            value = "₹${totalEarnings.toInt()}",
                            icon = Icons.Filled.CurrencyRupee,
                            color = ShopyGreenSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Active Orders",
                            value = "${sellerOrders.size}",
                            icon = Icons.Filled.ShoppingBag,
                            color = ShopyCrimson,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Pending Action",
                            value = "$pendingCount",
                            icon = Icons.Filled.PendingActions,
                            color = ShopySecondary,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Products",
                            value = "${sellerProducts.size}",
                            icon = Icons.Filled.Inventory2,
                            color = ShopyTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Tab bar: Orders vs Products
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ShopyCrimson
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Customer Orders (${sellerOrders.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("My Products (${sellerProducts.size})", fontWeight = FontWeight.Bold) }
                )
            }

            // Content
            if (selectedTab == 0) {
                // Orders tab
                if (sellerOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No orders placed yet for your products.", color = ShopyTextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sellerOrders, key = { it.id }) { order ->
                            SellerOrderCard(
                                order = order,
                                onAccept = { viewModel.updateSellerOrderStatus(order.id, OrderStatus.ACCEPTED) },
                                onPack = { viewModel.updateSellerOrderStatus(order.id, OrderStatus.PACKED) },
                                onDispatch = { viewModel.updateSellerOrderStatus(order.id, OrderStatus.DISPATCHED) }
                            )
                        }
                    }
                }
            } else {
                // Products tab
                if (sellerProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No products listed yet.", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Use the AI Studio to upload and enhance your first product!", fontSize = 12.sp, color = ShopyTextSecondary)
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onAddNewProduct,
                                colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson)
                            ) {
                                Text("Create Product Listing")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sellerProducts, key = { it.id }) { prod ->
                            SellerProductItemCard(product = prod)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 14.sp, color = color)
            Text(title, fontSize = 9.sp, color = ShopyTextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun SellerOrderCard(
    order: Order,
    onAccept: () -> Unit,
    onPack: () -> Unit,
    onDispatch: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().testTag("seller_order_${order.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (order.status) {
                                OrderStatus.PLACED -> ShopyPinkContainer
                                OrderStatus.ACCEPTED, OrderStatus.PACKED -> ShopySecondaryContainer
                                OrderStatus.DISPATCHED -> ShopyTealLight
                                OrderStatus.DELIVERED -> ShopyGreenLight
                                OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.status.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            OrderStatus.PLACED -> ShopyCrimson
                            OrderStatus.ACCEPTED, OrderStatus.PACKED -> ShopySecondary
                            OrderStatus.DISPATCHED -> ShopyTeal
                            OrderStatus.DELIVERED -> ShopyGreenSuccess
                            OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(order.productImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(54.dp).clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(order.productName, fontWeight = FontWeight.Medium, fontSize = 12.sp, maxLines = 1)
                    Text("Qty: ${order.quantity} | Size: ${order.selectedSize} | ₹${order.totalPrice.toInt()}", fontSize = 11.sp, color = ShopyTextSecondary)
                    Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 10.sp, color = ShopyTextMuted)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ShopyOutline)
            Spacer(modifier = Modifier.height(8.dp))

            // Action Workflows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (order.status) {
                    OrderStatus.PLACED -> {
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp).testTag("accept_order_${order.id}")
                        ) {
                            Text("Accept Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.ACCEPTED -> {
                        Button(
                            onClick = onPack,
                            colors = ButtonDefaults.buttonColors(containerColor = ShopySecondary),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp).testTag("pack_order_${order.id}")
                        ) {
                            Text("Mark as Packed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.PACKED -> {
                        Button(
                            onClick = onDispatch,
                            colors = ButtonDefaults.buttonColors(containerColor = ShopyTeal),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(32.dp).testTag("dispatch_order_${order.id}")
                        ) {
                            Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dispatch with Tracking", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    OrderStatus.DISPATCHED -> {
                        Text("In Transit (${order.trackingId})", fontSize = 11.sp, color = ShopyTeal, fontWeight = FontWeight.SemiBold)
                    }
                    OrderStatus.DELIVERED -> {
                        Text("Delivered Successfully", fontSize = 11.sp, color = ShopyGreenSuccess, fontWeight = FontWeight.SemiBold)
                    }
                    OrderStatus.CANCELLED -> {
                        Text("Order Cancelled", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun SellerProductItemCard(product: Product) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val img = product.getImageList().firstOrNull() ?: ""
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img)
                    .crossfade(true)
                    .build(),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Selling: ₹${product.sellingPrice.toInt()} (MRP ₹${product.originalPrice.toInt()})", fontSize = 11.sp, color = ShopyCrimson, fontWeight = FontWeight.Bold)
                Text("Stock: ${product.stock} units | Category: ${product.category}", fontSize = 10.sp, color = ShopyTextSecondary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (product.isApproved) ShopyGreenLight else ShopyPinkContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (product.isApproved) "Active" else "Pending",
                    fontSize = 11.sp,
                    color = if (product.isApproved) ShopyGreenSuccess else ShopyCrimson,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
