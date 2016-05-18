package ch.hsr.zedcontrol.roborio.parsing;

import org.jetbrains.annotations.Contract;

/**
 * Defines all the possible key words for parsed data objects.
 */
public enum KeyWords {
    BATTERY("Battery"),
    STATE("State"),
    LOCK("Lock"),
    UNLOCK("Unlock"),
    MODE("Mode");

    private final String stringValue;


    KeyWords(String s) {
        stringValue = s;
    }


    @Contract(pure = true)
    @Override
    public String toString() {
        return this.stringValue;
    }
}
