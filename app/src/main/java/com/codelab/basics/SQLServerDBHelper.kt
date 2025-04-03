package com.codelab.basics

import android.util.Log
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.concurrent.Executors

class SQLServerDBHelper {
    // SQL Server connection details
    private val ip = "localhost"  // Update with my IP
    private val port = "1433"  // SQL Server port
    private val dbName = "GroceryInventoryDB"
    private val username = "BDFulton"
    private val password = "Everest69$"

    private val connectionString = "jdbc:jtds:sqlserver://$ip:$port/$dbName;user=$username;password=$password;"

    // Function to establish a connection
    fun getConnection(): Connection? {
        return try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            DriverManager.getConnection(connectionString)
        } catch (e: Exception) {
            Log.e("SQLServerDBHelper", "Error Connecting to Database: ${e.message}")
            null
        }
    }

    // Function to retrieve all products
    fun getAllProducts(): List<Map<String, Any>> {
        val productList = mutableListOf<Map<String, Any>>()
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val query = "SELECT * FROM Products"
            try {
                val connection = getConnection()
                val statement = connection?.createStatement()
                val resultSet = statement?.executeQuery(query)

                while (resultSet?.next() == true) {
                    productList.add(
                        mapOf(
                            "ProductID" to resultSet.getInt("ProductID"),
                            "ProductName" to resultSet.getString("ProductName"),
                            "SKU" to resultSet.getString("SKU"),
                            "Category" to resultSet.getString("Category"),
                            "Quantity" to resultSet.getInt("Quantity"),
                            "Price" to resultSet.getDouble("Price"),
                            "Supplier" to resultSet.getString("Supplier")
                        )
                    )
                }
                connection?.close()
            } catch (e: SQLException) {
                Log.e("SQLServerDBHelper", "SQL Error: ${e.message}")
            }
        }
        return productList
    }

// Function to update inventory
    fun updateProductStock(sku: String, newQuantity: Int) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val query = "UPDATE Products SET Quantity = ? WHERE SKU = ?"
            try {
                val connection = getConnection()
                val statement = connection?.prepareStatement(query)
                statement?.apply {
                    setInt(1, newQuantity)
                    setString(2, sku)
                    executeUpdate()
                }
                connection?.close()
            } catch (e: SQLException) {
                Log.e("SQLServerDBHelper", "Update Stock Error: ${e.message}")
            }
        }
    }

    //Function to delete a product
    fun deleteProduct(sku: String) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val query = "DELETE FROM Products WHERE SKU = ?"
            try {
                val connection = getConnection()
                val statement = connection?.prepareStatement(query)
                statement?.apply {
                    setString(1, sku)
                    executeUpdate()
                }
                connection?.close()
            } catch (e: SQLException) {
                Log.e("SQLServerDBHelper", "Delete Product Error: ${e.message}")
            }
        }
    }

    // Function to insert a new product
    fun addProduct(name: String, sku: String, category: String, quantity: Int, price: Double, supplier: String) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val query = "INSERT INTO Products (ProductName, SKU, Category, Quantity, Price, Supplier) VALUES (?, ?, ?, ?, ?, ?)"
            try {
                val connection = getConnection()
                val preparedStatement: PreparedStatement? = connection?.prepareStatement(query)
                preparedStatement?.apply {
                    setString(1, name)
                    setString(2, sku)
                    setString(3, category)
                    setInt(4, quantity)
                    setDouble(5, price)
                    setString(6, supplier)
                    executeUpdate()
                }
                connection?.close()
            } catch (e: SQLException) {
                Log.e("SQLServerDBHelper", "Insert Error: ${e.message}")
            }
        }
    }
}