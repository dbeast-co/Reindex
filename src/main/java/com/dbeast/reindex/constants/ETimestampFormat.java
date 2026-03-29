package com.dbeast.reindex.constants;

/**
 * Enum to define timestamp format types in the Time Series Reindex Algorithm
 * Specifies whether the timestamp field contains epoch milliseconds or nanoseconds
 */
public enum ETimestampFormat {
    EPOCH_MILLIS("Epoch Milliseconds", "epoch_millis"),
    EPOCH_NANOS("Epoch Nanoseconds", "epoch_nanos");

    private final String displayName;
    private final String varForUI;

    ETimestampFormat(final String displayName, final String varForUI) {
        this.displayName = displayName;
        this.varForUI = varForUI;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVarForUI() {
        return varForUI;
    }

    /**
     * Get enum value by the UI variable name
     */
    public static ETimestampFormat fromVarForUI(final String varForUI) {
        for (ETimestampFormat format : ETimestampFormat.values()) {
            if (format.varForUI.equals(varForUI)) {
                return format;
            }
        }
        // Default to epoch milliseconds if not found
        return EPOCH_MILLIS;
    }
}

