package es.e1sordo.thesis.wtiat.agent.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ConnectorLoader {

    final static Logger LOGGER = LoggerFactory.getLogger(ConnectorLoader.class);

    private static final String MAIN_CLASS_NAME = "ElectronicDeviceImpl";

    public static Class loadJarAndGetMainClass(File connectorJarFile) {
        Class mainClass = null;
        try {
            JarFile jarFile = new JarFile(connectorJarFile);
            Enumeration<JarEntry> e = jarFile.entries();

            URL[] urls = new URL[]{connectorJarFile.toURI().toURL()};

            URLClassLoader cl = URLClassLoader.newInstance(urls);

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                // -6 because of .class
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');

                if (className.contains(MAIN_CLASS_NAME)) {
                    mainClass = cl.loadClass(className);
                    LOGGER.info("Main Connector {} was found and loaded", mainClass);
                } else {
                    cl.loadClass(className);
                }
            }
        } catch (ClassNotFoundException | IOException ex) {
            LOGGER.error("An error occurred while loading classes", ex);
        }

        return mainClass;
    }
}
