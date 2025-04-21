
package com.codelab.basics

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.codelab.basics.ui.theme.Blue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import java.util.concurrent.Executors


class MainActivity : ComponentActivity() {

    private fun getProductList(): List<Map<String, Any>> {
        val db = SQLServerDBHelper()
        return db.getAllProducts()
    }

    @Composable
    fun InventoryApp() {
        var selectedProduct by remember { mutableStateOf<Map<String, Any>?>(null) }
        var showAddProductScreen by remember { mutableStateOf(false) }
        var showSearchProductScreen by remember { mutableStateOf(false) }
        var showScanOutProductScreen by remember { mutableStateOf(false) }

        when {
            selectedProduct != null -> {
                ProductDetailsScreen(selectedProduct!!) {
                    selectedProduct = null
                }
            }
            showAddProductScreen -> {
                AddProductScreen {
                    showAddProductScreen = false
                }
            }
            showSearchProductScreen -> {
                SearchProductScreen {
                    showSearchProductScreen = false
                }
            }
            showScanOutProductScreen -> {
                ScanOutProductScreen {
                    showScanOutProductScreen = false
                }
            }
            else -> {
                ProductListScreen(
                    onProductClick = { selectedProduct = it },
                    onAddProductClick = { showAddProductScreen = true },
                    onSearchProductClick = { showSearchProductScreen = true },
                    onScanOutProductClick = { showScanOutProductScreen = true }
                )
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun ProductListScreen(onProductClick: (Map<String, Any>) -> Unit, onAddProductClick: () -> Unit, onSearchProductClick: () -> Unit, onScanOutProductClick: () -> Unit) {
        val productList = getProductList()
        val context = LocalContext.current
        val db = SQLServerDBHelper()
        Scaffold(
            floatingActionButton = {
                Column {
                    FloatingActionButton(onClick = onAddProductClick) {
                        Text("+")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FloatingActionButton(onClick = onSearchProductClick) {
                        Text("🔍")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FloatingActionButton(onClick = onScanOutProductClick) {
                        Text("📦")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    FloatingActionButton(onClick = {
                        db.testConnection { isConnected ->
                            val message = if (isConnected) "✅ Connected to SQL Server" else "❌ Could not connect to SQL Server"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("🧪")
                    }
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(text = "Inventory", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn {
                    items(productList) { product ->
                        val name = product["ProductName"] as? String ?: "Unnamed"
                        val stock = product["Quantity"]?.toString() ?: "0"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductClick(product) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Stock: $stock",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
    @Composable
    fun ProductDetailsScreen(product: Map<String, Any>, onBack: () -> Unit) {
        val context = LocalContext.current
        val db = SQLServerDBHelper()

        val name = product["ProductName"] as? String ?: "Unnamed"
        val sku = product["SKU"] as? String ?: "Unknown"
        val category = product["Category"] as? String ?: "Misc"
        val quantity = (product["Quantity"] as? Int) ?: 0
        val price = (product["Price"] as? Double) ?: 0.0
        val supplier = product["Supplier"] as? String ?: "Unlisted"

        var quantityChange by remember { mutableStateOf(TextFieldValue("1")) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Product Details", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Name: $name", style = MaterialTheme.typography.bodyLarge)
                Text(text = "SKU: $sku", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Category: $category", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Quantity: $quantity", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Price: $${"%.2f".format(price)}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Supplier: $supplier", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = quantityChange,
                    onValueChange = { quantityChange = it },
                    label = { Text("Amount to Adjust") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column {
                Button(
                    onClick = {
                        val change = quantityChange.text.toIntOrNull() ?: 1
                        val newQuantity = quantity + change
                        db.updateProductStock(sku, newQuantity)
                        Toast.makeText(context, "Quantity increased by $change", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Increase Stock")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val change = quantityChange.text.toIntOrNull() ?: 1
                        val newQuantity = quantity - change
                        db.updateProductStock(sku, newQuantity)
                        Toast.makeText(context, "Quantity reduced by $change", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Reduce Stock")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        db.deleteProduct(sku)
                        Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Delete Product")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Back")
                }
            }
        }
    }

    @Composable
    fun AddProductScreen(onProductAdded: () -> Unit) {
        var productName by remember { mutableStateOf("") }
        var sku by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var supplier by remember { mutableStateOf("") }
        val context = LocalContext.current
        val db = SQLServerDBHelper()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Add New Product", style = MaterialTheme.typography.headlineLarge)

                OutlinedTextField(value = productName, onValueChange = { productName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = quantity, onValueChange = { quantity = it.filter { char -> char.isDigit() } }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it.filter { char -> char.isDigit() || char == '.' } }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier") }, modifier = Modifier.fillMaxWidth())
            }

            Column {
                Button(
                    onClick = {
                        if (productName.isNotEmpty() && sku.isNotEmpty() && category.isNotEmpty() &&
                            quantity.isNotEmpty() && price.isNotEmpty() && supplier.isNotEmpty()
                        ) {
                            db.addProduct(productName, sku, category, quantity.toInt(), price.toDouble(), supplier)
                            Toast.makeText(context, "Product added!", Toast.LENGTH_SHORT).show()
                            onProductAdded()
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Save Product")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onProductAdded, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Cancel")
                }
            }
        }
    }

    @Composable
    fun SearchProductScreen(onBack: () -> Unit) {
        var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
        var searchResult by remember { mutableStateOf<Map<String, Any>?>(null) }
        val context = LocalContext.current
        val db = SQLServerDBHelper()

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Search Product", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Enter Product Name or SKU") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val executor = Executors.newSingleThreadExecutor()
                    executor.execute {
                        val query = "SELECT * FROM Products WHERE ProductName LIKE ? OR SKU = ?"
                        try {
                            val connection = db.getConnection()
                            val preparedStatement = connection?.prepareStatement(query)
                            preparedStatement?.setString(1, "%" + searchQuery.text + "%")
                            preparedStatement?.setString(2, searchQuery.text)
                            val resultSet = preparedStatement?.executeQuery()

                            if (resultSet?.next() == true) {
                                searchResult = mapOf(
                                    "ProductName" to resultSet.getString("ProductName"),
                                    "SKU" to resultSet.getString("SKU"),
                                    "Quantity" to resultSet.getInt("Quantity"),
                                    "UPC" to resultSet.getString("SKU")
                                )
                            } else {
                                searchResult = null
                            }
                            connection?.close()
                        } catch (e: Exception) {
                            Log.e("SearchProductScreen", "SQL Error: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Search")
            }

            Spacer(modifier = Modifier.height(16.dp))

            searchResult?.let { product ->
                Text(text = "Product: ${product["ProductName"]}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "SKU: ${product["SKU"]}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "UPC Code: ${product["UPC"]}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Stock: ${product["Quantity"]}", style = MaterialTheme.typography.bodyLarge)
            } ?: Text(text = "No product found", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Back")
            }
        }
    }

    @Composable
    fun ScanOutProductScreen(onBack: () -> Unit) {
        var scannedProducts by remember { mutableStateOf(listOf<Map<String, Any>>()) }
        var scanQuery by remember { mutableStateOf(TextFieldValue("")) }
        val context = LocalContext.current
        val db = SQLServerDBHelper()

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(text = "Scan Out Products", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = scanQuery,
                onValueChange = { scanQuery = it },
                label = { Text("Enter Product SKU or Scan Barcode") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val executor = Executors.newSingleThreadExecutor()
                    executor.execute {
                        val query = "SELECT * FROM Products WHERE SKU = ?"
                        try {
                            val connection = db.getConnection()
                            val preparedStatement = connection?.prepareStatement(query)
                            preparedStatement?.setString(1, scanQuery.text)
                            val resultSet = preparedStatement?.executeQuery()

                            if (resultSet?.next() == true) {
                                val product = mapOf(
                                    "ProductName" to resultSet.getString("ProductName"),
                                    "SKU" to resultSet.getString("SKU"),
                                    "Quantity" to resultSet.getInt("Quantity"),
                                    "UPC" to resultSet.getString("SKU")
                                )
                                Handler(Looper.getMainLooper()).post {
                                    scannedProducts = scannedProducts.toMutableList().apply { add(product) }
                                }
                            } else {
                                Log.e("ScanOut", "No product found with SKU: ${scanQuery.text}")
                            }
                            connection?.close()
                        } catch (e: Exception) {
                            Log.e("ScanOut", "Error: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Scan Product")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Scanned Products:", style = MaterialTheme.typography.bodyLarge)
            scannedProducts.forEach { product ->
                val name = product["ProductName"] as? String ?: "Unknown"
                val sku = product["SKU"] as? String ?: "-"
                Text(text = "$name - $sku", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val executor = Executors.newSingleThreadExecutor()
                    executor.execute {
                        try {
                            val connection = db.getConnection()
                            scannedProducts.forEach { product ->
                                val updateQuery = "UPDATE Products SET Quantity = Quantity - 1 WHERE SKU = ?"
                                val preparedStatement = connection?.prepareStatement(updateQuery)
                                preparedStatement?.setString(1, product["SKU"] as String)
                                preparedStatement?.executeUpdate()
                            }
                            connection?.close()
                            Handler(Looper.getMainLooper()).post {
                                scannedProducts = listOf()
                            }
                        } catch (e: Exception) {
                            Log.e("ScanOutProductScreen", "Update Error: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Confirm Scan Out")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Back")
            }
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                InventoryApp()
            }
        }
    }
}