package com.overstacked.config;

public record StackRule(String target, int maxStack) {

    public boolean isTag() {
        return target.startsWith("#");
    }

    public String rawTarget() {
        return isTag() ? target.substring(1) : target;
    }
}
