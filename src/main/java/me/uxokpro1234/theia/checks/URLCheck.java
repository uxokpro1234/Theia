package me.uxokpro1234.theia.checks;

import me.uxokpro1234.theia.Possible;
import me.uxokpro1234.theia.Program;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Converted from Kotlin to Java
 * @author Tigermouthbear
 * Updated by GiantNuker
 */
public final class URLCheck extends AbstractCheck {

    private static final URLCheck INSTANCE = new URLCheck();

    Map<MethodNode, String> methods = new HashMap<>();

    public URLCheck() {
        super("URLCheck", "URL created");
    }

    public static URLCheck getInstance() {
        return INSTANCE;
    }

    @Override
    public void run(final Program program) {
        Thread thread = new Thread(() -> {
            for (Map.Entry<MethodNode, String> entry : methods.entrySet()) {
                MethodNode mn = entry.getKey();
                String className = entry.getValue();

                for (org.objectweb.asm.tree.AbstractInsnNode insn : mn.instructions) {
                    if (insn instanceof TypeInsnNode) {
                        TypeInsnNode typeInsn = (TypeInsnNode) insn;
                        if ("java/net/URL".equals(typeInsn.desc)) {
                            getPossibles().add(new Possible(
                                    Possible.Severity.WARN,
                                    "Found URL [" + getURL(typeInsn, program) + "]",
                                    className
                            ));
                        }
                    }
                }
            }
        });

        thread.start();
        long startTime = System.currentTimeMillis();

        while (thread.isAlive()) {
            if (System.currentTimeMillis() - startTime > 20000) {
                thread.stop(); // deprecated, but kept for parity with original
                getPossibles().add(new Possible(
                        Possible.Severity.ALERT,
                        "URL CHECK TIMED OUT",
                        "URL CHECK TIMED OUT"
                ));
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        methods.clear();
    }

    private String getURL(TypeInsnNode target, Program program) {
        StringBuilder out = new StringBuilder();
        boolean looking = false;

        for (Object cnObj : program.getClassNodes().values()) {
            ClassNode cn = (ClassNode) cnObj;
            for (Object mnObj : cn.methods) {
                MethodNode mn = (MethodNode) mnObj;

                for (org.objectweb.asm.tree.AbstractInsnNode insn : mn.instructions) {
                    if (insn == target) looking = true;

                    if (looking) {
                        if (insn instanceof LdcInsnNode) {
                            out.append(((LdcInsnNode) insn).cst.toString()).append(" : ");
                        } else if (insn instanceof MethodInsnNode) {
                            MethodInsnNode methodInsn = (MethodInsnNode) insn;
                            if ("java/net/URL".equals(methodInsn.owner)
                                    && "<init>".equals(methodInsn.name)
                                    && "(Ljava/lang/String;)V".equals(methodInsn.desc)) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (out.length() > 80) return out.substring(0, 80);
        else if (out.length() > 0) return out.toString();

        return "could not parse URL";
    }
    public void methods(MethodNode mn, String className) {
        methods.put(mn, className);
    }
}
