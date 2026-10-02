package com.example.ui.seller

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiProductStudio
import com.example.ai.GeneratedProductShot
import com.example.ui.ShopyViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ShopyViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("Cotton Embroidered Festive Kurti") }
    var category by remember { mutableStateOf("Women Ethnic") }
    var originalPriceStr by remember { mutableStateOf("999") }
    var discountPercentStr by remember { mutableStateOf("60") }
    var stockStr by remember { mutableStateOf("50") }
    var sizes by remember { mutableStateOf("S, M, L, XL, XXL") }
    var colors by remember { mutableStateOf("Wine Red, Navy Blue, Forest Green") }
    var description by remember { mutableStateOf("Pure cotton high quality ethnic kurti with intricate thread embroidery and soft lining.") }

    var samplePhotoType by remember { mutableStateOf("fashion") }
    var rawPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val generatedShots by viewModel.aiGeneratedShots.collectAsState()
    val isGeneratingShots by viewModel.isGeneratingAiShots.collectAsState()
    val aiCopyDetails by viewModel.aiCopyDetails.collectAsState()
    val isGeneratingCopy by viewModel.isGeneratingAiCopy.collectAsState()

    val context = LocalContext.current

    // Initialize with a default product photo
    LaunchedEffect(Unit) {
        if (rawPhotoBitmap == null) {
            val bmp = AiProductStudio.createSampleProductBitmap("fashion")
            rawPhotoBitmap = bmp
            viewModel.processProductImageWithAi(bmp)
        }
    }

    val categories = listOf(
        "Women Ethnic",
        "Men Fashion",
        "Electronics",
        "Footwear",
        "Jewellery & Bags",
        "Home & Living"
    )
    var categoryExpanded by remember { mutableStateOf(false) }

    val originalPrice = originalPriceStr.toDoubleOrNull() ?: 0.0
    val discountPercent = discountPercentStr.toIntOrNull() ?: 0
    val computedSelling = originalPrice * (100 - discountPercent) / 100.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Product", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("add_product_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                Button(
                    onClick = {
                        val selectedUris = generatedShots.filter { it.isSelected }.map { it.localUri }
                        viewModel.createSellerProduct(
                            name = name,
                            category = category,
                            originalPrice = originalPrice,
                            discountPercent = discountPercent,
                            stock = stockStr.toIntOrNull() ?: 20,
                            sizes = sizes,
                            colors = colors,
                            description = description,
                            selectedImageUris = selectedUris,
                            onSuccess = onBack
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ShopyCrimson),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp)
                        .testTag("publish_product_button")
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish to Shopy Marketplace", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // AI Product Image Feature Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth().testTag("ai_studio_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ShopyPinkContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = ShopyCrimson, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("AI Product Photo Studio", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Auto background removal & 4 studio scenes", fontSize = 11.sp, color = ShopyTextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Raw Photo Selector (Simulated Upload / Camera)
                    Text("Step 1: Raw Product Photo", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                samplePhotoType = "fashion"
                                val bmp = AiProductStudio.createSampleProductBitmap("fashion")
                                rawPhotoBitmap = bmp
                                viewModel.processProductImageWithAi(bmp)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("👗 Fashion", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                samplePhotoType = "shoes"
                                val bmp = AiProductStudio.createSampleProductBitmap("shoes")
                                rawPhotoBitmap = bmp
                                viewModel.processProductImageWithAi(bmp)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("👟 Footwear", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                samplePhotoType = "gadget"
                                val bmp = AiProductStudio.createSampleProductBitmap("gadget")
                                rawPhotoBitmap = bmp
                                viewModel.processProductImageWithAi(bmp)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎧 Gadget", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step 2: Generated AI Studio Variations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Step 2: AI Generated Studio Shots", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        if (isGeneratingShots) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = ShopyCrimson)
                        }
                    }
                    Text(
                        "Tap on shots to toggle inclusion in your live product listing.",
                        fontSize = 11.sp,
                        color = ShopyTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (generatedShots.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(generatedShots, key = { it.id }) { shot ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (shot.isSelected) 2.dp else 1.dp,
                                        color = if (shot.isSelected) ShopyCrimson else ShopyOutline
                                    ),
                                    modifier = Modifier
                                        .width(150.dp)
                                        .clickable { viewModel.toggleShotSelection(shot.id) }
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .background(Color.White)
                                        ) {
                                            if (shot.bitmap != null) {
                                                Image(
                                                    bitmap = shot.bitmap.asImageBitmap(),
                                                    contentDescription = shot.title,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }

                                            // Checkbox overlay
                                            Checkbox(
                                                checked = shot.isSelected,
                                                onCheckedChange = { viewModel.toggleShotSelection(shot.id) },
                                                colors = CheckboxDefaults.colors(checkedColor = ShopyCrimson),
                                                modifier = Modifier.align(Alignment.TopEnd)
                                            )
                                        }

                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(shot.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                                            Text(shot.backgroundType.description, fontSize = 9.sp, color = ShopyTextSecondary, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // AI Copywriter Card (Gemini Integration)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("✨ AI Description & SEO Writer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Auto-generate compelling titles & bullet points", fontSize = 11.sp, color = ShopyTextSecondary)
                        }

                        Button(
                            onClick = { viewModel.enhanceListingWithAi(name, category, description) },
                            colors = ButtonDefaults.buttonColors(containerColor = ShopyTeal),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("ai_enhance_button")
                        ) {
                            if (isGeneratingCopy) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Generate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (aiCopyDetails != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = ShopyTealLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Generated SEO Title:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ShopyTeal)
                                Text(aiCopyDetails!!.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Key Highlights:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ShopyTeal)
                                aiCopyDetails!!.highlights.forEach { h ->
                                    Text("• $h", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            name = aiCopyDetails!!.title
                                            description = aiCopyDetails!!.description
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ShopyTeal),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Apply to Listing", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Product Details Fields
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Product Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name") },
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                    )

                    // Category Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                IconButton(onClick = { categoryExpanded = true }) {
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { categoryExpanded = true }
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Price & Discount Row
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = originalPriceStr,
                            onValueChange = { originalPriceStr = it },
                            label = { Text("Original Price (MRP)") },
                            prefix = { Text("₹") },
                            modifier = Modifier.weight(1f).testTag("product_mrp_input")
                        )
                        OutlinedTextField(
                            value = discountPercentStr,
                            onValueChange = { discountPercentStr = it },
                            label = { Text("Discount %") },
                            suffix = { Text("%") },
                            modifier = Modifier.weight(1f).testTag("product_discount_input")
                        )
                    }

                    // Computed Selling Price Info
                    Surface(
                        color = ShopyPinkContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Customer Selling Price:", fontSize = 12.sp, color = ShopyOnPinkContainer)
                            Text("₹${computedSelling.toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = ShopyCrimson)
                        }
                    }

                    // Stock & Sizes
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            label = { Text("Available Stock") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sizes,
                            onValueChange = { sizes = it },
                            label = { Text("Sizes (comma separated)") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }

                    OutlinedTextField(
                        value = colors,
                        onValueChange = { colors = it },
                        label = { Text("Colors (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Product Description") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
