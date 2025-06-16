package com.naviy.uz.clean_gen.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.naviy.uz.clean_gen.generator.Contents
import com.naviy.uz.clean_gen.generator.Generator
import com.naviy.uz.clean_gen.generator.toCamelCase
import com.naviy.uz.clean_gen.generator.toSnakeCase
import com.naviy.uz.clean_gen.ui.FeatureDialog

class ActionGenerateFlutter : AnAction() {
    /**
     * Is called by the context action menu entry with an [actionEvent]
     */
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val dialog = FeatureDialog(actionEvent.project)
        if (dialog.showAndGet()) {
            generate(
                actionEvent.dataContext,
                dialog.getName(),
                dialog.getFunctionsName(),
                dialog.getApiPoints(),
                dialog.getModelsName()
            )
        }
    }

    /**
     * Generates the Flutter Clean-Architecture structure in a [dataContext].
     * If a [root] String is provided, it will create the structure in a new folder.
     */
    private fun generate(
        dataContext: DataContext,
        root: String?,
        functions: List<String>,
        apiPoints: List<String>,
        models: List<String>
    ) {
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return
        val selected = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext) ?: return

        var folder = if (selected.isDirectory) selected else selected.parent
        WriteCommandAction.runWriteCommandAction(project) {
            if (!root.isNullOrBlank()) {
                val result = Generator.createFolder(
                    project, folder, root
                ) ?: return@runWriteCommandAction
                folder = result[root]
            }
            Generator.createFolder(
                project, folder,
                "data",
                "repositories", "data_sources", "models"
            )
            Generator.createFolder(
                project, folder,
                "domain",
                "repositories", "use_cases", "entities"
            )
            Generator.createFolder(
                project, folder,
                "presentation",
                "manager", "pages", "widgets"
            )

            // generate dart files
            val dataSourcesFolder = folder.findChild("data")?.findChild("data_sources")!!
            val modelsFolder = folder.findChild("data")?.findChild("models")!!
            val repositoriesImplFolder = folder.findChild("data")?.findChild("repositories")!!
            val repositoriesFolder = folder.findChild("domain")?.findChild("repositories")!!
            val useCasesFolder = folder.findChild("domain")?.findChild("use_cases")!!
            val presentationFolder = folder.findChild("presentation")!!
            val featureName = root ?: "feature"

            Generator.createDartFile(
                dataSourcesFolder, "${featureName.toSnakeCase()}_api_service",
                Contents.apiServiceContent(featureName, functions, apiPoints, models)
            )
            models.forEach(
                fun(model) {
                    Generator.createDartFile(
                        modelsFolder, model.toSnakeCase(),
                        "class ${model.toCamelCase()} {}"
                    )
                }
            )
            Generator.createDartFile(
                repositoriesImplFolder, "${featureName.toSnakeCase()}_repository_impl",
                Contents.repositoryImplContent(featureName, functions,models)
            )
            Generator.createDartFile(
                repositoriesFolder, "${featureName.toSnakeCase()}_repository",
                Contents.repositoryContent(featureName, functions,models)
            )
            functions
                .forEach {
                    Generator.createDartFile(
                        useCasesFolder, "${it.toSnakeCase()}_use_case",
                        Contents.useCaseContent(featureName, it, models.getOrNull(functions.indexOf(it)))
                    )
                }
            Generator.createDartFile(
                folder, "${featureName.toSnakeCase()}_di",
                Contents.diContent(featureName, functions)
            )
        }
    }
}