package com.gonudayo.sweasamplejudge.actions;

import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.*;
import javax.swing.*;


public class JudgeAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Messages.showMessageDialog(
                "Hello, This is SWEA Sample Judge",
                "Test",
                Messages.getInformationIcon()
        );
    }
}
