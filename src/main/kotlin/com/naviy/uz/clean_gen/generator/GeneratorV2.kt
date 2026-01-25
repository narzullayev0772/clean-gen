package com.naviy.uz.clean_gen.generator

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.naviy.uz.clean_gen.ui.FunctionConfig
import com.naviy.uz.clean_gen.ui.HttpMethod
import com.naviy.uz.clean_gen.ui.Notifier
import java.io.IOException

/**
 * Enhanced Generator with JSON support and HTTP methods
 */
object GeneratorV2 {
    
    fun createFolder(
        project: Project,
        folder: VirtualFile,
        parent: String,
        vararg children: String
    ): Map<String, VirtualFile>? {
        try {
            for (child in folder.children) {
                if (child.name == parent) {
                    Notifier.warning(project, "Directory [$parent] already exists")
                    return null
                }
            }
            val mapOfFolder = mutableMapOf<String, VirtualFile>()
            mapOfFolder[parent] = folder.createChildDirectory(folder, parent)
            for (child in children) {
                mapOfFolder[child] =
                    mapOfFolder[parent]?.createChildDirectory(mapOfFolder[parent], child)
                        ?: throw IOException()
            }
            return mapOfFolder
        } catch (e: IOException) {
            Notifier.warning(project, "Couldn't create $parent directory")
            e.printStackTrace()
            return null
        }
    }

