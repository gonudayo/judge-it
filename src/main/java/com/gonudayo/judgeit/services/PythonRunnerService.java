package com.gonudayo.judgeit.services;

import com.gonudayo.judgeit.settings.PythonRunnerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;



public class PythonRunnerService {
    public static void runPythonFile(Project project, VirtualFile file) {
        FileDocumentManager.getInstance().saveAllDocuments();
        String pythonPath = getPythonExecutable(project);
        if (pythonPath == null) {
            showResultDialog("Error", "Python SDK not found!",false);
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

            // 2. `sys.stdin = open("input.txt", "r")` 추가
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
                showResultDialog("Execution Error", output.getStderr(), false);
                return;  // 비교 진행 X
            }

            // 7. 비교 및 결과 출력
            compareOutputs(project, file.getParent().getPath());

        } catch (ExecutionException | IOException e) {
            showResultDialog("Execution Error", "Error: " + e.getMessage(), false);
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

        StringBuilder compareResultMessage = new StringBuilder();
        StringBuilder userOutputMessage = new StringBuilder();
        String userLine, sampleLine;
        int i = 1, success = 1;

        while (true) {
            userLine = userOutput.readLine();
            sampleLine = sampleOutput.readLine();

            if (userLine != null) {
                userOutputMessage.append(userLine).append("\n");
            }

            if (userLine == null && sampleLine == null) break;

            // 각 줄의 끝부분 공백 제거
            userLine = cleanLine(userLine);
            sampleLine = cleanLine(sampleLine);

            if (!userLine.equals(sampleLine)) {
                compareResultMessage.append("Line ").append(i).append(" is different.\n")
                        .append("You----->").append(userLine).append("\n")
                        .append("Sample-->").append(sampleLine).append("\n");
                success = 0;
            }
            i++;
        }

        userOutput.close();
        sampleOutput.close();

        // 최종 결과 메시지 출력
        if (success == 1) {
            showResultDialog("Judge Result", userOutputMessage.toString(), true);
        } else {
            showResultDialog("Judge Result", userOutputMessage.toString() +
                    "DIFF\n" + "--------------------\n" + compareResultMessage.toString(), false);
        }
    }


    // 결과창
    public static void showResultDialog(String title, String showMessage, boolean isSuccess) {
        // JTextPane 생성 (색상 적용 가능)
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(Color.BLACK);  // 배경 검정
        textPane.setForeground(Color.WHITE);  // 기본 글자색 흰색
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 14));

        // 스타일 적용을 위한 StyledDocument 사용
        StyledDocument doc = textPane.getStyledDocument();

        // 기본 스타일 (흰색)
        SimpleAttributeSet defaultStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(defaultStyle, Color.WHITE);

        // 결과 강조 스타일 (녹색/빨간색)
        SimpleAttributeSet resultStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(resultStyle, isSuccess ? Color.GREEN : Color.RED);
        StyleConstants.setBold(resultStyle, true); // 결과 부분만 볼드 처리

        try {
            // 첫 번째 줄 (결과 메시지 - 색상 적용)
            doc.insertString(doc.getLength(), isSuccess ? "Success" : "Failure", resultStyle);
            doc.insertString(doc.getLength(), "\n\n", defaultStyle);

            // 나머지 메시지 (기본 흰색)
            doc.insertString(doc.getLength(), showMessage, defaultStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // 스크롤 추가
        JScrollPane scrollPane = new JScrollPane(textPane);

        // JDialog 생성
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        // 닫기 버튼 생성
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        // 엔터 키로 닫기 활성화
        dialog.getRootPane().setDefaultButton(closeButton);

        // ESC 키로 창 닫기 기능 추가
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, "CLOSE_DIALOG");
        dialog.getRootPane().getActionMap().put("CLOSE_DIALOG", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        // 버튼을 포함할 패널 생성
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        // 기본 다이얼로그 크기 설정
        int maxWidth = 800, maxHeight = 800;  // 최대 크기
        int minWidth = 300, minHeight = 150;  // 최소 크기

        // 버튼 높이
        int buttonPanelHeight = 50;

        // 폰트 메트릭스로 한 줄 높이 계산
        FontMetrics metrics = textPane.getFontMetrics(textPane.getFont());
        int lineHeight = metrics.getHeight() + 3; // 한 줄 높이

        int lineCount = showMessage.split("\n").length + 4; // 결과 + 메시지 줄 수

        // 텍스트 길이에 따라 다이얼로그 크기 조절
        int textWidth = Math.min(maxWidth, Math.max(minWidth, showMessage.length() * 7));
        int textHeight = Math.min(maxHeight, Math.max(minHeight, lineCount * lineHeight));

        // 다이얼로그 크기 설정
        dialog.setSize(textWidth, textHeight + buttonPanelHeight);
        dialog.setLocationRelativeTo(null); // 화면 중앙에 표시

        // 컴포넌트 추가
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 포커스 트래버설 정책을 설정하여 버튼이 기본 포커스가 되도록 강제
        dialog.setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getInitialComponent(Window window) {
                return closeButton;  // 첫 포커스를 closeButton으로 강제
            }

            @Override public Component getComponentAfter(Container focusCycleRoot, Component aComponent) { return closeButton; }
            @Override public Component getComponentBefore(Container focusCycleRoot, Component aComponent) { return closeButton; }
            @Override public Component getDefaultComponent(Container focusCycleRoot) { return closeButton; }
            @Override public Component getLastComponent(Container focusCycleRoot) { return closeButton; }
            @Override public Component getFirstComponent(Container focusCycleRoot) { return closeButton; }
        });

        dialog.setResizable(true);
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