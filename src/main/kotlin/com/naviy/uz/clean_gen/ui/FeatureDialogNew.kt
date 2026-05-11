package com.naviy.uz.clean_gen.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

enum class HttpMethod {
    GET, POST, PUT, DELETE
}

data class FunctionConfig(
    val name: String,
    val apiPoint: String,
    val httpMethod: HttpMethod,
    val requestJson: String,
    val responseJson: String
)

class FeatureDialogNew(project: Project?) : DialogWrapper(project) {
    private val contentPanel = JPanel(BorderLayout())
    private val nameTextField = JTextField(35)
    private val functionsPanel = JPanel()
    private val functionConfigs = mutableListOf<FunctionPanel>()
    private val addFunctionButton = JButton("+")
    private val removeFunctionButton = JButton("-")

    init {
        title = "Clean-Architecture Generator with JSON Support"
        initUI()
        init()
    }

    private fun initUI() {
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)

        // Feature name panel
        val namePanel = JPanel(FlowLayout(FlowLayout.LEFT))
        namePanel.add(JLabel("Feature Name:"))
        namePanel.add(nameTextField)
        contentPanel.add(namePanel)

        // Functions container with scroll
        val functionsContainer = JPanel()
        functionsContainer.layout = BoxLayout(functionsContainer, BoxLayout.Y_AXIS)

        functionsPanel.layout = BoxLayout(functionsPanel, BoxLayout.Y_AXIS)
        val scrollPane = JBScrollPane(functionsPanel)
        scrollPane.preferredSize = Dimension(900, 400)
        functionsContainer.add(scrollPane)

        // Buttons panel
        val buttonsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        buttonsPanel.add(addFunctionButton)
        buttonsPanel.add(removeFunctionButton)
        functionsContainer.add(buttonsPanel)

        contentPanel.add(functionsContainer)

        addFunctionButton.addActionListener {
            addFunctionField()
        }
        removeFunctionButton.addActionListener {
            removeFunctionField()
        }

        // Add first function by default
        addFunctionField()
    }

    private fun addFunctionField() {
        val functionPanel = FunctionPanel()
        functionConfigs.add(functionPanel)
        functionsPanel.add(functionPanel)
        functionsPanel.revalidate()
        functionsPanel.repaint()
    }

    private fun removeFunctionField() {
        if (functionConfigs.isNotEmpty()) {
            val lastPanel = functionConfigs.removeLast()
            functionsPanel.remove(lastPanel)
            functionsPanel.revalidate()
            functionsPanel.repaint()
        }
    }

    override fun createCenterPanel(): JComponent = contentPanel

    override fun getPreferredFocusedComponent(): JComponent = nameTextField

    fun getName(): String = nameTextField.text

    fun getFunctionConfigs(): List<FunctionConfig> = functionConfigs.map { it.getConfig() }

    inner class FunctionPanel : JPanel() {
        private val functionNameField = JTextField(20)
        private val apiPointField = JTextField(20)
        private val httpMethodCombo = JComboBox(HttpMethod.values())
        private val requestJsonArea = JTextArea(5, 30)
        private val responseJsonArea = JTextArea(5, 30)

        init {
            layout = GridBagLayout()
            border = BorderFactory.createTitledBorder("Function Configuration")
            val gbc = GridBagConstraints()
            gbc.insets = Insets(5, 5, 5, 5)
            gbc.fill = GridBagConstraints.HORIZONTAL

            // Row 1: Function Name
            gbc.gridx = 0
            gbc.gridy = 0
            gbc.weightx = 0.0
            add(JLabel("Function Name:"), gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            add(functionNameField, gbc)

            // Row 2: API Point
            gbc.gridx = 0
            gbc.gridy = 1
            gbc.weightx = 0.0
            add(JLabel("API Endpoint:"), gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            apiPointField.text = "/"
            add(apiPointField, gbc)

            // Row 3: HTTP Method
            gbc.gridx = 0
            gbc.gridy = 2
            gbc.weightx = 0.0
            add(JLabel("HTTP Method:"), gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            add(httpMethodCombo, gbc)

            // Row 4: Request JSON
            gbc.gridx = 0
            gbc.gridy = 3
            gbc.weightx = 0.0
            gbc.anchor = GridBagConstraints.NORTHWEST
            add(JLabel("Request JSON:"), gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.BOTH
            gbc.weighty = 1.0
            requestJsonArea.lineWrap = true
            requestJsonArea.wrapStyleWord = true
            val requestScrollPane = JBScrollPane(requestJsonArea)
            requestScrollPane.preferredSize = Dimension(300, 100)
            add(requestScrollPane, gbc)

            // Row 5: Response JSON
            gbc.gridx = 0
            gbc.gridy = 4
            gbc.weightx = 0.0
            gbc.weighty = 0.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            add(JLabel("Response JSON:"), gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.BOTH
            gbc.weighty = 1.0
            responseJsonArea.lineWrap = true
            responseJsonArea.wrapStyleWord = true
            val responseScrollPane = JBScrollPane(responseJsonArea)
            responseScrollPane.preferredSize = Dimension(300, 100)
            add(responseScrollPane, gbc)

            maximumSize = Dimension(Integer.MAX_VALUE, preferredSize.height)
        }

        fun getConfig(): FunctionConfig {
            return FunctionConfig(
                name = functionNameField.text,
                apiPoint = apiPointField.text,
                httpMethod = httpMethodCombo.selectedItem as HttpMethod,
                requestJson = requestJsonArea.text,
                responseJson = responseJsonArea.text
            )
        }
    }
}
