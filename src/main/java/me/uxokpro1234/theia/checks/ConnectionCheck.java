package me.uxokpro1234.theia.checks;

import me.uxokpro1234.theia.Theia;
import me.uxokpro1234.theia.Possible;
import me.uxokpro1234.theia.Program;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.Arrays;
import java.util.Map;

/**
 * Converted from Kotlin to Java
 * Original author: Tigermouthbear
 * Updated by GiantNuker
 */
public final class ConnectionCheck extends AbstractCheck {
    private static final URLCheck urlCheck = new URLCheck();
    private static final Theia theia = Theia.getInstance();

    private static final String[] TYPES = {
            "java/net/HttpURLConnection",
            "java/net/HttpsURLConnection",
            "org/apache/http/impl/client/CloseableHttpClient",
            "okhttp3/Request",
            "java/net/Socket",
            "java/net/InetSocketAddress",
            "org/apache/http/impl/client/HttpClientBuilder",
            "org/apache/http/client/methods/HttpPost"
    };

    private static final String[] METHODS = {
            "java/net/URL:openConnection:()Ljava/net/URLConnection;",
            "java/net/URL:openStream:()Ljava/io/InputStream;"
    };

    // private constructor for singleton
    public ConnectionCheck() {
        super("WebConnectionCheck", "Any outgoing connection");
    }


    @Override
    public void run(Program program) {
        for (Map.Entry<String, ClassNode> entry : program.getClassNodes().entrySet()) {
            String className = entry.getKey();
            ClassNode cn = entry.getValue();

            if (theia.isExcluded(className)) continue;

            for (MethodNode mnObj : cn.methods) {
                // Assuming mnObj is a MethodNode
                MethodNode mn = mnObj;

                for (Object insnObj : mn.instructions) {
                    if (insnObj instanceof TypeInsnNode) {
                        TypeInsnNode insn = (TypeInsnNode) insnObj;
                        if (Arrays.asList(TYPES).contains(insn.desc)) {
                            getPossibles().add(new Possible(Possible.Severity.WARN, "Found connection", className));
                            urlCheck.methods.put(mn, className);
                        }
                    } else if (insnObj instanceof MethodInsnNode) {
                        MethodInsnNode insn = (MethodInsnNode) insnObj;
                        if (Arrays.asList(METHODS).contains(format(insn))) {
                            getPossibles().add(new Possible(Possible.Severity.WARN, "Found connection", className));
                            urlCheck.methods.put(mn, className);
                        }
                    }
                }
            }
        }
    }

    // Stub format method, needs actual implementation from your Kotlin code
         public String format(MethodInsnNode insn) {
            return insn.owner + ":" + insn.name + ":" + insn.desc;
        }
}

