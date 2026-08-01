/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class TabulaAnimationContainer {

    private String name;
    private String identifier;
    private boolean loops;
    private Map<String, List<TabulaAnimationComponentContainer>> sets = new HashMap<>();

    public String getName() {
        return this.name;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean doesLoop() {
        return this.loops;
    }

    public Map<String, List<TabulaAnimationComponentContainer>> getComponents() {
        return this.sets;
    }
}
