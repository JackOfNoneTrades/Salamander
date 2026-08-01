package com.geckolib.cache;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.geckolib.GeckoLibConstants;
import com.geckolib.animatable.SingletonGeoAnimatable;

/** Stable network identity registry for singleton animatables such as items. */
public final class SyncedSingletonAnimatableCache {

    private static final Map<SingletonGeoAnimatable, String> ANIMATABLE_IDENTITIES = new IdentityHashMap<>();
    private static final Map<String, SingletonGeoAnimatable> SYNCED_ANIMATABLES = new HashMap<>();

    private SyncedSingletonAnimatableCache() {}

    public static synchronized void registerSyncedAnimatable(SingletonGeoAnimatable animatable) {
        String id = getOrCreateId(animatable);
        SingletonGeoAnimatable existing = SYNCED_ANIMATABLES.put(id, animatable);

        if (existing == null) GeckoLibConstants.LOGGER.debug("Registered synced singleton animatable {}", id);
    }

    public static synchronized SingletonGeoAnimatable getSyncedAnimatable(String syncedAnimatableId) {
        SingletonGeoAnimatable animatable = SYNCED_ANIMATABLES.get(syncedAnimatableId);

        if (animatable == null) {
            GeckoLibConstants.LOGGER
                .error("Attempted to retrieve unregistered synced singleton animatable {}", syncedAnimatableId);
        }

        return animatable;
    }

    public static synchronized String getOrCreateId(SingletonGeoAnimatable animatable) {
        String existing = ANIMATABLE_IDENTITIES.get(animatable);

        if (existing != null) return existing;

        String baseId = animatable.getClass()
            .getName();
        int index = 0;
        String id = baseId + index;

        while (SYNCED_ANIMATABLES.containsKey(id) || ANIMATABLE_IDENTITIES.containsValue(id)) id = baseId + ++index;

        ANIMATABLE_IDENTITIES.put(animatable, id);

        return id;
    }
}
