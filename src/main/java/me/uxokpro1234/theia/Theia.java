package me.uxokpro1234.theia;

import me.uxokpro1234.theia.checks.*;
import me.uxokpro1234.theia.gui.GUI;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Converted from Kotlin Theia object
 * @author: Tigermouthbear
 * Updated by GiantNuker
 */

public class Theia {

    private static final Theia INSTANCE = new Theia();
    private static final ConnectionCheck connectionCheck = new ConnectionCheck();
    private static final URLCheck urlCheck = new URLCheck();
    private static final CommandCheck commandCheck = new CommandCheck();
    private static final FileDeletionCheck fileDeletionCheck = new FileDeletionCheck();

    private static final CoordCheck coordCheck = new CoordCheck();
    private static final ClassloadCheck classloadCheck = new ClassloadCheck();
    private static final ObfuscationCheck obfuscationCheck = new ObfuscationCheck();
    private static final RuntimeClassInjectionCheck runtimeClassInjectionCheck = new RuntimeClassInjectionCheck();
    private static final PersistenceCheck persistenceCheck = new PersistenceCheck();

    private static final Map<String, List<AbstractCheck>> overviewMap = new HashMap<>();
    public static final AbstractCheck[] checks = new AbstractCheck[]{
            connectionCheck,
            urlCheck,
            commandCheck,
            fileDeletionCheck,
            coordCheck,
            classloadCheck,
            obfuscationCheck,
            runtimeClassInjectionCheck,
            persistenceCheck
    };

    private static List<String> exclusions;
    public static String log = "";

    public static void run(File file, List<String> exclusionsList) {
        long startTime = System.currentTimeMillis();
        exclusions = exclusionsList;

        // Clear previous possibles
        for (AbstractCheck check : checks) {
            check.getPossibles().clear();
        }
        overviewMap.clear();

        Program program = new Program(file);
        StringBuilder out = new StringBuilder();

        final int[] completionIndex = {-1};
        final String[] checkName = {""};
        final long[] mStartTime = {0L};
        AtomicBoolean active = new AtomicBoolean(false);

        Thread logThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (active.get()) {
                    if (completionIndex[0] == -1) {
                        log("\rProcessing: " + file.getName());
                    } else if (completionIndex[0] < checks.length) {
                        log(String.format("\r%d/%d - %s [%dms]",
                                completionIndex[0] + 1, checks.length, checkName[0],
                                System.currentTimeMillis() - mStartTime[0]));
                    }
                }

                if (completionIndex[0] == checks.length) break;
            }
        });

        logThread.start();

        log("Processing: " + file.getName());
        log("");

        for (AbstractCheck check : checks) {
            completionIndex[0] = Arrays.asList(checks).indexOf(check);
            checkName[0] = check.getName();
            log(String.format("\r%d/%d - %s [0ms]", completionIndex[0] + 1, checks.length, checkName[0]));
            mStartTime[0] = System.currentTimeMillis();
            active.set(true);
            check.run(program);
            active.set(false);
            log(String.format("\r%d/%d - %s [%dms]", completionIndex[0] + 1, checks.length, checkName[0],
                    System.currentTimeMillis() - mStartTime[0]));
            log("");
        }

        completionIndex[0] = checks.length;
        log("Done in " + (System.currentTimeMillis() - startTime) + "ms");

        // Print checks
        for (AbstractCheck check : checks) {
            if (!check.getPossibles().isEmpty()) {
                out.append(check.getName()).append(": {\n");
                for (Possible possible : check.getPossibles()) {
                    out.append("\t").append(possible.getSeverity().name())
                            .append(": ").append(possible.getDescription())
                            .append(" in ").append(possible.getClazz()).append("\n");
                }
                out.append("}\n");
            } else {
                out.append(check.getName()).append(": CLEAR\n");
            }
        }

        // Generate overview map
        for (AbstractCheck check : checks) {
            for (Possible possible : check.getPossibles()) {
                overviewMap.computeIfAbsent(possible.getClazz(), k -> new ArrayList<>());
                if (!overviewMap.get(possible.getClazz()).contains(check)) {
                    overviewMap.get(possible.getClazz()).add(check);
                }
            }
        }

        // Print overview map formatted
        if (!overviewMap.isEmpty()) {
            out.append("\nOverview:\n");
            for (String clazz : overviewMap.keySet()) {
                StringBuilder o = new StringBuilder("\t" + clazz + ": ");
                for (AbstractCheck check : overviewMap.get(clazz)) {
                    o.append(check.getName()).append(" ");
                }
                out.append(o).append("\n");
            }
        } else {
            out.append("Program all clear!\n");
        }

        out.append("\nTheia completed in ").append(System.currentTimeMillis() - startTime).append(" milliseconds");
        log = out.toString();
    }

    public static boolean isExcluded(String className) {
        for (String dependency : exclusions) {
            if (className.startsWith(dependency)) return true;
        }
        return false;
    }

    public static Theia getInstance() {
        return INSTANCE;
    }

    public AbstractCheck[] getChecks() {
        return checks;
    }

    public String getLog() {
        return log;
    }
    public static void log(String text) {
        GUI gui = GUI.getInstance();
        if (text.startsWith("\r")) System.out.print(text);
        else System.out.println(text);
        if (gui.isVisible()) gui.log(text);
    }
}