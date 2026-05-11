package com.naviy.uz.clean_gen.action

import com.intellij.codeInsight.actions.OptimizeImportsProcessor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.naviy.uz.clean_gen.generator.*
import com.naviy.uz.clean_gen.ui.FeatureDialogNew

class ActionGenerateFlutterV2 : AnAction() {
    
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val dialog = FeatureDialogNew(actionEvent.project)
        if (dialog.showAndGet()) {
            generate(
                actionEvent.dataContext,
                dialog.getName(),
                dialog.getFunctionConfigs()
            )
        }
    }

    private fun generate(
        dataContext: DataContext,
        root: String?,
        functions: List<com.naviy.uz.clean_gen.ui.FunctionConfig>
    ) {
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        val selected = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext) ?: return

        var folder = if (selected.isDirectory) selected else selected.parent
        
        WriteCommandAction.runWriteCommandAction(project) {
            // Create root folder
            if (!root.isNullOrBlank()) {
                val result = GeneratorV2.createFolder(
                    project, folder, root
                ) ?: return@runWriteCommandAction
                folder = result[root]
            }
            
            // Create folder structure
            GeneratorV2.createFolder(
                project, folder, "data", "repositories", "data_sources", "models"
            )
            GeneratorV2.createFolder(
                project, folder, "domain", "repositories", "use_cases", "entities"
            )
            GeneratorV2.createFolder(
                project, folder, "presentation", "manager", "pages", "widgets"
            )

            val featureName = root ?: "feature"
            val createdDartFilesList = mutableListOf<VirtualFile>()
            val codeStyleManager = CodeStyleManager.getInstance(project)
            val psiManager = PsiManager.getInstance(project)

            // Get folders
            val dataSourcesFolder = folder.findChild("data")?.findChild("data_sources")!!
            val modelsFolder = folder.findChild("data")?.findChild("models")!!
            val repositoriesImplFolder = folder.findChild("data")?.findChild("repositories")!!
            val repositoriesFolder = folder.findChild("domain")?.findChild("repositories")!!
            val useCasesFolder = folder.findChild("domain")?.findChild("use_cases")!!
            val managerFolder = folder.findChild("presentation")?.findChild("manager")!!

            // Generate model files from JSON
            functions.forEach { func ->
                // Request model
                if (func.requestJson.isNotBlank()) {
                    val isBodyRequest = func.httpMethod in listOf(
                        com.naviy.uz.clean_gen.ui.HttpMethod.POST,
                        com.naviy.uz.clean_gen.ui.HttpMethod.PUT,
                        com.naviy.uz.clean_gen.ui.HttpMethod.DELETE
                    )
                    val requestSuffix = if (isBodyRequest) "BodyModel" else "ParamModel"
                    val requestFileName = if (isBodyRequest) "body_model" else "param_model"
                    val requestClassName = "${func.name.toCamelCase()}$requestSuffix"
                    val requestClass = JsonParser.parseJson(
                        func.requestJson,
                        requestClassName
                    )
                    requestClass?.let {
                        val dartCode = JsonParser.generateDartClass(it, func.requestJson)
                        GeneratorV2.createDartFile(
                            modelsFolder,
                            "${func.name.toSnakeCase()}_$requestFileName",
                            dartCode
                        )?.let { file -> createdDartFilesList.add(file) }
                    }
                }
                
                // Response model
                if (func.responseJson.isNotBlank()) {
                    val responseClassName = "${func.name.toCamelCase()}Model"
                    val responseClass = JsonParser.parseJson(
                        func.responseJson,
                        responseClassName
                    )
                    responseClass?.let {
                        val dartCode = JsonParser.generateDartClass(it, func.responseJson)
                        GeneratorV2.createDartFile(
                            modelsFolder,
                            "${func.name.toSnakeCase()}_model",
                            dartCode
                        )?.let { file -> createdDartFilesList.add(file) }
                    }
                }
            }

            // Generate API Service
            GeneratorV2.createDartFile(
                dataSourcesFolder,
                "${featureName.toSnakeCase()}_api_service",
                GeneratorV2.generateApiService(featureName, functions)
            )?.let { createdDartFilesList.add(it) }

            // Generate Repository Interface
            GeneratorV2.createDartFile(
                repositoriesFolder,
                "${featureName.toSnakeCase()}_repository",
                GeneratorV2.generateRepository(featureName, functions)
            )?.let { createdDartFilesList.add(it) }

            // Generate Repository Implementation
            GeneratorV2.createDartFile(
                repositoriesImplFolder,
                "${featureName.toSnakeCase()}_repository_impl",
                GeneratorV2.generateRepositoryImpl(featureName, functions)
            )?.let { createdDartFilesList.add(it) }

            // Generate Use Cases
            functions.forEach { func ->
                GeneratorV2.createDartFile(
                    useCasesFolder,
                    "${func.name.toSnakeCase()}_use_case",
                    GeneratorV2.generateUseCase(featureName, func)
                )?.let { createdDartFilesList.add(it) }
            }

            // Generate Cubit
            GeneratorV2.createDartFile(
                managerFolder,
                "${featureName.toSnakeCase()}_cubit",
                GeneratorV2.generateCubit(featureName, functions)
            )?.let { createdDartFilesList.add(it) }

            // Generate State
            GeneratorV2.createDartFile(
                managerFolder,
                "${featureName.toSnakeCase()}_state",
                GeneratorV2.generateState(featureName, functions)
            )?.let { createdDartFilesList.add(it) }

            // Generate DI
            GeneratorV2.createDartFile(
                folder,
                "${featureName.toSnakeCase()}_di",
                GeneratorV2.generateDI(featureName, functions)
            )?.let { createdDartFilesList.add(it) }

            // Format and optimize imports
            val psiFiles = createdDartFilesList.mapNotNull { psiManager.findFile(it) }
            psiFiles.forEach {
                codeStyleManager.reformat(it)
                OptimizeImportsProcessor(project, it).run()
            }
        }
    }
}
