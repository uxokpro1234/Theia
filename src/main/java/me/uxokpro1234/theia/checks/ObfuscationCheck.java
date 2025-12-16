package me.uxokpro1234.theia.checks;

import me.uxokpro1234.theia.Program;
import me.uxokpro1234.theia.Theia;
import me.uxokpro1234.theia.Possible;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Map;

/**
 * @author: uxokpro1234
 * 10.12.25
 */
public class ObfuscationCheck extends AbstractCheck {

    public ObfuscationCheck() {
        super("ObfuscationCheck", "Detects heavy bytecode obfuscation");
    }

    @Override
    public void run(Program program) {
        for (Map.Entry<String, ClassNode> entry : program.getClassNodes().entrySet()) {
            String className = entry.getKey();
            ClassNode cn = entry.getValue();

            if (Theia.getInstance().isExcluded(className)) continue;

            int classScore = 0;

            for (MethodNode mn : cn.methods) {
                classScore += analyzeMethod(mn);
            }

            if (classScore >= 70) {
                getPossibles().add(new Possible(
                        Possible.Severity.ALERT,
                        "Heavily obfuscated class (score=" + classScore + ")",
                        className
                ));
            } else if (classScore >= 40) {
                getPossibles().add(new Possible(
                        Possible.Severity.WARN,
                        "Suspicious obfuscation (score=" + classScore + ")",
                        className
                ));
            }
        }
    }
    private int analyzeMethod(MethodNode mn) {
        int score = 0;
        int jumpCount = 0;

        int insnCount = mn.instructions.size();
        if (insnCount > 2000) score += 30;
        if (insnCount > 5000) score += 20;

        if (mn.maxLocals > 30) score += 10;
        if (mn.maxLocals > 60) score += 20;

        for (AbstractInsnNode insn : mn.instructions) {
            int opcode = insn.getOpcode();

            if (opcode >= Opcodes.IFEQ && opcode <= Opcodes.GOTO) {
                jumpCount++;
            }

            if (insn instanceof TableSwitchInsnNode || insn instanceof LookupSwitchInsnNode) {
                score += 20;
            }

            if (isXorChar(insn)) {
                score += 25;
            }

            if (isStringBuilderCharAppend(insn)) {
                score += 15;
            }
        }

        if (jumpCount > 50) score += 15;
        if (jumpCount > 100) score += 25;

        return score;
    }
    private boolean isXorChar(AbstractInsnNode insn) {
        return insn.getOpcode() == Opcodes.IXOR
                || insn.getOpcode() == Opcodes.LXOR;
    }

    private boolean isStringBuilderCharAppend(AbstractInsnNode insn) {
        if (!(insn instanceof MethodInsnNode)) return false;

        MethodInsnNode m = (MethodInsnNode) insn;
        return m.owner.equals("java/lang/StringBuilder")
                && m.name.equals("append")
                && m.desc.equals("(C)Ljava/lang/StringBuilder;");
    }
}
