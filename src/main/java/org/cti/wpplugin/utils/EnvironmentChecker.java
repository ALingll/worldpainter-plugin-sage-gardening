package org.cti.wpplugin.utils;

public class EnvironmentChecker {
    public static boolean isRunInJar(){
        return EnvironmentChecker.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath()
                .endsWith(".jar");
    }
}
