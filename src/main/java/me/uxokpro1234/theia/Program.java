package me.uxokpro1234.theia;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Converted from Kotlin
 * Author: Tigermouthbear
 * Date: 4/10/20
 */

public class Program {

    private final Map<String, byte[]> files = new HashMap<>();
    private final Map<String, ClassNode> classNodes = new HashMap<>();

    public Program(File file) {
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                byte[] bytes = null; // initialize to avoid compilation error

                try (InputStream input = jar.getInputStream(entry)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[256];
                    int n;
                    while ((n = input.read(buf)) != -1) {
                        baos.write(buf, 0, n);
                    }
                    bytes = baos.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bytes == null) continue; // skip if reading failed

                if (!entry.getName().endsWith(".class")) {
                    files.put(entry.getName(), bytes);
                } else {
                    ClassNode c = new ClassNode();
                    try {
                        ClassReader reader = new ClassReader(bytes);
                        reader.accept(c, ClassReader.EXPAND_FRAMES);
                        classNodes.put(c.name, c);
                    } catch (Exception ignored) {
                        // Ignore classes that can't be read
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, byte[]> getFiles() {
        return files;
    }

    public Map<String, ClassNode> getClassNodes() {
        return classNodes;
    }
}
