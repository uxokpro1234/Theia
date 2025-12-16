package me.uxokpro1234.theia.checks;

import me.uxokpro1234.theia.Theia;
import me.uxokpro1234.theia.Possible;
import me.uxokpro1234.theia.Program;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Converted from Kotlin to Java
 * Original author: Tigermouthbear
 * 4/13/20
 */
public class CommandCheck extends AbstractCheck {
    Theia theia = Theia.getInstance();
    private static final String[] METHODS = new String[]{
            "java/lang/Runtime:exec:(Ljava/lang/String;)Ljava/lang/Process;",
            "java/lang/ProcessBuilder:command:([Ljava/lang/String;)Ljava/lang/ProcessBuilder;"
    };

    public CommandCheck() {
        super("CommandCheck", "Executes a system command");
    }

    @Override
    public void run(Program program) {
        // Convert METHODS array to a Set for faster lookup
        Set<String> methodSet = new HashSet<>(Arrays.asList(METHODS));

        for (ClassNode cn : program.getClassNodes().values()) {
            if (theia.isExcluded(cn.name)) continue;

            for (MethodNode mn : cn.methods) {
                // Use ASM's instruction iterator
                for (AbstractInsnNode insn = mn.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof MethodInsnNode) {
                        String formatted = format((MethodInsnNode) insn);
                        if (methodSet.contains(formatted)) {
                            getPossibles().add(new Possible(
                                    Possible.Severity.ALERT,
                                    "Shell command executed",
                                    cn.name
                            ));
                        }
                    }
                }
            }
        }
    }

}

