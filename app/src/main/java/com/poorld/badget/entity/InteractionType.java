package com.poorld.badget.entity;

public enum InteractionType {

    Listen(1),Connect(2), Script(3), ScriptDirectory(4);

    int interactionType;

    InteractionType(int type) {
        this.interactionType = type;
    }

    public int getInteractionType() {
        return interactionType;
    }

}
