package com.gonudayo.sweasamplejudge.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "com.gonudayo.sweasamplejudge.settings.PythonRunnerSettings",
        storages = @Storage("PythonRunnerSettings.xml")
)
public class PythonRunnerSettings implements PersistentStateComponent<PythonRunnerSettings.State> {

    public static class State {
        public String inputFilePath = "C:/Users/SSAFY/Downloads/SWEA-samples/input.txt";
        public String outputFilePath = "C:/Users/SSAFY/Downloads/SWEA-samples/output.txt";
    }

    private State state = new State();

    @Nullable
    @Override
    public State getState() {
        if (state == null) {
            state = new State();
        }
        return state;
    }

    @Override
    public void loadState(@NotNull State newState) {
        if (newState != null) {
            this.state = newState;
        }
    }

    public static PythonRunnerSettings getInstance() {
        PythonRunnerSettings instance = com.intellij.openapi.application.ApplicationManager.getApplication()
                .getService(PythonRunnerSettings.class);
        return instance != null ? instance : new PythonRunnerSettings();
    }
}
