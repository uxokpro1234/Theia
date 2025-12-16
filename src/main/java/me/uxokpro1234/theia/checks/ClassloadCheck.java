package me.uxokpro1234.theia.checks;

import me.uxokpro1234.theia.Theia;
import me.uxokpro1234.theia.Possible;
import me.uxokpro1234.theia.Program;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.List;

/**
 * Combined ClassloadCheck
 * Detects:
 * - File deletion / runtime execution
 * - Dynamically defined classes
 * - Reflection usage (getDeclaredField)
 * - Classes extending ClassLoader
 */

public final class ClassloadCheck extends AbstractCheck {

    public static final ClassloadCheck INSTANCE = new ClassloadCheck();
    private final Theia theia = Theia.getInstance();

    private static final String[] SUSPICIOUS_METHODS = {
            "java/io/File:delete:()Z",
            "java/nio/file/Files:deleteIfExists:(Ljava/nio/file/Path;)Z",
            "java/lang/Runtime:exec:(Ljava/lang/String;)Ljava/lang/Process;"
    };

    public ClassloadCheck() {
        super("ClassloadCheck", "Detects dynamic class loading, reflection, and suspicious file/runtime operations");
    }

    @Override
    public void run(Program program) {
        for (Object cnObj : program.getClassNodes().values()) {
            ClassNode cn = (ClassNode) cnObj;
            if (theia.isExcluded(cn.name)) continue;

            List<MethodNode> methods = cn.methods;
            for (MethodNode mn : methods) {
                for (AbstractInsnNode insn : mn.instructions) {
                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode min = (MethodInsnNode) insn;
                        String formatted = format(min);

                        // File deletion / runtime exec
                        if (Arrays.asList(SUSPICIOUS_METHODS).contains(formatted)) {
                            String message = formatted.contains("Runtime:exec") ?
                                    "Suspicious runtime execution detected" :
                                    "Found file deletion";

                            getPossibles().add(new Possible(
                                    Possible.Severity.WARN,
                                    message,
                                    cn.name
                            ));
                        }

                        // Reflection / dynamic class loading detection
                        switch (min.name) {
                            case "defineClass":
                                getPossibles().add(new Possible(
                                        Possible.Severity.ALERT,
                                        "Dynamically defined class with defineClass",
                                        cn.name
                                ));
                                break;
                            case "getDeclaredField":
                                getPossibles().add(new Possible(
                                        Possible.Severity.WARN,
                                        "Accesses field with reflection (may be dynamic loading)",
                                        cn.name
                                ));
                                break;
                        }

                        // Environment inspection
                        if (min.owner.equals("java/lang/System") &&
                                (min.name.equals("getProperty") || min.name.equals("getenv"))) {
                            getPossibles().add(new Possible(
                                    Possible.Severity.WARN,
                                    "Environment inspection detected",
                                    cn.name
                            ));
                        }
                    }
                }

                // Class extending ClassLoader
                if ("java/lang/ClassLoader".equals(cn.superName)) {
                    getPossibles().add(new Possible(
                            Possible.Severity.ALERT,
                            "Class extends ClassLoader  (Dynamic loading can bypass other checks)",
                            cn.name
                    ));
                }
            }
        }
    }

    // Helper to format MethodInsnNode
    public String format(MethodInsnNode insn) {
        return insn.owner + ":" + insn.name + ":" + insn.desc;
    }
}
