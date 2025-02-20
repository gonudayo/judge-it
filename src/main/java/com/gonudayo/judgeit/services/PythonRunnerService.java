package com.gonudayo.judgeit.services;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.ExecutionException;
import com.gonudayo.judgeit.settings.PythonRunnerSettings;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import javax.swing.*;
import java.awt.*;



public class PythonRunnerService {
    public static void runPythonFile(Project project, VirtualFile file) {
        String pythonPath = getPythonExecutable(project);
        if (pythonPath == null) {
            showResultDialog("Error", "Python SDK not found!");
            return;
        }

        // 원본 Python 파일 경로
        String originalFilePath = file.getPath();
        // 임시 Python 파일 경로 (같은 폴더에 "__temp__.py" 이름으로 저장)
        String tempFilePath = file.getParent().getPath() + "/__temp__.py";
        // 출력 결과 저장 경로
        String userOutputFilePath = file.getParent().getPath() + "/your_output.txt";
        // 지정된 입력 파일 경로
        String sampleInputFilePath = getInputFilePath();

        try {
            // 1. 원본 Python 코드 읽기
            String originalCode = new String(Files.readAllBytes(Paths.get(originalFilePath)), StandardCharsets.UTF_8);

            // 2. `sys.stdin = open("C:/Users/SSAFY/Downloads/SWEA-samples/input.txt", "r")` 추가
            String modifiedCode = "import sys\nsys.stdin = open(\"" + sampleInputFilePath + "\", \"r\")\n\n" + originalCode;

            // 3. 수정된 코드를 임시 파일에 저장
            Files.write(Paths.get(tempFilePath), modifiedCode.getBytes(StandardCharsets.UTF_8));

            // 4. Python 실행 명령어 설정
            GeneralCommandLine commandLine = new GeneralCommandLine(pythonPath, tempFilePath);
            commandLine.setCharset(StandardCharsets.UTF_8);

            // 5. Python 실행
            CapturingProcessHandler processHandler = new CapturingProcessHandler(commandLine);
            ProcessOutput output = processHandler.runProcess();

            // 6. Python 실행 결과를 파일에 저장
            Files.write(Paths.get(userOutputFilePath), output.getStdout().getBytes(StandardCharsets.UTF_8));

            // 실행 중 에러가 발생하면 stderr 표시
            if (!output.getStderr().isEmpty()) {
                showResultDialog("Execution Error", output.getStderr());
                return;  // 비교 진행 X
            }

            // 7. 비교 및 결과 출력
            compareOutputs(project, file.getParent().getPath());

        } catch (ExecutionException | IOException e) {
            showResultDialog("Execution Error", "Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 8. 실행이 끝난 후 임시 파일 삭제
            new File(tempFilePath).delete();
        }
    }

    // 파일 비교 및 결과 출력
    private static void compareOutputs(Project project, String parentPath) throws IOException {
        BufferedReader userOutput = new BufferedReader(new FileReader(parentPath + "/your_output.txt"));
        BufferedReader sampleOutput = new BufferedReader(new FileReader(getOutputFilePath()));

        StringBuilder resultMessage = new StringBuilder();
        String userLine, sampleLine;
        int i = 1, success = 1;

        while (true) {
            userLine = userOutput.readLine();
            sampleLine = sampleOutput.readLine();

            if (userLine == null && sampleLine == null) break;

            // 각 줄의 끝부분 공백 제거
            userLine = cleanLine(userLine);
            sampleLine = cleanLine(sampleLine);

            if (!userLine.equals(sampleLine)) {
                resultMessage.append("Line ").append(i).append(": Different!\n")
                        .append("You    : ").append(userLine).append("\n")
                        .append("Sample : ").append(sampleLine).append("\n");
                success = 0;
            }
            i++;
        }

        userOutput.close();
        sampleOutput.close();

        // 최종 결과 메시지 출력
        if (success == 1) {
            showResultDialog("Comparison Result", "Success!");
        } else {
            showResultDialog("Comparison Result", "Fail!\n\n" + resultMessage.toString());
        }
    }

    // 결과창
    private static void showResultDialog(String title, String showMessage) {
        JTextArea textArea = new JTextArea(showMessage, 50, 100); // 높이 50줄, 너비 100글자
        textArea.setEditable(false); // 편집 불가 설정
        JScrollPane scrollPane = new JScrollPane(textArea); // 스크롤 추가

        // JDialog 생성
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        // 닫기 버튼 생성
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose()); // 버튼 클릭 시 창 닫기

        // 엔터 키로 닫기 활성화
        dialog.getRootPane().setDefaultButton(closeButton);

        // 버튼을 포함할 패널 생성
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        // 컴포넌트 추가
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(800, 800);  // 기본 크기 설정
        dialog.setLocationRelativeTo(null); // 화면 중앙에 표시
        dialog.setResizable(true); // 크기 조정 가능하게 설정
        dialog.setVisible(true);
    }

    // 공백 제거
    private static String cleanLine(String line) {
        return (line == null) ? "" : line.replaceAll("[ \t]+$", "");
    }

    // Python 실행 파일 경로 가져오기
    private static String getPythonExecutable(Project project) {
        Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        return (sdk != null) ? sdk.getHomePath() : "python";
    }

    // 샘플 폴더 경로 가져오기
    private static String getSampleFolderPath() {
        return PythonRunnerSettings.getInstance().getState().folderPath;
    }

    // 인풋 파일 경로 (폴더 내부의 input.txt로 설정)
    private static String getInputFilePath() {
        return getSampleFolderPath() + "/input.txt";
    }

    // 아웃풋 파일 경로 (폴더 내부의 output.txt로 설정)
    private static String getOutputFilePath() {
        return getSampleFolderPath() + "/output.txt";
    }

}
