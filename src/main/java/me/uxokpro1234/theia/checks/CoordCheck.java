package me.uxokpro1234.theia.checks;

import me.uxokpro1234.theia.Theia;
import me.uxokpro1234.theia.Possible;
import me.uxokpro1234.theia.Program;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * @author Crystallinqq
 * 4/14/20
 *
 * Updated by dominikaaaa on 26/05/
 * Updated by GiantNuker 6/10/2020
 */

import org.objectweb.asm.tree.MethodNode;
import java.util.Arrays;

/**
 * Converted from Kotlin to Java
 * Original author: Crystallinqq
 * Updated by dominikaaaa and GiantNuker
 */
public final class CoordCheck extends AbstractCheck {

    private static final CoordCheck INSTANCE = new CoordCheck();

    private static final String[] COORD_NAMES = {
            "field_148990_b", // 1.8.9 posX
            "field_70165_t",  // 1.8.9 posX
            "field_75646_b",  // 1.8.9 posX
            "field_75656_e",  // 1.8.9 posX
            "field_148988_d", // 1.8.9 posZ
            "field_70161_v",  // 1.8.9 posZ
            "field_75644_d",  // 1.8.9 posZ
            "field_75654_g",  // 1.8.9 posZ
            "field_70165_t",  // 1.12.2 posX
            "field_70163_u",  // 1.12.2 posY (xD)
            "field_70161_v",  // 1.12.2 posZ
            "field_148990_b", // 1.13.2 posX
            "field_148988_d", // 1.13.2 posZ
            "field_148990_b", // 1.14.4 posX
            "field_148988_d", // 1.14.4 posZ
            "field_148990_b", // 1.15 posX
            "field_148988_d"  // 1.15 posZ
    };

    public CoordCheck() {
        super("CoordCheck", "A coordinate was referenced. Most clients use this");
    }

    public static CoordCheck getInstance() {
        return INSTANCE;
    }

    @Override
    public void run(Program program) {
        for (Object cnObj : program.getClassNodes().values()) {
            ClassNode cn = (ClassNode) cnObj;
            if (Theia.getInstance().isExcluded(cn.name)) continue;

            for (MethodNode mn : cn.methods) {
                for (Object insnObj : mn.instructions) {
                    if (insnObj instanceof FieldInsnNode) {
                        FieldInsnNode insn = (FieldInsnNode) insnObj;
                        if (Arrays.asList(COORD_NAMES).contains(insn.name)) {
                            getPossibles().add(new Possible(
                                    Possible.Severity.WARN,
                                    "X or Z coordinates grabbed",
                                    cn.name
                            ));
                        }
                    }
                }
            }
        }
    }
}

