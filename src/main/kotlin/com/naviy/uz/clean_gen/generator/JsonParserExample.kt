package com.naviy.uz.clean_gen.generator

/**
 * Example usage and test cases for JsonParser
 */
fun main() {
    // Example 1: Simple user object
    val userJson = """
        {
            "id": 1,
            "name": "John Doe",
            "email": "john@example.com",
            "age": 30,
            "is_active": true
        }
    """.trimIndent()
    
    val userClass = JsonParser.parseJson(userJson, "User")
    userClass?.let {
        println("=== User Model ===")
        println(JsonParser.generateDartClass(it, userJson))
        println()
    }
    
    // Example 2: Nested object
    val loginResponseJson = """
        {
            "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9",
            "user": {
                "id": 1,
                "name": "John Doe",
                "email": "john@example.com"
            },
            "expires_in": 3600
        }
    """.trimIndent()
    
    val loginResponseClass = JsonParser.parseJson(loginResponseJson, "LoginResponse")
    loginResponseClass?.let {
        println("=== Login Response Model ===")
        println(JsonParser.generateDartClass(it, loginResponseJson))
        println()
    }
    
    // Example 3: Array of objects
    val productsJson = """
        [
            {
                "id": 1,
                "name": "Product 1",
                "price": 99.99,
                "tags": ["electronics", "gadgets"],
                "in_stock": true
            }
        ]
    """.trimIndent()
    
    val productClass = JsonParser.parseJson(productsJson, "Product")
    productClass?.let {
        println("=== Product Model ===")
        println(JsonParser.generateDartClass(it, productsJson))
        println()
    }
    
    // Example 4: Complex nested structure
    val orderJson = """
        {
            "order_id": "ORD-12345",
            "customer": {
                "id": 1,
                "name": "John Doe",
                "email": "john@example.com"
            },
            "items": [
                {
                    "product_id": 1,
                    "name": "Product 1",
                    "quantity": 2,
                    "price": 99.99
                }
            ],
            "total": 199.98,
            "status": "pending"
        }
    """.trimIndent()
    
    val orderClass = JsonParser.parseJson(orderJson, "Order")
    orderClass?.let {
        println("=== Order Model ===")
        println(JsonParser.generateDartClass(it, orderJson))
        println()
    }
}
