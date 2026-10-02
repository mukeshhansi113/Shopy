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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Address
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.ui.CartUiItem
import com.example.ui.CustomerTab
import com.example.ui.ShopyViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: ShopyViewModel,
    onContinueShopping: () -> Unit,
    onTrackOrder: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    val cartUiItems by viewModel.cartUiItems.collectAsState()
    val addresses by viewModel.userAddresses.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()

    var showAddressDialog by remember { mutableStateOf(false) }
    var showNewAddressForm by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var placedOrderResult by remember { mutableStateOf<Order?>(null) }

    // Price Calculations
    val totalMrp = cartUiItems.sumOf { it.product.originalPrice * it.cartItem.quantity }
    val totalSelling = cartUiItems.sumOf { it.product.sellingPrice * it.cartItem.quantity }
    val totalDiscount = totalMrp - totalSelling

    if (placedOrderResult != null) {
        OrderSuccessDialog(
            order = placedOrderResult!!,
            onDismiss = { placedOrderResult = null },
            onTrackOrder = { order ->
                placedOrderResult = null
                onTrackOrder(order)
            }
        )
    }

    if (showAddressDialog) {
        AddressSelectionDialog(
            addresses = addresses,
            selectedAddress = selectedAddress,
            onSelect = {
                viewModel.selectAddress(it)
                showAddressDialog = false
            },
            onAddNew = {
                showAddressDialog = false
                showNewAddressForm = true
            },
            onDismiss = { showAddressDialog = false }
        )
    }

    if (showNewAddressForm) {
        NewAddressDialog(
            onSave = { name, phone, house, area, city, state, pin ->
                viewModel.addNewAddress(name, phone, house, area, city, state, pin)
                showNewAddressForm = false
            },
            onDismiss = { showNewAddressForm = false }
        )
    }

    if (showCheckoutDialog && selectedAddress != null) {
        CheckoutPaymentDialog(
            payableAmount = totalSelling,
            address = selectedAddress!!,
            onConfirmOrder = { paymentMode ->
                showCheckoutDialog = false
                // Place order for first/all cart items
                val first = cartUiItems.firstOrNull()
                if (first != null) {
                    viewModel.placeOrder(
                        product = first.product,
                        size = first.cartItem.selectedSize,
                        color = first.cartItem.selectedColor,
                        quantity = first.cartItem.quantity,
                        paymentMethod = paymentMode,
                        address = selectedAddress!!,
                        onSuccess = { order ->
                            placedOrderResult = order
                        }
                    )
                }
            },
            onDismiss = { showCheckoutDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShopyBackground)
    ) {
        if (cartUiItems.isEmpty()) {
            // Empty Cart State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(ShopyPinkContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            tint = ShopyCrimson,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Cart is Empty",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore trending fashion, electronics & kitchen essentials!",
                        fontSize = 13.sp,
                        color = ShopyTextSecondary
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onContinueShopping,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                        modifier = Modifier.testTag("explore_products_button")
                    ) {
                        Text("Explore Products", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Items List & Summary
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Delivery Address Card
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = ShopyCrimson, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Deliver to: ", fontSize = 12.sp, color = ShopyTextSecondary)
                                    Text(
                                        text = selectedAddress?.fullName ?: "Add Address",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedAddress?.let { "${it.houseNoStreet}, ${it.city} - ${it.pincode}" } ?: "No address selected. Tap to add.",
                                    fontSize = 12.sp,
                                    color = ShopyTextSecondary,
                                    maxLines = 1
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    if (addresses.isEmpty()) showNewAddressForm = true else showAddressDialog = true
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp).testTag("change_address_button")
                            ) {
                                Text(if (selectedAddress == null) "Add" else "Change", fontSize = 11.sp, color = ShopyCrimson)
                            }
                        }
                    }
                }

                // Cart Items
                items(cartUiItems, key = { it.cartItem.id }) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { viewModel.updateCartQuantity(item.cartItem, item.cartItem.quantity + 1) },
                        onDecrease = { viewModel.updateCartQuantity(item.cartItem, item.cartItem.quantity - 1) },
                        onRemove = { viewModel.removeCartItem(item.cartItem) }
                    )
                }

                // Price Summary
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Price Details (${cartUiItems.size} items)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Product Price", fontSize = 13.sp, color = ShopyTextSecondary)
                                Text("₹${totalMrp.toInt()}", fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Product Discount", fontSize = 13.sp, color = ShopyTextSecondary)
                                Text("-₹${totalDiscount.toInt()}", fontSize = 13.sp, color = ShopyGreenSuccess, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Charges", fontSize = 13.sp, color = ShopyTextSecondary)
                                Text("FREE", fontSize = 13.sp, color = ShopyTeal, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalDivider(color = ShopyOutline)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("₹${totalSelling.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ShopyCrimson)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ShopyGreenLight)
                                    .padding(vertical = 6.dp, horizontal = 10.dp)
                            ) {
                                Text(
                                    "Yay! You are saving ₹${totalDiscount.toInt()} on this order",
                                    color = ShopyGreenSuccess,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Sticky Checkout Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("₹${totalSelling.toInt()}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Free Delivery", color = ShopyTeal, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            if (selectedAddress == null) {
                                showNewAddressForm = true
                            } else {
                                showCheckoutDialog = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                        modifier = Modifier.height(48.dp).testTag("proceed_checkout_button")
                    ) {
                        Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartUiItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                val imageUrl = item.product.getImageList().firstOrNull() ?: ""
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ShopySurfaceVariant)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.name,
                        maxLines = 2,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Size: ${item.cartItem.selectedSize} | Color: ${item.cartItem.selectedColor}",
                        fontSize = 11.sp,
                        color = ShopyTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${(item.product.sellingPrice * item.cartItem.quantity).toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${(item.product.originalPrice * item.cartItem.quantity).toInt()}",
                            fontSize = 12.sp,
                            color = ShopyTextMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.product.discountPercent}% Off",
                            fontSize = 11.sp,
                            color = ShopyGreenSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ShopyOutline)
            Spacer(modifier = Modifier.height(8.dp))

            // Actions row: Quantity Stepper & Remove
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onRemove) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = ShopyTextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("REMOVE", fontSize = 12.sp, color = ShopyTextSecondary, fontWeight = FontWeight.Bold)
                }

                // Stepper: - Qty +
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ShopySurfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${item.cartItem.quantity}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AddressSelectionDialog(
    addresses: List<Address>,
    selectedAddress: Address?,
    onSelect: (Address) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Delivery Address", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                addresses.forEach { addr ->
                    val isChosen = addr.id == selectedAddress?.id
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChosen) ShopyPinkContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isChosen) ShopyCrimson else ShopyOutline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(addr) }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = isChosen, onClick = { onSelect(addr) })
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(addr.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${addr.houseNoStreet}, ${addr.areaColony}", fontSize = 11.sp, color = ShopyTextSecondary)
                                Text("${addr.city}, ${addr.state} - ${addr.pincode}", fontSize = 11.sp, color = ShopyTextSecondary)
                                Text("Ph: ${addr.phone}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson)
            ) {
                Text("+ Add New Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NewAddressDialog(
    onSave: (fullName: String, phone: String, house: String, area: String, city: String, state: String, pincode: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Jaipur") }
    var state by remember { mutableStateOf("Rajasthan") }
    var pin by remember { mutableStateOf("302001") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Delivery Address", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Mobile Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = house, onValueChange = { house = it }, label = { Text("Flat / House No. / Building") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Area / Colony / Street") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("Pincode") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name, phone, house, area, city, state, pin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson)
            ) {
                Text("Save Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CheckoutPaymentDialog(
    payableAmount: Double,
    address: Address,
    onConfirmOrder: (paymentMode: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPayment by remember { mutableStateOf("Cash on Delivery") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Payment, contentDescription = null, tint = ShopyCrimson)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Payment Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Total Payable: ₹${payableAmount.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = ShopyCrimson)

                // Cash on Delivery
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedPayment == "Cash on Delivery") ShopyPinkContainer else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPayment == "Cash on Delivery") ShopyCrimson else ShopyOutline),
                    modifier = Modifier.fillMaxWidth().clickable { selectedPayment = "Cash on Delivery" }
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedPayment == "Cash on Delivery", onClick = { selectedPayment = "Cash on Delivery" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Cash on Delivery (COD)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Pay cash/UPI to courier at your doorstep", fontSize = 11.sp, color = ShopyTextSecondary)
                        }
                    }
                }

                // UPI
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedPayment == "UPI") ShopyPinkContainer else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPayment == "UPI") ShopyCrimson else ShopyOutline),
                    modifier = Modifier.fillMaxWidth().clickable { selectedPayment = "UPI" }
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedPayment == "UPI", onClick = { selectedPayment = "UPI" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("UPI (GPay / PhonePe / Paytm)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Instant refund + Extra ₹50 discount applicable", fontSize = 11.sp, color = ShopyGreenSuccess)
                        }
                    }
                }

                // Debit/Credit Card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedPayment == "Debit/Credit Card") ShopyPinkContainer else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPayment == "Debit/Credit Card") ShopyCrimson else ShopyOutline),
                    modifier = Modifier.fillMaxWidth().clickable { selectedPayment = "Debit/Credit Card" }
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedPayment == "Debit/Credit Card", onClick = { selectedPayment = "Debit/Credit Card" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Credit / Debit Card / NetBanking", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Visa, Mastercard, RuPay, Maestro", fontSize = 11.sp, color = ShopyTextSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmOrder(selectedPayment) },
                colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                modifier = Modifier.testTag("confirm_order_button")
            ) {
                Text("Confirm & Place Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun OrderSuccessDialog(
    order: Order,
    onDismiss: () -> Unit,
    onTrackOrder: (Order) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(ShopyGreenSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Order Placed Successfully! 🎉", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ShopyGreenSuccess)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(order.productName, fontSize = 12.sp, color = ShopyTextSecondary, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Amount: ₹${order.totalPrice.toInt()} (${order.paymentMethod})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Delivery Partner: ${order.courierName}", fontSize = 11.sp, color = ShopyTeal)
            }
        },
        confirmButton = {
            Button(
                onClick = { onTrackOrder(order) },
                colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                modifier = Modifier.testTag("track_order_success_button")
            ) {
                Text("Track Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
