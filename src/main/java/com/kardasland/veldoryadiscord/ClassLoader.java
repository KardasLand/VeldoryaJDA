package com.kardasland.veldoryadiscord;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;


public class ClassLoader {
    boolean isJava11;

    public ClassLoader(){
        String javaFull = System.getProperty("java.version");
        if (javaFull.startsWith("1.8")){
            isJava11 = false;
        }else {
            int sys_major_version = Integer.parseInt(javaFull.substring(0, 2));
            isJava11 = (sys_major_version >= 11);
            //System.out.println("System Java Major: " + sys_major_version);
        }
        if (isJava11){
            VeldoryaJDA.instance.getLogger().info("Plugin is compiled with Java 8, added temporary fix for dependency injection. Plugin is still functional, please ignore if warning comes.");
        }
    }
    /**
     * Adds external dependencies to project.
     * @param file library to add
     */
    public void addDependency(File file) throws IllegalAccessException {
        try {
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            //URLClassLoader sysLoader = new URLClassLoader(new URL[0]);
            java.lang.ClassLoader sysLoader = java.lang.ClassLoader.getSystemClassLoader();
            //addURL.setAccessible(true);
            if (isJava11){
                Method method = sysLoader.getClass()
                        .getDeclaredMethod("appendToClassPathForInstrumentation", String.class);

                setAccessible(method, true);
                //System.out.println("Path: " + file.getPath());
                method.invoke(sysLoader, file.getPath());
                //addURL.invoke(sysLoader, file.toURI().toURL());
            }else {
                setAccessible(addURL, true);
                addURL.invoke(URLClassLoader.getSystemClassLoader(), file.toURI().toURL());
            }
        }catch (Exception exception){
            throw new IllegalAccessException();
        }
    }

    static void setAccessible(AccessibleObject ao, boolean accessible) {
        if (System.getSecurityManager() == null)
            ao.setAccessible(accessible);
        else {
            AccessController.doPrivileged((PrivilegedAction) () -> {
                ao.setAccessible(accessible);
                return null;
            });
        }
    }
}