    fun createDartFile(
        directory: VirtualFile,
        fileName: String,
        content: String
    ): VirtualFile? {
        val dartFileName = "$fileName.dart"
        val dartFile = directory.findOrCreateChildData(this, dartFileName)
        try {
            VfsUtil.saveText(dartFile, content.trimIndent())
            return dartFile
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun generateApiService(
        featureName: String,
        functions: List<FunctionConfig>
    ): String {
        val requestImports = mutableSetOf<String>()
        val responseImports = mutableSetOf<String>()
        
        functions.forEach { func ->
            if (func.requestJson.isNotBlank()) {
                val isBodyRequest = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                val requestFileName = if (isBodyRequest) "body_model" else "param_model"
                requestImports.add("import '../models/${func.name.toSnakeCase()}_$requestFileName.dart';")
            }
            if (func.responseJson.isNotBlank()) {
                responseImports.add("import '../models/${func.name.toSnakeCase()}_model.dart';")
            }
        }
        
        return """
import 'package:retrofit/retrofit.dart';
import 'package:dio/dio.dart';
${requestImports.joinToString("\n")}
${responseImports.joinToString("\n")}

part '${featureName.toSnakeCase()}_api_service.g.dart';

@RestApi()
abstract class ${featureName.toCamelCase()}ApiService {
    factory ${featureName.toCamelCase()}ApiService(Dio dio, {String baseUrl}) = _${featureName.toCamelCase()}ApiService;
    
    ${generateApiMethods(functions)}
}
        """.trimIndent()
    }
    
    private fun generateApiMethods(functions: List<FunctionConfig>): String {
        return functions.joinToString("\n\n") { func ->
            val httpAnnotation = when (func.httpMethod) {
                HttpMethod.GET -> "@GET('${func.apiPoint}')"
                HttpMethod.POST -> "@POST('${func.apiPoint}')"
                HttpMethod.PUT -> "@PUT('${func.apiPoint}')"
                HttpMethod.DELETE -> "@DELETE('${func.apiPoint}')"
            }
            
            val isBodyRequest = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
            val requestSuffix = if (isBodyRequest) "BodyModel" else "ParamModel"
            val requestClassName = "${func.name.toCamelCase()}$requestSuffix"
            val responseClassName = "${func.name.toCamelCase()}Model"
            
            val hasRequestBody = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT) && func.requestJson.isNotBlank()
            val requestType = if (hasRequestBody) requestClassName else null
            val responseType = if (func.responseJson.isNotBlank()) {
                responseClassName
            } else {
                "dynamic"
            }
            
            val parameters = if (hasRequestBody) {
                "@Body() $requestType request"
            } else if (func.httpMethod == HttpMethod.GET && func.requestJson.isNotBlank()) {
                // For GET with params, generate query parameters
                val requestClass = JsonParser.parseJson(func.requestJson, requestClassName)
                requestClass?.fields?.joinToString(", ") { field ->
                    "@Query('${field.name}') ${field.type}? ${field.name}"
                } ?: ""
            } else {
                ""
            }
            
            """
    $httpAnnotation
    Future<HttpResponse<$responseType>> ${func.name}($parameters);
            """.trimIndent()
        }
    }

    fun generateRepository(
        featureName: String,
        functions: List<FunctionConfig>
    ): String {
        val imports = functions.mapNotNull { func ->
            if (func.responseJson.isNotBlank()) {
                "import '../../data/models/${func.name.toSnakeCase()}_model.dart';"
            } else null
        }.distinct()
        
        val requestImports = functions.mapNotNull { func ->
            if (func.requestJson.isNotBlank()) {
                val isBodyRequest = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                val requestFileName = if (isBodyRequest) "body_model" else "param_model"
                "import '../../data/models/${func.name.toSnakeCase()}_$requestFileName.dart';"
            } else null
        }.distinct()
        
        return """
${(imports + requestImports).distinct().joinToString("\n")}

abstract class ${featureName.toCamelCase()}Repository {
    ${functions.joinToString("\n    ") { func ->
            val isBodyRequest = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
            val requestSuffix = if (isBodyRequest) "BodyModel" else "ParamModel"
            val responseClassName = "${func.name.toCamelCase()}Model"
            val requestClassName = "${func.name.toCamelCase()}$requestSuffix"
            val responseType = if (func.responseJson.isNotBlank()) responseClassName else "dynamic"
            val hasRequest = func.requestJson.isNotBlank()
            val requestParam = if (hasRequest) "$requestClassName request" else ""
            "Future<DataState<$responseType>> ${func.name}($requestParam);"
        }}
}
        """.trimIndent()
    }

    fun generateRepositoryImpl(
        featureName: String,
        functions: List<FunctionConfig>
    ): String {
        val imports = mutableSetOf<String>()
        imports.add("import '../../domain/repositories/${featureName.toSnakeCase()}_repository.dart';")
        imports.add("import '../data_sources/${featureName.toSnakeCase()}_api_service.dart';")
        
        functions.forEach { func ->
            if (func.requestJson.isNotBlank()) {
                val isBodyRequest = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                val requestFileName = if (isBodyRequest) "body_model" else "param_model"
                imports.add("import '../models/${func.name.toSnakeCase()}_$requestFileName.dart';")
            }
            if (func.responseJson.isNotBlank()) {
                imports.add("import '../models/${func.name.toSnakeCase()}_model.dart';")
            }
        }
        
        return """
${imports.joinToString("\n")}

class ${featureName.toCamelCase()}RepositoryImpl with BaseRepository implements ${featureName.toCamelCase()}Repository {
    final ${featureName.toCamelCase()}ApiService _apiService;
    
    ${featureName.toCamelCase()}RepositoryImpl(this._apiService);
    
    ${functions.joinToString("\n\n    ") { func ->
            val isBodyRequest = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
            val requestSuffix = if (isBodyRequest) "BodyModel" else "ParamModel"
            val responseClassName = "${func.name.toCamelCase()}Model"
            val requestClassName = "${func.name.toCamelCase()}$requestSuffix"
            val responseType = if (func.responseJson.isNotBlank()) responseClassName else "dynamic"
            val hasRequestBody = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT) && func.requestJson.isNotBlank()
            val hasRequest = func.requestJson.isNotBlank()
            val requestParam = if (hasRequest) "$requestClassName request" else ""
            
            val apiCall = if (hasRequestBody) {
                "_apiService.${func.name}(request)"
            } else if (func.httpMethod == HttpMethod.GET && func.requestJson.isNotBlank()) {
                val requestClass = JsonParser.parseJson(func.requestJson, requestClassName)
                val params = requestClass?.fields?.joinToString(", ") { "request.${it.name}" } ?: ""
                "_apiService.${func.name}($params)"
            } else {
                "_apiService.${func.name}()"
            }
            
            """
@override
    Future<DataState<$responseType>> ${func.name}($requestParam) async =>
        await handleResponse(response: $apiCall);
            """.trimIndent()
        }}
}
        """.trimIndent()
    }

    fun generateUseCase(
        featureName: String,
        func: FunctionConfig
    ): String {
        val hasRequest = func.requestJson.isNotBlank()
        val isBodyRequest = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
        val requestSuffix = if (isBodyRequest) "BodyModel" else "ParamModel"
        val requestFileName = if (isBodyRequest) "body_model" else "param_model"
        val requestClassName = "${func.name.toCamelCase()}$requestSuffix"
        val responseClassName = "${func.name.toCamelCase()}Model"
        
        val requestType = if (hasRequest) requestClassName else "void"
        val responseType = if (func.responseJson.isNotBlank()) responseClassName else "dynamic"
        
        val imports = mutableListOf<String>()
        imports.add("import '../../domain/repositories/${featureName.toSnakeCase()}_repository.dart';")
        if (hasRequest) {
            imports.add("import '../../data/models/${func.name.toSnakeCase()}_$requestFileName.dart';")
        }
        if (func.responseJson.isNotBlank()) {
            imports.add("import '../../data/models/${func.name.toSnakeCase()}_model.dart';")
        }
        
        val callParams = if (hasRequest) "params" else ""
        
        return """
${imports.joinToString("\n")}

class ${func.name.toCamelCase()}UseCase implements UseCase<DataState<$responseType>, $requestType> {
    final ${featureName.toCamelCase()}Repository _repository;

    ${func.name.toCamelCase()}UseCase(this._repository);

    @override
    Future<DataState<$responseType>> call({required $requestType params}) async => 
        await _repository.${func.name}($callParams);
}
        """.trimIndent()
    }

    fun generateCubit(
        featureName: String,
        functions: List<FunctionConfig>
    ): String {
        val useCaseImports = functions.joinToString("\n") { func ->
            "import '../../domain/use_cases/${func.name.toSnakeCase()}_use_case.dart';"
        }
        
        val modelImports = mutableSetOf<String>()
        functions.forEach { func ->
            if (func.responseJson.isNotBlank()) {
                modelImports.add("import '../../data/models/${func.name.toSnakeCase()}_model.dart';")
            }
            if (func.requestJson.isNotBlank()) {
                val isBodyRequest = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                val requestFileName = if (isBodyRequest) "body_model" else "param_model"
                modelImports.add("import '../../data/models/${func.name.toSnakeCase()}_$requestFileName.dart';")
            }
        }
        
        return """
import 'package:bloc/bloc.dart';
$useCaseImports
${modelImports.joinToString("\n")}

part '${featureName.toSnakeCase()}_state.dart';

class ${featureName.toCamelCase()}Cubit extends Cubit<${featureName.toCamelCase()}State> {
    ${functions.joinToString("\n    ") { func ->
            "final ${func.name.toCamelCase()}UseCase _${func.name}UseCase;"
        }}
    
    ${featureName.toCamelCase()}Cubit(
        ${functions.joinToString(",\n        ") { func ->
            "this._${func.name}UseCase"
        }}
    ) : super(${featureName.toCamelCase()}State.initial());
    
    ${functions.joinToString("\n\n    ") { func ->
            val isBodyRequest = func.httpMethod in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
            val requestSuffix = if (isBodyRequest) "BodyModel" else "ParamModel"
            val requestClassName = "${func.name.toCamelCase()}$requestSuffix"
            val hasRequest = func.requestJson.isNotBlank()
            val requestParam = if (hasRequest) "$requestClassName request" else ""
            val callParam = if (hasRequest) "request" else "null as void"
            """
Future<void> ${func.name}($requestParam) => Fetcher.fetchWithBase(
        fetcher: _${func.name}UseCase.call(params: $callParam),
        state: state.${func.name}State,
        emitter: (newState) => emit(state.copyWith(${func.name}State: newState)),
    );
            """.trimIndent()
        }}
}
        """.trimIndent()
    }

    fun generateState(
        featureName: String,
        functions: List<FunctionConfig>
    ): String {
        return """
part of '${featureName.toSnakeCase()}_cubit.dart';

class ${featureName.toCamelCase()}State {
    ${functions.joinToString("\n    ") { func ->
            val responseClassName = "${func.name.toCamelCase()}Model"
            val responseType = if (func.responseJson.isNotBlank()) responseClassName else "dynamic"
            "final BaseState<$responseType> ${func.name}State;"
        }}
    
    ${featureName.toCamelCase()}State({
        ${functions.joinToString(",\n        ") { func ->
            "required this.${func.name}State"
        }}
    });
    
    ${featureName.toCamelCase()}State copyWith({
        ${functions.joinToString(",\n        ") { func ->
            val responseClassName = "${func.name.toCamelCase()}Model"
            val responseType = if (func.responseJson.isNotBlank()) responseClassName else "dynamic"
            "BaseState<$responseType>? ${func.name}State"
        }}
    }) => ${featureName.toCamelCase()}State(
        ${functions.joinToString(",\n        ") { func ->
            "${func.name}State: ${func.name}State ?? this.${func.name}State"
        }}
    );
    
    factory ${featureName.toCamelCase()}State.initial() => ${featureName.toCamelCase()}State(
        ${functions.joinToString(",\n        ") { func ->
            "${func.name}State: BaseState.initial()"
        }}
    );
}
        """.trimIndent()
    }

    fun generateDI(
        featureName: String,
        functions: List<FunctionConfig>
    ): String {
        val useCaseImports = functions.joinToString("\n") { func ->
            "import 'domain/use_cases/${func.name.toSnakeCase()}_use_case.dart';"
        }
        
        return """
import 'data/data_sources/${featureName.toSnakeCase()}_api_service.dart';
import 'data/repositories/${featureName.toSnakeCase()}_repository_impl.dart';
import 'domain/repositories/${featureName.toSnakeCase()}_repository.dart';
$useCaseImports
import 'presentation/manager/${featureName.toSnakeCase()}_cubit.dart';

Future<void> ${featureName.toCamelCase()}DI() async {
    // DataSources
    locator.registerSingleton(${featureName.toCamelCase()}ApiService(locator()));

    // Repositories
    locator.registerSingleton<${featureName.toCamelCase()}Repository>(
        ${featureName.toCamelCase()}RepositoryImpl(locator())
    );

    // UseCases
    ${functions.joinToString("\n    ") { func ->
            "locator.registerSingleton(${func.name.toCamelCase()}UseCase(locator()));"
        }}

    // Cubit
    locator.registerFactory<${featureName.toCamelCase()}Cubit>(
        () => ${featureName.toCamelCase()}Cubit(
            ${functions.joinToString(",\n            ") { func ->
                "locator()"
            }}
        )
    );
}
        """.trimIndent()
    }
}
