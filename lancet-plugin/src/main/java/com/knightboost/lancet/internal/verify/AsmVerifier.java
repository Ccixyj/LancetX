package com.knightboost.lancet.internal.verify;



import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;

/**
 * Created by yangzhiqian on 2019/4/18<br/>
 */
public class AsmVerifier {

    public static void verify(ClassNode classNode) throws AsmVerifyException {
        for (Object methodNode : classNode.methods) {
            verify(classNode.name, (MethodNode) methodNode);
        }
    }

    public static void verify(String owner, MethodNode methodNode) throws AsmVerifyException {
        try {
            new Analyzer(new BasicInterpreter()).analyze(owner, methodNode);
        } catch (AnalyzerException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("verify err:")
                    .append(owner.replaceAll("/","."))
                    .append(".")
                    .append(methodNode.name)
                    .append(" ")
                    .append(methodNode.desc)
                    .append("\n");
            int size = methodNode.instructions.size();
            for (int i = 0; i < size; i++) {
                stringBuilder.append(methodNode.instructions.get(i)).append("\n");
            }
            throw new AsmVerifyException(stringBuilder.toString(), e);
        }
    }
}
