package com.naviy.uz.clean_gen.generator

import org.json.JSONArray
import org.json.JSONObject

data class DartField(
    val name: String,
    val type: String,
    val isNullable: Boolean = false,
    val isList: Boolean = false
)

data class DartClass(
    val name: String,
    val fields: List<DartField>,
    val nestedClasses: List<DartClass> = emptyList()
)

object JsonParser {
    
    /**
     * Parse JSON string and generate Dart class structure
     */
    fun parseJson(json: String, className: String): DartClass? {
        if (json.isBlank()) return null
        
        return try {
            val jsonObject = JSONObject(json)
            parseJsonObject(jsonObject, className)
        } catch (e: Exception) {
            // If parsing fails, try as array
            try {
                val jsonArray = JSONArray(json)
                if (jsonArray.length() > 0) {
                    val firstItem = jsonArray.getJSONObject(0)
                    parseJsonObject(firstItem, className)
                } else {
                    null
                }
            } catch (e2: Exception) {
                null
            }
        }
    }
    
    private fun parseJsonObject(jsonObject: JSONObject, className: String): DartClass {
        val fields = mutableListOf<DartField>()
        val nestedClasses = mutableListOf<DartClass>()
        
        jsonObject.keys().forEach { key ->
            val value = jsonObject.get(key)
            val fieldName = key.toCamelCaseField()
            
            when {
                value is JSONObject -> {
                    val nestedClassName = key.toCamelCase()
                    val nestedClass = parseJsonObject(value, nestedClassName)
                    nestedClasses.add(nestedClass)
                    fields.add(DartField(fieldName, nestedClassName, false, false))
                }
                value is JSONArray -> {
                    if (value.length() > 0) {
                        val firstItem = value.get(0)
                        if (firstItem is JSONObject) {
                            val nestedClassName = key.toCamelCase().removeSuffix("s")
                            val nestedClass = parseJsonObject(firstItem, nestedClassName)
                            nestedClasses.add(nestedClass)
                            fields.add(DartField(fieldName, nestedClassName, false, true))
                        } else {
                            val dartType = inferDartType(firstItem)
                            fields.add(DartField(fieldName, dartType, false, true))
                        }
                    } else {
                        fields.add(DartField(fieldName, "dynamic", false, true))
                    }
                }
                value == JSONObject.NULL -> {
                    fields.add(DartField(fieldName, "dynamic", true, false))
                }
                else -> {
                    val dartType = inferDartType(value)
                    fields.add(DartField(fieldName, dartType, false, false))
                }
            }
        }
        
        return DartClass(className, fields, nestedClasses)
    }
    
    private fun inferDartType(value: Any): String {
        return when (value) {
            is String -> "String"
            is Int -> "int"
            is Long -> "int"
            is Double -> "double"
            is Float -> "double"
            is Boolean -> "bool"
            else -> "dynamic"
        }
    }
    
    /**
     * Generate complete Dart class code with fromJson and toJson
     */
    fun generateDartClass(dartClass: DartClass, originalJson: String? = null): String {
        val buffer = StringBuilder()
        
        // Generate nested classes first
        dartClass.nestedClasses.forEach { nested ->
            buffer.append(generateDartClass(nested, null))
            buffer.append("\n\n")
        }
        
        // Add JSON comment if provided
        if (originalJson != null && originalJson.isNotBlank()) {
            buffer.append("/// Generated from JSON:\n")
            buffer.append("/// ```json\n")
            originalJson.lines().forEach { line ->
                buffer.append("/// ${line.trim()}\n")
            }
            buffer.append("/// ```\n")
        }
        
        // Main class
        buffer.append("class ${dartClass.name} {\n")
        
        // Fields
        dartClass.fields.forEach { field ->
            val nullSuffix = if (field.isNullable) "?" else ""
            val type = if (field.isList) "List<${field.type}>" else field.type
            buffer.append("  final $type$nullSuffix ${field.name};\n")
        }
        
        buffer.append("\n")
        
        // Constructor
        buffer.append("  ${dartClass.name}({\n")
        dartClass.fields.forEach { field ->
            val required = if (!field.isNullable) "required " else ""
            buffer.append("    ${required}this.${field.name},\n")
        }
        buffer.append("  });\n\n")
        
        // fromJson
        buffer.append("  factory ${dartClass.name}.fromJson(Map<String, dynamic> json) {\n")
        buffer.append("    return ${dartClass.name}(\n")
        dartClass.fields.forEach { field ->
            val jsonKey = field.name.toSnakeCase()
            when {
                field.isList -> {
                    if (isComplexType(field.type)) {
                        buffer.append("      ${field.name}: json['$jsonKey'] != null\n")
                        buffer.append("          ? (json['$jsonKey'] as List).map((e) => ${field.type}.fromJson(e)).toList()\n")
                        buffer.append("          : ${if (field.isNullable) "null" else "[]"},\n")
                    } else {
                        buffer.append("      ${field.name}: json['$jsonKey'] != null\n")
                        buffer.append("          ? List<${field.type}>.from(json['$jsonKey'])\n")
                        buffer.append("          : ${if (field.isNullable) "null" else "[]"},\n")
                    }
                }
                isComplexType(field.type) -> {
                    buffer.append("      ${field.name}: json['$jsonKey'] != null\n")
                    buffer.append("          ? ${field.type}.fromJson(json['$jsonKey'])\n")
                    buffer.append("          : ${if (field.isNullable) "null" else "throw Exception('${field.name} is required')"},\n")
                }
                else -> {
                    val defaultValue = getDefaultValue(field.type)
                    buffer.append("      ${field.name}: json['$jsonKey']")
                    if (field.type != "dynamic") {
                        buffer.append(" as ${field.type}?")
                    }
                    if (!field.isNullable && defaultValue != null) {
                        buffer.append(" ?? $defaultValue")
                    }
                    buffer.append(",\n")
                }
            }
        }
        buffer.append("    );\n")
        buffer.append("  }\n\n")
        
        // toJson
        buffer.append("  Map<String, dynamic> toJson() {\n")
        buffer.append("    return {\n")
        dartClass.fields.forEach { field ->
            val jsonKey = field.name.toSnakeCase()
            when {
                field.isList -> {
                    if (isComplexType(field.type)) {
                        buffer.append("      '$jsonKey': ${field.name}${if (field.isNullable) "?" else ""}.map((e) => e.toJson()).toList(),\n")
                    } else {
                        buffer.append("      '$jsonKey': ${field.name},\n")
                    }
                }
                isComplexType(field.type) -> {
                    buffer.append("      '$jsonKey': ${field.name}${if (field.isNullable) "?" else ""}.toJson(),\n")
                }
                else -> {
                    buffer.append("      '$jsonKey': ${field.name},\n")
                }
            }
        }
        buffer.append("    };\n")
        buffer.append("  }\n")
        
        buffer.append("}\n")
        
        return buffer.toString()
    }
    
    private fun isComplexType(type: String): Boolean {
        return type !in listOf("String", "int", "double", "bool", "dynamic", "num")
    }
    
    private fun getDefaultValue(type: String): String? {
        return when (type) {
            "String" -> "\"\""
            "int" -> "0"
            "double" -> "0.0"
            "bool" -> "false"
            else -> null
        }
    }
}

fun String.toCamelCaseField(): String {
    val parts = this.split("_", "-")
    if (parts.isEmpty()) return this
    
    return parts[0].lowercase() + parts.drop(1).joinToString("") {
        it.replaceFirstChar { char -> char.uppercaseChar() }
    }
}
