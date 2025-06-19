package com.naviy.uz.clean_gen.generator

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.naviy.uz.clean_gen.ui.Notifier
import java.io.IOException
import java.util.Locale

/**
 * Generator Factory to create structure
 */
interface Generator {
    companion object {
        /**
         * Creates a [parent] folder and its [children] in a given [folder].
         * [project] is needed for the notifications if there is an error or a warning situation.
         * @return null if an error occurred or the a map of all virtual files created
         */
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
    }
}

// contents
class Contents {
    companion object {
        fun apiServiceContent(
            name: String,
            functions: List<String>,
            apiPoints: List<String>,
            models: List<String>
        ): String = """
import 'package:retrofit/retrofit.dart';
import 'package:dio/dio.dart';
${
            models.mapIndexed { _, model ->
                "import '../models/${model.toSnakeCase()}.dart';"
            }.joinToString("")
        }
part '${name}_api_service.g.dart';

@RestApi()
abstract class ${name.toCamelCase()}ApiService {
    factory ${name.toCamelCase()}ApiService(Dio dio, {String baseUrl}) = _${name.toCamelCase()}ApiService;
    
    /// URLS
    ${
            functions.mapIndexed { index, function ->
                "static const String _${function} = '${apiPoints[index]}';"
            }.joinToString("\n")
        }

    /// REQUESTS
    ${
            functions.mapIndexed { _, function ->
                """
    @GET(_${function.toSnakeCase()})
    Future<HttpResponse<BaseResponse<${
                    models.getOrNull(functions.indexOf(function)) ?: "MODEL_HERE"
                }>>> ${function}();
                """.trimIndent()
            }.joinToString("\n")
        }
}
        """

        fun repositoryImplContent(
            name: String,
            functions: List<String>,
            models: List<String>
        ): String = """
import '../../domain/repositories/${name.toSnakeCase()}_repository.dart';
import '../data_sources/${name.toSnakeCase()}_api_service.dart';
${
            models.mapIndexed { _, model ->
                "import '../models/${model.toSnakeCase()}.dart';"
            }.joinToString("")
        }
class ${name.toCamelCase()}RepositoryImpl with BaseRepository implements ${name.toCamelCase()}Repository {
  final ${name.toCamelCase()}ApiService _apiService;
  ${name.toCamelCase()}RepositoryImpl(this._apiService);
  
    ${
            functions.mapIndexed { _, function ->
                """
    @override
    Future<DataState<${
                    models.getOrNull(functions.indexOf(function)) ?: "MODEL_HERE"
                }>> ${function}(REQUEST_BODY body) async =>
        await handleResponse(response: _apiService.${function}(body));
                """.trimIndent()
            }.joinToString("\n")
        }
}
"""

        fun repositoryContent(
            name: String,
            functions: List<String>,
            models: List<String>
        ): String = """
${
            models.mapIndexed { _, model ->
                "import '../../data/models/${model.toSnakeCase()}.dart';"
            }.joinToString("")
        }        
            abstract class ${name.toCamelCase()}Repository {
    ${
            functions.mapIndexed { _, function ->
                """
    Future<DataState<${
                    models.getOrNull(functions.indexOf(function)) ?: "MODEL_HERE"
                }>> ${function}();
                """.trimIndent()
            }.joinToString("\n")
        }
        }
"""

        fun useCaseContent(
            name: String,
            function: String,
            model: String?
        ): String = """
import '../../data/models/${model?.toSnakeCase()}.dart';

import '../repositories/${name.toSnakeCase()}_repository.dart';


class ${function.toCamelCase()}UseCase implements UseCase<DataState<${
            model ?: "MODEL_HERE"
        }>, REQUEST_BODY> {
  final ${name.toCamelCase()}Repository _repository;

  ${function.toCamelCase()}UseCase(this._repository);

  @override
  Future<DataState<${model ?: "MODEL_HERE"}>> call({required REQUEST_BODY params}) async => 
        await _repository.${function}(params);
}
"""

        fun diContent(
            name: String,
            functions: List<String>,
        ): String = """
import 'data/data_sources/${name.toSnakeCase()}_api_service.dart';
import 'data/repositories/${name.toSnakeCase()}_repository_impl.dart';
import 'domain/repositories/${name.toSnakeCase()}_repository.dart';
${
            functions.mapIndexed { _, function ->
                "import 'domain/use_cases/${function.toSnakeCase()}_use_case.dart';"
            }.joinToString("")
}
import 'presentation/manager/${name.toSnakeCase()}_cubit.dart';
            
  Future<void> ${name.toCamelCase()}DI() async {
  // DataSources
  locator.registerSingleton(${name.toCamelCase()}ApiService(locator()));

  // Repositories
  locator.registerSingleton<${name.toCamelCase()}Repository>(${name.toCamelCase()}RepositoryImpl(locator()));

  // UseCases
  ${
            functions.joinToString("\n") { function ->
                "locator.registerSingleton(${function.toCamelCase()}UseCase(locator()));"
            }
        }

  // Blocs
  locator.registerFactory<${name.toCamelCase()}Cubit>(() => ${name.toCamelCase()}Cubit());
}
           """
    }
}

fun String.toSnakeCase(): String {
    return this.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase(Locale.ROOT)
}

fun String.toCamelCase(): String {
    return this.split("_").joinToString("") {
        it.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
        }
    }
}
