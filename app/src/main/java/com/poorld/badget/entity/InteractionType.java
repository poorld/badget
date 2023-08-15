package com.poorld.badget.entity;

public enum InteractionType {

    Listen(0),Connect(1), Script(2), ScriptDirectory(3);

    int interactionType;

    InteractionType(int type) {
        this.interactionType = type;
    }

    public int getInteractionType() {
        return interactionType;
    }

    public static InteractionType fromAttr(int attrValue) {
        for (InteractionType type : values()) {
            if (type.interactionType == attrValue) {
                return type;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String toString() {
        return "InteractionType{" +
                "interactionType=" + interactionType +
                '}';
    }
}
