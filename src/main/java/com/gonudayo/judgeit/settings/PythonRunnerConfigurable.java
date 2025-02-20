package com.gonudayo.judgeit.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PythonRunnerConfigurable implements Configurable {
    private PythonRunnerSettingsComponent settingsComponent;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Python Runner Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsComponent = new PythonRunnerSettingsComponent();

        // 기존 설정값을 불러오기
        PythonRunnerSettings.State state = PythonRunnerSettings.getInstance().getState();
        settingsComponent.setFolderPath(state.folderPath);

        return settingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        PythonRunnerSettings.State state = PythonRunnerSettings.getInstance().getState();
        return !settingsComponent.getFolderPath().equals(state.folderPath);
    }

    @Override
    public void apply() {
        PythonRunnerSettings.State state = PythonRunnerSettings.getInstance().getState();
        state.folderPath = settingsComponent.getFolderPath();
    }

    @Override
    public void reset() {
        PythonRunnerSettings.State state = PythonRunnerSettings.getInstance().getState();
        settingsComponent.setFolderPath(state.folderPath);
    }

    @Override
    public void disposeUIResources() {
        settingsComponent = null;
    }
}
