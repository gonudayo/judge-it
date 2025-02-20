package com.gonudayo.judgeit.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.*;
import java.awt.*;

public class PythonRunnerSettingsComponent {
    private final JPanel panel;
    private final TextFieldWithBrowseButton folderPathField;

    public PythonRunnerSettingsComponent() {
        panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 한 줄 정렬
        panel.add(new JLabel("File:"));

        // TextFieldWithBrowseButton을 직접 사용하여 파일 탐색기 연결
        folderPathField = new TextFieldWithBrowseButton();
        folderPathField.setPreferredSize(new Dimension(300, 25)); // 길이 조정
        folderPathField.addBrowseFolderListener(
                "Select Folder", "", null,
                FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        panel.add(folderPathField);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getFolderPath() {
        return folderPathField.getText().replace("\\", "/"); // 백슬래시 → 슬래시 변환
    }

    public void setFolderPath(String path) {
        folderPathField.setText(path);
    }
}
