package com.deviceatlas.cloud.deviceidentification.client;

public enum ActionConstants {

    /** Action to be taken after an end-point responds: If an-endpoint response was fine */
    FAILOVER_NOT_REQUIRED((byte)0),
    /** Action to be taken after an end-point responds: If the error controller returns this the fail-over mechanism must stop trying the next end-point */
    FAILOVER_STOP((byte)1),
    /** Action to be taken after an end-point responds: If the error controller returns this the fail-over mechanism must try the next end-point */
    FAILOVER_CONTINUE((byte)2);

    private final byte action;

    ActionConstants(final byte action) {
        this.action = action;
    }

    public byte getAction() {
        return action;
    }
}
