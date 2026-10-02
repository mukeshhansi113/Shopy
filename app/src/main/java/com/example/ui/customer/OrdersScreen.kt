package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.ui.ShopyViewModel
import com.example.ui.components.OrderTimeline
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: ShopyViewModel,
    onShopNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.customerOrders.collectAsState()
    val trackingOrder by viewModel.trackingOrder.collectAsState()

    if (trackingOrder != null) {
        OrderTrackingModal(
            order = trackingOrder!!,
            onDismiss = { viewModel.viewOrderTracking(null) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShopyBackground)
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Orders",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${orders.size} orders placed",
                    fontSize = 12.sp,
                    color = ShopyTextSecondary
                )
            }
        }

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(ShopyPinkContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.LocalShipping,
                            contentDescription = null,
                            tint = ShopyCrimson,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Orders Yet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "You haven't placed any orders yet. Start shopping now!",
                        fontSize = 13.sp,
                        color = ShopyTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onShopNow,
                        colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Start Shopping")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    CustomerOrderCard(
                        order = order,
                        onTrackClick = { viewModel.viewOrderTracking(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerOrderCard(
    order: Order,
    onTrackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(order.placedAt) {
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        sdf.format(Date(order.placedAt))
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTrackClick() }
            .testTag("order_card_${order.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Status badge & Order Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (order.status) {
                                OrderStatus.DELIVERED -> ShopyGreenLight
                                OrderStatus.DISPATCHED -> ShopyTealLight
                                OrderStatus.PACKED, OrderStatus.ACCEPTED -> ShopySecondaryContainer
                                OrderStatus.PLACED -> ShopyPinkContainer
                                OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when (order.status) {
                            OrderStatus.PLACED -> "Order Placed"
                            OrderStatus.ACCEPTED -> "Accepted by Seller"
                            OrderStatus.PACKED -> "Packed & Ready"
                            OrderStatus.DISPATCHED -> "Dispatched 🚚"
                            OrderStatus.DELIVERED -> "Delivered ✅"
                            OrderStatus.CANCELLED -> "Cancelled"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.status) {
                            OrderStatus.DELIVERED -> ShopyGreenSuccess
                            OrderStatus.DISPATCHED -> ShopyTeal
                            OrderStatus.PACKED, OrderStatus.ACCEPTED -> ShopySecondary
                            OrderStatus.PLACED -> ShopyCrimson
                            OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                        }
                    )
                }

                Text(
                    text = order.orderNumber,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopyTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle: Image & Product Details
            Row(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(order.productImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = order.productName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ShopySurfaceVariant)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.productName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Size: ${order.selectedSize} | Qty: ${order.quantity}",
                        fontSize = 11.sp,
                        color = ShopyTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total: ₹${order.totalPrice.toInt()} (${order.paymentMethod})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ShopyOutline)
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ordered on $dateStr",
                    fontSize = 11.sp,
                    color = ShopyTextMuted
                )

                Button(
                    onClick = onTrackClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("track_button_${order.id}")
                ) {
                    Icon(Icons.Filled.DirectionsTransit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Track Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OrderTrackingModal(
    order: Order,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Order Tracking", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tracking Bar Info
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ShopySurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tracking ID:", fontSize = 11.sp, color = ShopyTextSecondary)
                            Text(order.trackingId.ifBlank { "Assigned Soon" }, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Courier Partner:", fontSize = 11.sp, color = ShopyTextSecondary)
                            Text(order.courierName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ShopyTeal)
                        }
                    }
                }

                // Visual Timeline
                OrderTimeline(status = order.status)

                HorizontalDivider(color = ShopyOutline)

                // Shipping address
                Column {
                    Text("Shipping To:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(order.deliveryAddress, fontSize = 11.sp, color = ShopyTextSecondary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson)
            ) {
                Text("Done")
            }
        }
    )
}
