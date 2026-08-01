package com.geckolib.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import com.geckolib.GeckoLibConstants;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoLocator;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVector;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.ModelProperties;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;

/** Immutable snapshot of baked geometry resources loaded during the latest resource reload. */
public final class BakedModelCache {

    private static final class MissingModelHolder {

        private static final BakedGeoModel INSTANCE = createMissingModel();
    }

    private final Map<ResourceLocation, BakedGeoModel> cache;

    public BakedModelCache(Map<ResourceLocation, BakedGeoModel> cache) {
        this.cache = Collections.unmodifiableMap(new LinkedHashMap<>(cache));
    }

    public Map<ResourceLocation, BakedGeoModel> cache() {
        return this.cache;
    }

    public int size() {
        return this.cache.size();
    }

    public BakedGeoModel getModel(ResourceLocation modelFile) {
        BakedGeoModel model = this.cache.get(modelFile);

        if (model == null) {
            ResourceLocation stripped = GeckoLibResources.stripPrefixAndSuffix(modelFile);

            if (!modelFile.equals(stripped)) {
                GeckoLibConstants.LOGGER
                    .error("Superfluous prefix or suffix in model resource path '{}'; use '{}'", modelFile, stripped);
                model = this.cache.get(stripped);
            }
        }

        if (model != null) return model;

        GeckoLibConstants.LOGGER.error("Unable to find model '{}'", modelFile);

        return missingModel();
    }

    public static BakedGeoModel missingModel() {
        return MissingModelHolder.INSTANCE;
    }

    private static BakedGeoModel createMissingModel() {
        GeoVertex[] vertices = { new GeoVertex(-0.5f, 1, 0.5f, 1, 0), new GeoVertex(-0.5f, 1, -0.5f, 0, 0),
            new GeoVertex(-0.5f, 0, -0.5f, 0, 1), new GeoVertex(-0.5f, 0, 0.5f, 1, 1) };
        GeoQuad[] quads = { new GeoQuad(vertices, -1, 0, 0, ForgeDirection.WEST),
            new GeoQuad(vertices, 1, 0, 0, ForgeDirection.EAST), new GeoQuad(vertices, 0, 0, -1, ForgeDirection.NORTH),
            new GeoQuad(vertices, 0, 0, 1, ForgeDirection.SOUTH), new GeoQuad(vertices, 0, 1, 0, ForgeDirection.UP),
            new GeoQuad(vertices, 0, -1, 0, ForgeDirection.DOWN) };
        GeoCube cube = new GeoCube(quads, GeoVector.ZERO, GeoVector.ZERO, new GeoVector(16, 16, 16));
        GeoBone root = new CuboidGeoBone(
            null,
            "Main",
            new GeoBone[0],
            new GeoCube[] { cube },
            new GeoLocator[0],
            0,
            0,
            0,
            0,
            0,
            0);
        ModelProperties properties = new ModelProperties(
            GeckoLibConstants.id("internal/missingno"),
            "geometry.unknown",
            2.5f,
            2.5f,
            new GeoVector(0, 0.75, 0),
            16,
            16);

        return new BakedGeoModel(new GeoBone[] { root }, Collections.emptyMap(), properties);
    }
}
