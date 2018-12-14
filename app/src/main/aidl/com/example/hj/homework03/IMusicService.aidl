// IMusicService.aidl
package com.example.hj.homework03;

// Declare any non-default types here with import statements

interface IMusicService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    int currentPosition();
    int getMaxDuration();
    void resume();
    void pause();
    void rewind();
    int getStatus();
}
