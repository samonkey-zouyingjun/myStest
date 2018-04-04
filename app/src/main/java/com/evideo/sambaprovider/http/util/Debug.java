package com.evideo.sambaprovider.http.util;

import java.io.PrintStream;

public final class Debug {
    public static Debug debug = new Debug();
    public static boolean enabled = false;
    private PrintStream out = System.out;

    public synchronized PrintStream getOut() {
        return this.out;
    }

    public synchronized void setOut(PrintStream out) {
        this.out = out;
    }

    public static Debug getDebug() {
        return debug;
    }

    public static final void on() {
        enabled = true;
    }

    public static final void off() {
        enabled = false;
    }

    public static boolean isOn() {
        return enabled;
    }

    public static final void message(String s) {
        if (enabled) {
            debug.getOut().println("CyberGarage message : " + s);
        }
    }

    public static final void message(String m1, String m2) {
        if (enabled) {
            debug.getOut().println("CyberGarage message : ");
        }
        debug.getOut().println(m1);
        debug.getOut().println(m2);
    }

    public static final void warning(String s) {
        debug.getOut().println("CyberGarage warning : " + s);
    }

    public static final void warning(String m, Exception e) {
        if (e.getMessage() == null) {
            debug.getOut().println("CyberGarage warning : " + m + " START");
            e.printStackTrace(debug.getOut());
            debug.getOut().println("CyberGarage warning : " + m + " END");
            return;
        }
        debug.getOut().println("CyberGarage warning : " + m + " (" + e.getMessage() + ")");
        e.printStackTrace(debug.getOut());
    }

    public static final void warning(Exception e) {
        warning(e.getMessage());
        e.printStackTrace(debug.getOut());
    }
}
