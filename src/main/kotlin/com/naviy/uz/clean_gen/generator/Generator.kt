package com.naviy.uz.clean_gen.generator

import ai.grazie.utils.capitalize
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


        private fun createDartFile(
            directory: VirtualFile,
            fileName: String,
            content: String
        ) {
            val dartFileName = "$fileName.dart"
            val dartFile = directory.findOrCreateChildData(this, dartFileName)
            try {
                VfsUtil.saveText(dartFile, content)
            } catch (e: IOException) {
                e.printStackTrace()
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
            apiPoints: List<String>
        ): String = """
import 'package:retrofit/retrofit.dart';
import 'package:dio/dio.dart';

part '${name}_api_service.g.dart';

@RestApi()
abstract class ${name.capitalize()}ApiService {
    factory ${name.capitalize()}ApiService(Dio dio, {String baseUrl}) = _${name.capitalize()}ApiService;
    
    /// URLS
    ${
            functions.mapIndexed { index, function ->
                "static const String _${function.toSnakeCase()} = '${apiPoints[index]}';"
            }.joinToString("\n")
        }
    }

    /// REQUESTS
    ${
            functions.mapIndexed { index, function ->
                """
    @GET(_${function.toSnakeCase()})
    Future<HttpResponse<BaseResponse<MODEL_HERE>>> ${function}();
                """.trimIndent()
            }.joinToString("\n")
        }
    }
}
        """

        fun repositoryImplContent(
            name: String,
            functions: List<String>,
        ): String = """
class ${name.capitalize()}RepositoryImpl with BaseRepository implements ${name.capitalize()}Repository {
  final ${name.capitalize()}ApiService _apiService;
  ${name.capitalize()}RepositoryImpl(this._apiService);
  
  @override
  Future<DataState<ResponseModel>> funcName(REQUEST_BODY body) async =>
    await handleResponse(response: _apiService.funcName(body));
    
    ${
            functions.mapIndexed { index, function ->
                """
    @override
    Future<DataState<MODEL_HERE>> ${function}(REQUEST_BODY body) async =>
        await handleResponse(response: _apiService.${function}(body));
                """.trimIndent()
            }.joinToString("\n")
        }
        
    }
}
"""

        fun repositoryContent(
            name: String,
            functions: List<String>,
        ): String = """
            abstract class ${name.capitalize()}Repository {
    ${
            functions.mapIndexed { _, function ->
                """
    Future<DataState<MODEL_HERE>> ${function}();
                """.trimIndent()
            }
        }
"""

        fun useCaseContent(
            name: String,
            function: String
        ): String = """
            class ${function.capitalize()}UseCase implements UseCase<DataState<MODEL_HERE>, REQUEST_BODY> {
  final ${name.capitalize()}Repository _repository;

  ${function.capitalize()}UseCase(this._repository);

  @override
  Future<DataState<MODEL_HERE>> call({required REQUEST_BODY params}) async => 
        await _repository.${function}(params);
}
"""

        fun diContent(
            name: String,
            functions: List<String>,
        ): String = """
            Future<void> ${name.toSnakeCase()}DI() async {
  // DataSources
  locator.registerSingleton(${name.capitalize()}ApiService(locator()));

  // Repositories
  locator.registerSingleton<${name.capitalize()}Repository>(${name.capitalize()}RepositoryImpl(locator()));

  // UseCases
  ${
            functions.joinToString("\n") { function ->
                "locator.registerSingleton(${function.capitalize()}UseCase(locator()));"
            }
        }

  // Blocs
  locator.registerFactory<${name.capitalize()}Cubit>(() => ${name.capitalize()}Cubit());
}
           """
    }
}

private fun String.toSnakeCase(): String {
    return this.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase(Locale.ROOT)
}
