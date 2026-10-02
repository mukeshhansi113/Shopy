package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import com.example.data.model.Product
import com.example.ui.ShopyViewModel
import com.example.ui.theme.*

@Composable
fun WishlistScreen(
    viewModel: ShopyViewModel,
    onProductClick: (Product) -> Unit,
    onShopNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allApprovedProducts.collectAsState()
    val wishlistIds by viewModel.wishlistProductIds.collectAsState()

    val wishlistedProducts = remember(allProducts, wishlistIds) {
        allProducts.filter { wishlistIds.contains(it.id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShopyBackground)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("My Wishlist", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${wishlistedProducts.size} items saved", fontSize = 12.sp, color = ShopyTextSecondary)
            }
        }

        if (wishlistedProducts.isEmpty()) {
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
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = ShopyCrimson,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your Wishlist is Empty", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Save items you love and review them anytime.", fontSize = 13.sp, color = ShopyTextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onShopNow,
                        colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Explore Products")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(wishlistedProducts, key = { it.id }) { product ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(product) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.9f)
                                    .background(ShopySurfaceVariant)
                            ) {
                                val img = product.getImageList().firstOrNull() ?: ""
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(img)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                IconButton(
                                    onClick = { viewModel.toggleWishlist(product.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(32.dp)
                                        .background(Color.White, CircleShape)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = ShopyCrimson, modifier = Modifier.size(18.dp))
                                }
                            }

                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    product.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "₹${product.sellingPrice.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.addToCart(
                                            product = product,
                                            size = product.getSizeList().firstOrNull() ?: "Standard",
                                            color = product.getColorList().firstOrNull() ?: "Default"
                                        )
                                        viewModel.toggleWishlist(product.id)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                                    modifier = Modifier.fillMaxWidth().height(34.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Move to Cart", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
