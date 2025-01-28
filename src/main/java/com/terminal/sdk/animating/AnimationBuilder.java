package com.terminal.sdk.animating;

import java.util.ArrayList;
import java.util.List;


/**
 * Builder для создания и настройки анимаций.
 */
public class AnimationBuilder {
    private String name;
    private int frameDelay;
    private List<Animation> compositeAnimations;
    private AnimationType type;

    public enum AnimationType {
        NETWORK_SCAN,
        COMPOSITE
    }

    private AnimationBuilder() {
        this.frameDelay = 100;
        this.compositeAnimations = new ArrayList<>();
    }

    public static AnimationBuilder create() {
        return new AnimationBuilder();
    }

    public AnimationBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public AnimationBuilder withFrameDelay(int frameDelay) {
        this.frameDelay = frameDelay;
        return this;
    }

    public AnimationBuilder withType(AnimationType type) {
        this.type = type;
        return this;
    }

    public AnimationBuilder addAnimation(Animation animation) {
        this.compositeAnimations.add(animation);
        return this;
    }

    public Animation build() {
        if (type == null) {
            throw new IllegalStateException("Animation type must be specified");
        }

        switch (type) {
            case COMPOSITE:
                return new CompositeAnimation(name != null ? name : "composite", 
                                           frameDelay, 
                                           compositeAnimations);
            default:
                throw new IllegalStateException("Unknown animation type: " + type);
        }
    }
} 