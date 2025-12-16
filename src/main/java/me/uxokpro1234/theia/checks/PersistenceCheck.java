package me.uxokpro1234.theia.checks;

import me.uxokpro1234.theia.Possible;
import me.uxokpro1234.theia.Program;
import me.uxokpro1234.theia.Theia;
import org.objectweb.asm.tree.*;

/**
 * @author: uxokpro1234
 * 27.11.25
 */
public class PersistenceCheck extends AbstractCheck {

    public PersistenceCheck() {
        super("PersistenceCheck", "Detects system persistence mechanisms");
    }

    @Override
    public void run(Program program) {
        for (ClassNode cn : program.getClassNodes().values()) {
            if (Theia.getInstance().isExcluded(cn.name)) continue;

            int score = 0;

            for (MethodNode mn : cn.methods) {
                for (AbstractInsnNode insn : mn.instructions) {

                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode m = (MethodInsnNode) insn;

                        // Command execution
                        if (m.owner.equals("java/lang/Runtime") && m.name.equals("exec")) {
                            score += 30;
                        }

                        // Preferences (used for persistence)
                        if (m.owner.equals("java/util/prefs/Preferences")) {
                            score += 20;
                        }

                        // File writes
                        if (m.owner.equals("java/io/FileWriter")
                                || m.owner.equals("java/nio/file/Files")) {
                            score += 10;
                        }
                    }

                    if (insn instanceof LdcInsnNode) {
                        Object cst = ((LdcInsnNode) insn).cst;
                        if (cst instanceof String) {
                            String s = ((String) cst).toLowerCase();

                            if (s.contains("startup")
                                    || s.contains("autorun")
                                    || s.contains("registry")
                                    || s.contains("schtasks")
                                    || s.contains("crontab")) {
                                score += 25;
                            }
                        }
                    }
                }
            }

            if (score >= 50) {
                getPossibles().add(new Possible(
                        Possible.Severity.ALERT,
                        "Persistence mechanisms detected (score=" + score + ")",
                        cn.name
                ));
            } else if (score >= 30) {
                getPossibles().add(new Possible(
                        Possible.Severity.WARN,
                        "Possible persistence behavior (score=" + score + ")",
                        cn.name
                ));
            }
        }
    }
}

