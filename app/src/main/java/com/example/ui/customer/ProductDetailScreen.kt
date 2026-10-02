package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Product
import com.example.ui.ShopyViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    viewModel: ShopyViewModel,
    onBack: () -> Unit,
    onBuyNow: (Product, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val images = remember(product) { product.getImageList() }
    var selectedImageIndex by remember { mutableIntStateOf(0) }

    val sizes = remember(product) { product.getSizeList() }
    var selectedSize by remember { mutableStateOf(sizes.firstOrNull() ?: "Standard") }

    val colors = remember(product) { product.getColorList() }
    var selectedColor by remember { mutableStateOf(colors.firstOrNull() ?: "Default") }

    var pincodeInput by remember { mutableStateOf("302001") }
    var pincodeChecked by remember { mutableStateOf(true) }

    val wishlistIds by viewModel.wishlistProductIds.collectAsState()
    val isWishlisted = wishlistIds.contains(product.id)

    var showAddedSnackbar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.category, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleWishlist(product.id) },
                        modifier = Modifier.testTag("wishlist_detail_button")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) ShopyCrimson else ShopyTextSecondary
                        )
                    }
                    IconButton(onClick = { /* Share product link */ }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.addToCart(product, selectedSize, selectedColor)
                            showAddedSnackbar = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_to_cart_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ShopyCrimson
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, ShopyCrimson)
                    ) {
                        Icon(Icons.Outlined.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Cart", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            onBuyNow(product, selectedSize, selectedColor)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("buy_now_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson)
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        },
        snackbarHost = {
            if (showAddedSnackbar) {
                Snackbar(
                    action = {
                        TextButton(onClick = { showAddedSnackbar = false }) {
                            Text("OK", color = Color.White)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Added to Cart successfully! 🛒")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ShopyBackground)
                .verticalScroll(rememberScrollState())
        ) {
            // Main Big Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(Color.White)
            ) {
                val currentImageUrl = images.getOrNull(selectedImageIndex) ?: images.firstOrNull() ?: ""
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                if (product.discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ShopyGreenSuccess)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${product.discountPercent}% OFF",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Image Indicator Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${selectedImageIndex + 1}/${images.size.coerceAtLeast(1)}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Thumbnail strip if multiple images
            if (images.size > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(images) { index, imgUrl ->
                        val isSelected = index == selectedImageIndex
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ShopyCrimson else ShopyOutline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedImageIndex = index }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imgUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Primary Details Card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = product.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pricing
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${product.sellingPrice.toInt()}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        if (product.originalPrice > product.sellingPrice) {
                            Text(
                                text = "₹${product.originalPrice.toInt()}",
                                fontSize = 16.sp,
                                color = ShopyTextMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${product.discountPercent}% Off",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShopyGreenSuccess
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rating & Reviews Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ShopyGreenSuccess)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("★ ${product.rating}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Text(
                            text = "${product.reviewCount} Ratings, 84 Reviews",
                            fontSize = 12.sp,
                            color = ShopyTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Assurances
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ShopyPinkContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = ShopyCrimson, modifier = Modifier.size(18.dp))
                        Text(
                            "Special Offer: Extra ₹50 off on UPI Payment",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ShopyOnPinkContainer
                        )
                    }
                }
            }

            // Size Selector Card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Size", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sizes.forEach { size ->
                            val isSelected = selectedSize == size
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ShopyCrimson else ShopyOutline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(if (isSelected) ShopyPinkContainer else Color.White)
                                    .clickable { selectedSize = size }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = size,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) ShopyCrimson else ShopyTextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Color Selector Card
            if (colors.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Available Colors", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            colors.forEach { col ->
                                val isSelected = selectedColor == col
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedColor = col },
                                    label = { Text(col, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ShopyPinkContainer,
                                        selectedLabelColor = ShopyCrimson
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Pincode & Delivery Section
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivery & Services", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = pincodeInput,
                            onValueChange = { pincodeInput = it },
                            label = { Text("Enter Delivery Pincode") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(56.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { pincodeChecked = true },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Check", fontWeight = FontWeight.Bold, color = ShopyCrimson)
                        }
                    }

                    if (pincodeChecked) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ShopyGreenSuccess, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Free Delivery in 2-3 Days to $pincodeInput", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Payments, contentDescription = null, tint = ShopyTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cash on Delivery Available", fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Sync, contentDescription = null, tint = ShopySecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("7-Day Easy Return / Exchange Policy", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Seller Card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sold By", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ShopyPinkContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    product.sellerName.take(1),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = ShopyCrimson
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(product.sellerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Verified Seller", tint = ShopyTeal, modifier = Modifier.size(16.dp))
                                }
                                Text("4.3 ★ | 98% Positive Feedback", fontSize = 12.sp, color = ShopyTextSecondary)
                            }
                        }
                        OutlinedButton(
                            onClick = { /* View seller profile */ },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("View Store", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Description
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Product Details", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.description,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = ShopyTextSecondary
                    )
                }
            }
        }
    }
}
