package com.naviy.uz.clean_gen.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class FeatureDialog(project: Project?) : DialogWrapper(project) {
    private val contentPanel = JPanel(BorderLayout())
    private val nameTextField = JTextField(35)
    private val functionsPanel = JPanel()
    private val functionsNameTextFields = mutableListOf<JTextField>()
    private val apiPointTextFields = mutableListOf<JTextField>()
    private val modelNameTextFields = mutableListOf<JTextField>()
    private val addFunctionButton = JButton("+") // Qo‘shimcha input qo‘shish tugmasi
    private val removeFunctionButton = JButton("-") // O‘chirish tugmasi

    init {
        title = "Clean-Architecture Generator"
        initUI()
        init()
    }

    private fun initUI() {
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)

        val namePanel = JPanel(FlowLayout(FlowLayout.LEFT))
        namePanel.add(JLabel("Feature Name:"))
        namePanel.add(nameTextField)
        contentPanel.add(namePanel)
        val functionsLabelPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        functionsLabelPanel.layout = GridLayout(1, 3)
        functionsLabelPanel.preferredSize = functionsLabelPanel.preferredSize.apply {
            width = 800
            height = 30
        }
        functionsLabelPanel.add(JLabel("Function Names:"))
        functionsLabelPanel.add(JLabel("API Points:"))
        functionsLabelPanel.add(JLabel("Model Names:"))
        contentPanel.add(functionsLabelPanel)

        val functionsContainer = JPanel()
        functionsContainer.layout = BoxLayout(functionsContainer, BoxLayout.Y_AXIS)

        // **GridLayout bilan ustun formatda joylashuv**
        functionsPanel.layout = GridLayout(0, 1) // 0 = cheksiz qator, 1 = faqat bitta ustun
        functionsContainer.add(functionsPanel)

        val addFunctionPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        addFunctionPanel.add(addFunctionButton)
        addFunctionPanel.add(removeFunctionButton)
        functionsContainer.add(addFunctionPanel)

        contentPanel.add(functionsContainer)

        addFunctionButton.addActionListener {
            addFunctionField()
        }
        removeFunctionButton.addActionListener {
            removeFunctionField()
        }
    }

    private fun addFunctionField() {
        // **Yangi function uchun input**
        val functionField = JTextField(20)
        functionsNameTextFields.add(functionField)

        // **Yangi api point uchun input**
        val apiPointField = JTextField(20)
        apiPointField.text = "/"
        apiPointTextFields.add(apiPointField)

        // **Qo‘shimcha model uchun input**
        val modelField = JTextField(20)
        modelNameTextFields.add(modelField)

        // **Har bir function uchun panel**
        val functionPanel = JPanel()
        functionPanel.layout = FlowLayout(FlowLayout.LEFT)
        functionPanel.add(functionField)
        functionPanel.add(apiPointField)
        functionPanel.add(modelField)
        // **Ustun bo‘ylab qo‘shish**
        functionsPanel.add(functionPanel)

        functionsPanel.revalidate()
        functionsPanel.repaint()
    }

    private fun removeFunctionField() {
        if (functionsNameTextFields.isNotEmpty()) {
            val lastFunctionField = functionsNameTextFields.removeLast()
            val lastApiPointField = apiPointTextFields.removeLast()
            val lastModelField = modelNameTextFields.removeLast()
            functionsPanel.remove(lastFunctionField.parent)
            functionsPanel.remove(lastApiPointField.parent)
            functionsPanel.remove(lastModelField.parent)
            functionsPanel.revalidate()
            functionsPanel.repaint()
        }
    }


    override fun createCenterPanel(): JComponent = contentPanel

    override fun getPreferredFocusedComponent(): JComponent = nameTextField

    fun getName(): String = nameTextField.text

    fun getFunctionsName(): List<String> = functionsNameTextFields.map { it.text }

    fun getApiPoints(): List<String> = apiPointTextFields.map { it.text }

    fun getModelsName(): List<String> = modelNameTextFields.map { it.text }
}
