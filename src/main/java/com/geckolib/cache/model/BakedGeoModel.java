package com.geckolib.cache.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.geckolib.cache.BakedModelCache;

/** Baked geometry model with lazily initialized bone lookup. */
public class BakedGeoModel {

    private final GeoBone[] topLevelBones;
    private final Map<String, GeoLocator> locators;
    private final ModelProperties properties;
    private volatile Map<String, GeoBone> boneLookup;

    public BakedGeoModel(GeoBone[] topLevelBones, Map<String, GeoLocator> locators, ModelProperties properties) {
        this.topLevelBones = topLevelBones;
        this.locators = Collections.unmodifiableMap(new LinkedHashMap<>(locators));
        this.properties = properties;
    }

    public GeoBone[] topLevelBones() {
        return this.topLevelBones;
    }

    public Map<String, GeoLocator> locators() {
        return this.locators;
    }

    public ModelProperties properties() {
        return this.properties;
    }

    public Map<String, GeoBone> boneLookup() {
        Map<String, GeoBone> lookup = this.boneLookup;

        if (lookup == null) {
            synchronized (this) {
                lookup = this.boneLookup;

                if (lookup == null) {
                    Map<String, GeoBone> bones = new LinkedHashMap<>();

                    for (GeoBone bone : this.topLevelBones) {
                        collectBones(bone, bones);
                    }

                    lookup = Collections.unmodifiableMap(bones);
                    this.boneLookup = lookup;
                }
            }
        }

        return lookup;
    }

    public Optional<GeoBone> getBone(String name) {
        return Optional.ofNullable(boneLookup().get(name));
    }

    public Optional<GeoLocator> getLocator(String name) {
        return Optional.ofNullable(this.locators.get(name));
    }

    public boolean isMissingno() {
        return this == BakedModelCache.missingModel();
    }

    private static void collectBones(GeoBone bone, Map<String, GeoBone> bones) {
        bones.put(bone.name(), bone);

        for (GeoBone child : bone.children()) {
            collectBones(child, bones);
        }
    }
}
