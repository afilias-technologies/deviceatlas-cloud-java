package com.deviceatlas.cloud.deviceidentification.client;

public enum HeaderConstants {
    DA_HEADER_PREFIX("X-DA-"),
    CLIENT_COOKIE_HEADER("Client-Properties"),
    REMOTE_ADDR("remote-addr"),
    UA_HEADER("user-agent"),
    COOKIE_HEADER("cookie");

    private final String text;

    HeaderConstants(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
