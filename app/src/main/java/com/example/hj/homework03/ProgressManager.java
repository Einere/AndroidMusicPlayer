package com.example.hj.homework03;

class ProgressManager {
    private static Thread thread = null;
    private static Runnable runnable = null;

    private ProgressManager() {

    }

    static void setRunnable(Runnable runnable) {
        ProgressManager.runnable = runnable;
    }

    static synchronized Thread getThread() {
        if (thread == null) {
            synchronized (ProgressManager.class) {
                if (thread == null) {
                    thread = new Thread(runnable);
                }
            }
        }
        return thread;
    }

    static void freeThread() {
        thread = null;
    }

    static boolean isNull() {
        if (thread == null) {
            return true;
        } else {
            return false;
        }
    }

    static boolean isAlive() {
        if (!isNull()) {
            return thread.isAlive();
        }
        return false;
    }
}
