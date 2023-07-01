package ua.romenkost.varreplacer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class VarReplacer extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }
        PsiElementFactory factory = PsiElementFactory.SERVICE.getInstance(psiFile.getProject());
        List<PsiTypeElement> allPsiVarTypeElements = findAllPsiVarTypeElements(psiFile.getChildren());
        if (allPsiVarTypeElements.isEmpty()) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(psiFile.getProject(), () -> {
            allPsiVarTypeElements.forEach(psiVarTypeElement -> {
                        PsiType psiType = psiVarTypeElement.getType();
                        PsiTypeElement newPsiTypeElement = factory.createTypeElement(psiType);
                        psiVarTypeElement.replace(newPsiTypeElement);
            });
            JavaCodeStyleManager.getInstance(psiFile.getProject())
                    .shortenClassReferences(psiFile);
        }));
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }

    private List<PsiTypeElement> findAllPsiVarTypeElements(PsiElement[] psiElements) {
        List<PsiTypeElement> result = new ArrayList<>();
        Queue<PsiElement> psiElementQueue = Arrays.stream(psiElements)
                .collect(Collectors.toCollection(LinkedList::new));
        while (!psiElementQueue.isEmpty()) {
            PsiElement psiElement = psiElementQueue.poll();
            if(psiElement instanceof PsiTypeElement && psiElement.getFirstChild() instanceof PsiKeyword) {
                result.add((PsiTypeElement) psiElement);
            } else {
                psiElementQueue.addAll(Arrays.asList(psiElement.getChildren()));
            }
        }
        return result;
    }
}