package com.microsoft.gctoolkit.event.zgc;

public enum ZGCPhase {
    FULL(""),
    MAJOR_YOUNG("Y"),
    MAJOR_OLD("O"),
    MINOR_YOUNG("y");

    private final String phase;

    ZGCPhase(String s) {
        this.phase = s;
    }

    public String getPhase() {
        return phase;
    }

    public static ZGCPhase get(String label) {
        for (ZGCPhase zgcPhase : ZGCPhase.values()) {
            if (zgcPhase.getPhase().equals(label.trim())) {
                return zgcPhase;
            }
        }
        throw new IllegalArgumentException("No matching ZGCPhase found for: " + label);
    }
}
