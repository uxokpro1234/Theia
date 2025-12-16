package me.uxokpro1234.theia.checks;

import me.uxokpro1234.theia.Possible;
import me.uxokpro1234.theia.Program;
import me.uxokpro1234.theia.Theia;
import org.objectweb.asm.tree.*;

/**
 * @author: uxokpro1234
 * 10.12.25
 */
public class RuntimeClassInjectionCheck extends AbstractCheck {

    public RuntimeClassInjectionCheck() {
        super("RuntimeClassInjection", "Detects runtime class definition");
    }

    @Override
    public void run(Program program) {
        for (ClassNode cn : program.getClassNodes().values()) {
            if (Theia.getInstance().isExcluded(cn.name)) continue;

            boolean foundDefineClass = false;
            boolean foundBase64 = false;

            for (MethodNode mn : cn.methods) {
                for (AbstractInsnNode insn : mn.instructions) {

                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode m = (MethodInsnNode) insn;

                        // Runtime class definition
                        if (
                                (m.owner.equals("java/lang/ClassLoader") && m.name.equals("defineClass")) ||
                                        (m.owner.equals("sun/misc/Unsafe") && m.name.equals("defineClass")) ||
                                        (m.owner.contains("MethodHandles$Lookup") && m.name.equals("defineClass"))
                        ) {
                            foundDefineClass = true;
                        }

                        // Base64 decoding
                        if (m.owner.contains("Base64") || m.owner.contains("BASE64Decoder")) {
                            foundBase64 = true;
                        }
                    }
                }
            }

            if (foundDefineClass) {
                Possible.Severity sev = foundBase64
                        ? Possible.Severity.ALERT
                        : Possible.Severity.WARN;

                getPossibles().add(new Possible(
                        sev,
                        foundBase64
                                ? "Runtime class injection with Base64 payload"
                                : "Runtime class injection detected",
                        cn.name
                ));
            }
        }
    }
}

