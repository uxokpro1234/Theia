package me.uxokpro1234.theia;

import me.uxokpro1234.theia.gui.GUI;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        if (args.length > 2) {
            System.out.println("java -jar theia.jar [file] [exclusions]");
            return;
        }

        switch (args.length) {
            case 0:
                // enable anti-aliasing
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");

                // Open GUI
                GUI.getInstance().open();
                break;

            case 1:
                Theia.getInstance().run(new File(args[0]), Arrays.asList());
                break;

            case 2:
                List<String> exclusions = Arrays.asList(args[1].split(","));
                Theia.getInstance().run(new File(args[0]), exclusions);
                break;
        }

        log(
                "___________.__           .__        \n" +
                        "\\__    ___/|  |__   ____ |__|____   \n" +
                        "  |    |   |  |  \\_/ __ \\|  \\__  \\  \n" +
                        "  |    |   |   Y  \\  ___/|  |/ __ \\_\n" +
                        "  |____|   |___|  /\\___  >__(____  /\n" +
                        "                \\/     \\/        \\/ \n"
        );
        log("Theia v0.2");
        log("Created by Tigermouthbear");
        log("With contributions from");
        log("GiantNuker, Crystalinqq, Dominika and uxokpro1234\n");
        log(Theia.getInstance().getLog());
    }

    private static void log(String text) {
        System.out.println(text);
    }
}

