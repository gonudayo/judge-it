package com.gonudayo.judgeit.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.components.Service;

@State(
        name = "com.gonudayo.judgeit.settings.PythonRunnerSettings",
        storages = @Storage("PythonRunnerSettings.xml")
)
@Service(Service.Level.APP)
public class PythonRunnerSettings implements PersistentStateComponent<PythonRunnerSettings.State> {
    public static class State {
        private static final String USER_HOME = System.getProperty("user.home");
        public String folderPath =  USER_HOME + "/Downloads/SWEA-samples/"; // 기본 폴더 경로
    }

    private State state = new State();

    @Nullable
    @Override
    public State getState() {
        if (state == null) {
            state = new State(); // 기본값 유지
        }
        if (state.folderPath == null || state.folderPath.isEmpty()) {
            state.folderPath = "C:/Users/SSAFY/Downloads/SWEA-samples/"; // 기본값 설정
        }
        return state;
    }

    @Override
    public void loadState(@NotNull State newState) {
        if (newState != null) {
            this.state = newState;
            System.out.println("Loaded folderPath: " + newState.folderPath); // 디버깅용 출력
        }
    }

    public static PythonRunnerSettings getInstance() {
        return com.intellij.openapi.application.ApplicationManager.getApplication()
                .getService(PythonRunnerSettings.class);
    }
}
