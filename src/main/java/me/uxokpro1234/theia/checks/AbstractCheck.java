package me.uxokpro1234.theia.checks;

import me.uxokpro1234.theia.Possible;
import me.uxokpro1234.theia.Program;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigermouthbear
 * 4/10/20
 *
 * Updated by GiantNuker 6/10/2020
 */
public abstract class AbstractCheck {

    private final String name;
    private final String desc;
    private final List<Possible> possibles = new ArrayList<>();

    public AbstractCheck(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public abstract void run(Program program);

    protected String format(MethodInsnNode insn) {
        return insn.owner + ":" + insn.name + ":" + insn.desc;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public List<Possible> getPossibles() {
        return possibles;
    }
}

