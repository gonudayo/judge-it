package com.gonudayo.judgeit.settings;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import javax.swing.*;
import java.awt.*;

public class PythonRunnerSettingsComponent {
    private final JPanel panel;
    private final TextFieldWithBrowseButton inputPathField;
    private final TextFieldWithBrowseButton outputPathField;

    public PythonRunnerSettingsComponent() {
        panel = new JPanel(new GridLayout(3, 2));

        panel.add(new JLabel("Input File Path:"));
        inputPathField = new TextFieldWithBrowseButton();
        inputPathField.addBrowseFolderListener("Select Input File", "", null,
                com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.createSingleFileDescriptor());
        panel.add(inputPathField);

        panel.add(new JLabel("Output File Path:"));
        outputPathField = new TextFieldWithBrowseButton();
        outputPathField.addBrowseFolderListener("Select Output File", "", null,
                com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.createSingleFileDescriptor());
        panel.add(outputPathField);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getInputFilePath() {
        return inputPathField.getText();
    }

    public void setInputFilePath(String path) {
        inputPathField.setText(path);
    }

    public String getOutputFilePath() {
        return outputPathField.getText();
    }

    public void setOutputFilePath(String path) {
        outputPathField.setText(path);
    }
}
