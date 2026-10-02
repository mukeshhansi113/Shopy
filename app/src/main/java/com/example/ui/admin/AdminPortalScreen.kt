package com.example.ui.admin

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
import com.example.data.model.Product
import com.example.data.model.UserAccount
import com.example.ui.ShopyViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(
    viewModel: ShopyViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.adminAllProducts.collectAsState()
    val allSellers by viewModel.adminAllSellers.collectAsState()
    val allOrders by viewModel.adminAllOrders.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Products, 1: Sellers, 2: Orders, 3: Analytics

    val totalGmv = remember(allOrders) {
        allOrders.sumOf { it.totalPrice }
    }
    val platformCommission = remember(totalGmv) {
        totalGmv * 0.05 // 5% marketplace commission
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShopyBackground)
    ) {
        // Admin Header
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
                            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = ShopyTeal)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shopy Admin Portal", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Text("Global marketplace control & monitoring", fontSize = 11.sp, color = ShopyTextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ShopyTealLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Master Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ShopyTeal)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // KPI Overview Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminMetricCard(
                        title = "Platform GMV",
                        value = "₹${totalGmv.toInt()}",
                        color = ShopyCrimson,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Commission (5%)",
                        value = "₹${platformCommission.toInt()}",
                        color = ShopyGreenSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Sellers",
                        value = "${allSellers.size}",
                        color = ShopySecondary,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "Products",
                        value = "${allProducts.size}",
                        color = ShopyTeal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = ShopyCrimson,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Products Moderation (${allProducts.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Sellers Verification (${allSellers.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("All Orders (${allOrders.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Reports & Analytics", fontWeight = FontWeight.Bold) }
            )
        }

        // Tab Body
        when (selectedTab) {
            0 -> {
                // Products Moderation List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allProducts, key = { it.id }) { product ->
                        AdminProductCard(
                            product = product,
                            onToggleApproval = { viewModel.setProductApproval(product.id, !product.isApproved) }
                        )
                    }
                }
            }
            1 -> {
                // Sellers List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allSellers, key = { it.id }) { seller ->
                        AdminSellerCard(
                            seller = seller,
                            onToggleVerify = { viewModel.setSellerVerification(seller.id, !seller.isVerified) }
                        )
                    }
                }
            }
            2 -> {
                // All Orders List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allOrders, key = { it.id }) { order ->
                        AdminOrderCard(order = order)
                    }
                }
            }
            3 -> {
                // Reports & Analytics
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Marketplace Revenue Share", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Gross Sales:", fontSize = 13.sp, color = ShopyTextSecondary)
                                Text("₹${totalGmv.toInt()}", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Shopy Commission (5%):", fontSize = 13.sp, color = ShopyTextSecondary)
                                Text("₹${platformCommission.toInt()}", fontWeight = FontWeight.Bold, color = ShopyGreenSuccess)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Seller Payouts (95%):", fontSize = 13.sp, color = ShopyTextSecondary)
                                Text("₹${(totalGmv * 0.95).toInt()}", fontWeight = FontWeight.Bold, color = ShopyCrimson)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Top Performing Categories", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            CategoryStatRow(name = "Women Ethnic", share = "42%", count = "5 Products")
                            CategoryStatRow(name = "Electronics", share = "28%", count = "2 Products")
                            CategoryStatRow(name = "Men Fashion", share = "18%", count = "2 Products")
                            CategoryStatRow(name = "Home & Living", share = "12%", count = "2 Products")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryStatRow(name: String, share: String, count: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(count, fontSize = 10.sp, color = ShopyTextMuted)
        }
        Text(share, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ShopyCrimson)
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 13.sp, color = color)
            Text(title, fontSize = 9.sp, color = ShopyTextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun AdminProductCard(
    product: Product,
    onToggleApproval: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().testTag("admin_product_${product.id}")
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
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                Text("Seller: ${product.sellerName} | ₹${product.sellingPrice.toInt()}", fontSize = 11.sp, color = ShopyTextSecondary)
                Text("Category: ${product.category} | Stock: ${product.stock}", fontSize = 10.sp, color = ShopyTextMuted)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = onToggleApproval,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (product.isApproved) ShopyGreenLight else ShopyPinkContainer
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(30.dp).testTag("toggle_approval_${product.id}")
            ) {
                Text(
                    text = if (product.isApproved) "Approved" else "Pending",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (product.isApproved) ShopyGreenSuccess else ShopyCrimson
                )
            }
        }
    }
}

@Composable
fun AdminSellerCard(
    seller: UserAccount,
    onToggleVerify: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().testTag("admin_seller_${seller.id}")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ShopyPinkContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(seller.name.take(1), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ShopyCrimson)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(seller.businessName.ifBlank { seller.name }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (seller.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = ShopyTeal, modifier = Modifier.size(16.dp))
                    }
                }
                Text("Contact: ${seller.name} (+91 ${seller.phone})", fontSize = 11.sp, color = ShopyTextSecondary)
                Text("GST: ${seller.gstNumber.ifBlank { "Not provided" }}", fontSize = 10.sp, color = ShopyTextMuted)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = onToggleVerify,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (seller.isVerified) ShopyTealLight else ShopyPinkContainer
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(30.dp).testTag("toggle_verify_${seller.id}")
            ) {
                Text(
                    text = if (seller.isVerified) "Verified" else "Verify",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (seller.isVerified) ShopyTeal else ShopyCrimson
                )
            }
        }
    }
}

@Composable
fun AdminOrderCard(order: Order) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("₹${order.totalPrice.toInt()} (${order.paymentMethod})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ShopyCrimson)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Product: ${order.productName}", fontSize = 12.sp, maxLines = 1)
            Text("Customer: ${order.customerName} | Status: ${order.status}", fontSize = 11.sp, color = ShopyTextSecondary)
        }
    }
}
