package com.geckolib.animation.object;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.geckolib.cache.animation.Animation;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Post-play handling for a baked animation. */
@FunctionalInterface
public interface LoopType {

    Map<String, LoopType> LOOP_TYPES = new ConcurrentHashMap<>();

    LoopType DEFAULT = animation -> animation.loopType()
        .shouldPlayAgain(animation);
    LoopType PLAY_ONCE = register("play_once", register("false", animation -> false));
    LoopType HOLD_ON_LAST_FRAME = register("hold_on_last_frame", animation -> false);
    LoopType LOOP = register("loop", register("true", animation -> true));

    boolean shouldPlayAgain(Animation animation);

    default String getId() {
        for (Map.Entry<String, LoopType> entry : LOOP_TYPES.entrySet()) {
            if (entry.getValue() == this) return entry.getKey();
        }

        throw new IllegalStateException("LoopType has not been registered");
    }

    static LoopType fromJson(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) return PLAY_ONCE;

        JsonPrimitive primitive = json.getAsJsonPrimitive();

        if (primitive.isBoolean()) return primitive.getAsBoolean() ? LOOP : PLAY_ONCE;

        if (primitive.isString()) return fromString(primitive.getAsString());

        return PLAY_ONCE;
    }

    static LoopType fromString(String name) {
        LoopType loopType = LOOP_TYPES.get(name);

        return loopType == null ? PLAY_ONCE : loopType;
    }

    static <T extends LoopType> T register(String name, T loopType) {
        LOOP_TYPES.put(name, loopType);

        return loopType;
    }
}
