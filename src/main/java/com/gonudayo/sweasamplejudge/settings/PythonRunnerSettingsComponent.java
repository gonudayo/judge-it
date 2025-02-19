package com.gonudayo.sweasamplejudge.settings;

import javax.swing.*;
import java.awt.*;

public class PythonRunnerSettingsComponent {
    private final JPanel panel;
    private final JTextField inputFilePathField;
    private final JTextField outputFilePathField;

    public PythonRunnerSettingsComponent() {
        panel = new JPanel(new GridLayout(3, 2));

        panel.add(new JLabel("Input File Path:"));
        inputFilePathField = new JTextField();
        panel.add(inputFilePathField);

        panel.add(new JLabel("Output File Path:"));
        outputFilePathField = new JTextField();
        panel.add(outputFilePathField);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getInputFilePath() {
        return inputFilePathField.getText();
    }

    public void setInputFilePath(String path) {
        inputFilePathField.setText(path);
    }

    public String getOutputFilePath() {
        return outputFilePathField.getText();
    }

    public void setOutputFilePath(String path) {
        outputFilePathField.setText(path);
    }
}
