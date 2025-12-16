package me.uxokpro1234.theia.checks;

import me.uxokpro1234.theia.Theia;
import me.uxokpro1234.theia.Possible;
import me.uxokpro1234.theia.Program;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.Arrays;

/**
 * Converted from Kotlin to Java
 * Original author: Tigermouthbear
 * Updated by GiantNuker
 */
public final class FileDeletionCheck extends AbstractCheck {
    public Theia theia = Theia.getInstance();
    private static final FileDeletionCheck INSTANCE = new FileDeletionCheck();

    private static final String[] METHODS = {
            "java/io/File:delete:()Z",
            "java/nio/file/Files:deleteIfExists:(Ljava/nio/file/Path;)Z"
    };

    public FileDeletionCheck() {
        super("FileDeletionCheck", "File deleted");
    }

    public static FileDeletionCheck getInstance() {
        return INSTANCE;
    }

    @Override
    public void run(final Program program) {
        for (Object cnObj : program.getClassNodes().values()) {
            ClassNode cn = (ClassNode) cnObj;

            if (theia.isExcluded(cn.name)) continue;

            for (MethodNode mn : cn.methods) {
                for (AbstractInsnNode insn : mn.instructions) {
                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;
                        if (Arrays.asList(METHODS).contains(format(methodInsn))) {
                            getPossibles().add(new Possible(
                                    Possible.Severity.WARN,
                                    "Found file deletion",
                                    cn.name
                            ));
                        }
                    }
                }
            }
        }
    }



    // Helper method to format MethodInsnNode like Kotlin version
    public String format(MethodInsnNode insn) {
        return insn.owner + ":" + insn.name + ":" + insn.desc;
    }
}

