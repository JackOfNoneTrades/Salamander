package com.geckolib.loading.loader;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/** Internal Bedrock geometry parser and baker. */
final class GeometryParser {

    private static final double MODEL_SCALE = 1d / 16d;
    private static final double DEG_TO_RAD = Math.PI / 180d;
    private static final ForgeDirection[] FACE_ORDER = { ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH,
        ForgeDirection.SOUTH, ForgeDirection.UP, ForgeDirection.DOWN };
    private static final Set<String> KNOWN_FORMATS = new HashSet<>();

    static {
        KNOWN_FORMATS.add("1.12.0");
        KNOWN_FORMATS.add("1.14.0");
        KNOWN_FORMATS.add("1.16.0");
        KNOWN_FORMATS.add("1.19.30");
        KNOWN_FORMATS.add("1.21.0");
    }

    private GeometryParser() {}

    static BakedGeoModel parse(ResourceLocation resourcePath, JsonObject root) {
        String formatVersion = requireString(root, "format_version");

        if (!KNOWN_FORMATS.contains(formatVersion)) GeckoLibConstants.LOGGER.warn(
            "{}: unknown geometry format version '{}'; attempting GeckoLib 5-compatible parsing",
            resourcePath,
            formatVersion);

        JsonArray definitions = requireArray(root, "minecraft:geometry");

        if (definitions.size() == 0) throw new JsonParseException("No geometry definitions found in " + resourcePath);

        JsonObject definition = requireObject(definitions.get(0), "first geometry definition");
        Description description = parseDescription(resourcePath, optionalObject(definition, "description"));
        List<BoneDefinition> bones = parseBones(optionalArray(definition, "bones"));

        return bake(resourcePath, description, bones);
    }

    private static Description parseDescription(ResourceLocation resourcePath, JsonObject object) {
        if (object == null) {
            GeckoLibConstants.LOGGER.warn("{}: geometry has no description; using 16x16 defaults", resourcePath);
            return Description.DEFAULT;
        }

        String identifier = optionalString(object, "identifier", "geometry.unknown");
        Float boundsWidth = optionalFloat(object, "visible_bounds_width");
        Float boundsHeight = optionalFloat(object, "visible_bounds_height");
        GeoVector boundsOffset = optionalVector(object, "visible_bounds_offset");
        int textureWidth = optionalInt(object, "texture_width", 16);
        int textureHeight = optionalInt(object, "texture_height", 16);

        if (textureWidth <= 0 || textureHeight <= 0)
            throw new JsonParseException("Geometry texture dimensions must be positive in " + resourcePath);

        return new Description(identifier, boundsWidth, boundsHeight, boundsOffset, textureWidth, textureHeight);
    }

    private static List<BoneDefinition> parseBones(JsonArray array) {
        List<BoneDefinition> bones = new ArrayList<>();

        if (array == null) return bones;

        for (JsonElement element : array) {
            JsonObject bone = requireObject(element, "bone");
            String name = requireString(bone, "name");
            String parent = optionalString(bone, "parent", null);
            GeoVector pivot = optionalVector(bone, "pivot");
            GeoVector rotation = optionalVector(bone, "rotation");
            Boolean mirror = optionalBoolean(bone, "mirror");
            Float inflate = optionalFloat(bone, "inflate");
            JsonArray cubes = optionalArray(bone, "cubes");
            JsonObject locators = optionalObject(bone, "locators");

            bones.add(new BoneDefinition(name, parent, pivot, rotation, mirror, inflate, cubes, locators));
        }

        return bones;
    }

    private static BakedGeoModel bake(ResourceLocation resourcePath, Description description,
        List<BoneDefinition> definitions) {
        Map<String, BoneDefinition> byName = new LinkedHashMap<>();
        Map<String, List<BoneDefinition>> children = new LinkedHashMap<>();
        List<BoneDefinition> roots = new ArrayList<>();

        for (BoneDefinition definition : definitions) {
            if (byName.put(definition.name, definition) != null)
                throw new JsonParseException("Duplicate geometry bone name: " + definition.name);
        }

        for (BoneDefinition definition : definitions) {
            if (definition.parent == null) {
                roots.add(definition);
                continue;
            }

            if (definition.name.equals(definition.parent))
                throw new JsonParseException("Bone has defined itself as its parent: " + definition.name);

            if (!byName.containsKey(definition.parent)) throw new JsonParseException(
                "Bone '" + definition.name + "' has undefined parent '" + definition.parent + "'");

            children.computeIfAbsent(definition.parent, ignored -> new ArrayList<>())
                .add(definition);
        }

        validateAcyclic(definitions, byName);

        Map<String, GeoLocator> locators = new LinkedHashMap<>();
        GeoBone[] topLevelBones = new GeoBone[roots.size()];

        for (int i = 0; i < roots.size(); i++) {
            topLevelBones[i] = bakeBone(null, roots.get(i), children, description, locators);
        }

        ModelProperties properties = new ModelProperties(
            resourcePath,
            description.identifier,
            description.boundsWidth,
            description.boundsHeight,
            description.boundsOffset,
            description.textureWidth,
            description.textureHeight);

        return new BakedGeoModel(topLevelBones, locators, properties);
    }

    private static void validateAcyclic(List<BoneDefinition> definitions, Map<String, BoneDefinition> byName) {
        for (BoneDefinition definition : definitions) {
            Set<String> lineage = new HashSet<>();
            BoneDefinition current = definition;

            while (current != null) {
                if (!lineage.add(current.name))
                    throw new JsonParseException("Recursive geometry bone hierarchy involving '" + current.name + "'");

                current = current.parent == null ? null : byName.get(current.parent);
            }
        }
    }

    private static GeoBone bakeBone(GeoBone parent, BoneDefinition definition,
        Map<String, List<BoneDefinition>> childDefinitions, Description description,
        Map<String, GeoLocator> globalLocators) {
        List<BoneDefinition> children = childDefinitions.get(definition.name);
        int childCount = children == null ? 0 : children.size();
        int cubeCount = definition.cubes == null ? 0 : definition.cubes.size();
        int locatorCount = definition.locators == null ? 0
            : definition.locators.entrySet()
                .size();
        GeoBone[] bakedChildren = new GeoBone[childCount];
        GeoCube[] cubes = new GeoCube[cubeCount];
        GeoLocator[] locators = new GeoLocator[locatorCount];
        GeoVector pivot = definition.pivot == null ? GeoVector.ZERO : definition.pivot;
        GeoVector rotation = definition.rotation == null ? GeoVector.ZERO : definition.rotation;
        CuboidGeoBone bone = new CuboidGeoBone(
            parent,
            definition.name,
            bakedChildren,
            cubes,
            locators,
            (float) -pivot.x(),
            (float) pivot.y(),
            (float) pivot.z(),
            (float) (-rotation.x() * DEG_TO_RAD),
            (float) (-rotation.y() * DEG_TO_RAD),
            (float) (rotation.z() * DEG_TO_RAD));

        bakeLocators(definition.locators, bone, locators, globalLocators);

        for (int i = 0; i < cubeCount; i++) {
            cubes[i] = bakeCube(
                requireObject(definition.cubes.get(i), "cube on bone '" + definition.name + "'"),
                definition,
                description);
        }

        for (int i = 0; i < childCount; i++) {
            bakedChildren[i] = bakeBone(bone, children.get(i), childDefinitions, description, globalLocators);
        }

        return bone;
    }

    private static void bakeLocators(JsonObject definitions, GeoBone bone, GeoLocator[] target,
        Map<String, GeoLocator> globalLocators) {
        if (definitions == null) return;

        int index = 0;

        for (Map.Entry<String, JsonElement> entry : definitions.entrySet()) {
            JsonElement value = entry.getValue();
            GeoVector offset;
            GeoVector rotation;

            if (value.isJsonArray()) {
                offset = parseVector(value.getAsJsonArray(), "locator '" + entry.getKey() + "'");
                rotation = GeoVector.ZERO;
            } else {
                JsonObject locator = requireObject(value, "locator '" + entry.getKey() + "'");
                offset = vectorOrZero(optionalVector(locator, "offset"));
                rotation = vectorOrZero(optionalVector(locator, "rotation"));
            }

            GeoLocator baked = new GeoLocator(
                bone,
                entry.getKey(),
                (float) -offset.x(),
                (float) offset.y(),
                (float) offset.z(),
                (float) (-rotation.x() * DEG_TO_RAD),
                (float) (-rotation.y() * DEG_TO_RAD),
                (float) (rotation.z() * DEG_TO_RAD));

            target[index++] = baked;

            if (globalLocators.put(entry.getKey(), baked) != null)
                GeckoLibConstants.LOGGER.error("Duplicate locator name '{}' on bone '{}'", entry.getKey(), bone.name());
        }
    }

    private static GeoCube bakeCube(JsonObject cube, BoneDefinition bone, Description description) {
        GeoVector rawOrigin = vectorOrZero(optionalVector(cube, "origin"));
        GeoVector rawSize = vectorOrZero(optionalVector(cube, "size"));
        GeoVector rawRotation = vectorOrZero(optionalVector(cube, "rotation"));
        GeoVector rawPivot = vectorOrZero(optionalVector(cube, "pivot"));
        Float cubeInflate = optionalFloat(cube, "inflate");
        Boolean cubeMirror = optionalBoolean(cube, "mirror");
        boolean mirror = cubeMirror != null ? cubeMirror : Boolean.TRUE.equals(bone.mirror);
        double inflation = (cubeInflate != null ? cubeInflate : bone.inflate == null ? 0 : bone.inflate) * MODEL_SCALE;
        GeoVector origin = rawOrigin.add(rawSize.x(), 0, 0)
            .multiply(-MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        GeoVector vertexSize = rawSize.scale(MODEL_SCALE);
        GeoVector rotation = rawRotation.multiply(-DEG_TO_RAD, -DEG_TO_RAD, DEG_TO_RAD);
        GeoVector pivot = rawPivot.multiply(-1, 1, 1);
        JsonElement rawUv = cube.get("uv");

        if (rawUv == null || rawUv.isJsonNull()) throw new JsonParseException("Cube is missing required UV data");

        UvDefinition uv = parseUv(rawUv);
        VertexSet vertices = new VertexSet(origin, vertexSize, inflation);
        GeoQuad[] quads = new GeoQuad[6];

        for (int i = 0; i < FACE_ORDER.length; i++) {
            ForgeDirection direction = FACE_ORDER[i];

            quads[i] = isZeroSizeFace(rawSize, direction) ? null
                : uv.bake(vertices, rawSize, direction, mirror, description.textureWidth, description.textureHeight);
        }

        return new GeoCube(quads, pivot, rotation, rawSize);
    }

    private static boolean isZeroSizeFace(GeoVector size, ForgeDirection direction) {
        if (size.x() == 0) return direction != ForgeDirection.WEST && direction != ForgeDirection.EAST;

        if (size.y() == 0) return direction != ForgeDirection.UP && direction != ForgeDirection.DOWN;

        if (size.z() == 0) return direction != ForgeDirection.NORTH && direction != ForgeDirection.SOUTH;

        return false;
    }

    private static UvDefinition parseUv(JsonElement element) {
        if (element.isJsonArray()) return UvDefinition.box(parseUvPair(element.getAsJsonArray(), "box UV"));

        JsonObject object = requireObject(element, "UV mapping");
        EnumMap<ForgeDirection, FaceUv> faces = new EnumMap<>(ForgeDirection.class);

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            ForgeDirection direction;

            try {
                direction = ForgeDirection.valueOf(
                    entry.getKey()
                        .toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new JsonParseException("Unknown UV face direction '" + entry.getKey() + "'", exception);
            }

            if (direction == ForgeDirection.UNKNOWN)
                throw new JsonParseException("UNKNOWN is not a valid UV face direction");

            JsonObject face = requireObject(entry.getValue(), "UV face '" + entry.getKey() + "'");
            UvPair uv = parseUvPair(requireArray(face, "uv"), "UV face coordinate");
            UvPair uvSize = parseUvPair(requireArray(face, "uv_size"), "UV face size");
            int rotation = optionalInt(face, "uv_rotation", 0);

            faces.put(direction, new FaceUv(uv, uvSize, UvRotation.fromDegrees(rotation)));
        }

        return UvDefinition.faces(faces);
    }

    private static UvPair parseUvPair(JsonArray array, String context) {
        if (array.size() != 2) throw new JsonParseException(context + " must contain exactly two values");

        return new UvPair(
            array.get(0)
                .getAsDouble(),
            array.get(1)
                .getAsDouble());
    }

    private static GeoQuad bakeQuad(VertexSet vertices, ForgeDirection direction, UvPair uv, UvPair uvSize,
        UvRotation rotation, boolean boxUv, boolean mirror, int textureWidth, int textureHeight) {
        GeoVertex[] quadVertices = vertices.verticesForQuad(direction, boxUv, mirror);
        float normalX = direction.offsetX;
        float normalY = direction.offsetY;
        float normalZ = direction.offsetZ;
        double u = uv.u;
        double v = uv.v;
        double uWidth = (u + uvSize.u) / textureWidth;
        double vHeight = (v + uvSize.v) / textureHeight;

        u /= textureWidth;
        v /= textureHeight;

        if (!mirror) {
            double swap = uWidth;
            uWidth = u;
            u = swap;
        } else {
            normalX *= -1;
        }

        double[] coordinates = rotation.rotate(u, v, uWidth, vHeight);

        quadVertices[0] = quadVertices[0].withUVs(coordinates[0], coordinates[1]);
        quadVertices[1] = quadVertices[1].withUVs(coordinates[2], coordinates[3]);
        quadVertices[2] = quadVertices[2].withUVs(coordinates[4], coordinates[5]);
        quadVertices[3] = quadVertices[3].withUVs(coordinates[6], coordinates[7]);

        return new GeoQuad(quadVertices, normalX, normalY, normalZ, direction);
    }

    private static GeoVector vectorOrZero(GeoVector vector) {
        return vector == null ? GeoVector.ZERO : vector;
    }

    private static GeoVector optionalVector(JsonObject object, String member) {
        JsonElement value = object.get(member);

        if (value == null || value.isJsonNull()) return null;

        if (!value.isJsonArray()) throw new JsonParseException("JSON member '" + member + "' must be an array");

        return parseVector(value.getAsJsonArray(), member);
    }

    private static GeoVector parseVector(JsonArray array, String context) {
        if (array.size() != 3) throw new JsonParseException(context + " must contain exactly three values");

        return new GeoVector(
            array.get(0)
                .getAsDouble(),
            array.get(1)
                .getAsDouble(),
            array.get(2)
                .getAsDouble());
    }

    private static JsonArray requireArray(JsonObject object, String member) {
        JsonArray value = optionalArray(object, member);

        if (value == null) throw new JsonParseException("Missing JSON array '" + member + "'");

        return value;
    }

    private static JsonArray optionalArray(JsonObject object, String member) {
        JsonElement value = object.get(member);

        if (value == null || value.isJsonNull()) return null;

        if (!value.isJsonArray()) throw new JsonParseException("JSON member '" + member + "' must be an array");

        return value.getAsJsonArray();
    }

    private static JsonObject optionalObject(JsonObject object, String member) {
        JsonElement value = object.get(member);

        if (value == null || value.isJsonNull()) return null;

        return requireObject(value, member);
    }

    private static JsonObject requireObject(JsonElement element, String context) {
        if (element == null || !element.isJsonObject())
            throw new JsonParseException(context + " must be a JSON object");

        return element.getAsJsonObject();
    }

    private static String requireString(JsonObject object, String member) {
        JsonElement value = object.get(member);

        if (value == null || value.isJsonNull()) throw new JsonParseException("Missing JSON string '" + member + "'");

        return value.getAsString();
    }

    private static String optionalString(JsonObject object, String member, String defaultValue) {
        JsonElement value = object.get(member);

        return value == null || value.isJsonNull() ? defaultValue : value.getAsString();
    }

    private static Float optionalFloat(JsonObject object, String member) {
        JsonElement value = object.get(member);

        return value == null || value.isJsonNull() ? null : value.getAsFloat();
    }

    private static Boolean optionalBoolean(JsonObject object, String member) {
        JsonElement value = object.get(member);

        return value == null || value.isJsonNull() ? null : value.getAsBoolean();
    }

    private static int optionalInt(JsonObject object, String member, int defaultValue) {
        JsonElement value = object.get(member);

        return value == null || value.isJsonNull() ? defaultValue : value.getAsInt();
    }

    private static final class Description {

        private static final Description DEFAULT = new Description("geometry.unknown", null, null, null, 16, 16);

        private final String identifier;
        private final Float boundsWidth;
        private final Float boundsHeight;
        private final GeoVector boundsOffset;
        private final int textureWidth;
        private final int textureHeight;

        private Description(String identifier, Float boundsWidth, Float boundsHeight, GeoVector boundsOffset,
            int textureWidth, int textureHeight) {
            this.identifier = identifier;
            this.boundsWidth = boundsWidth;
            this.boundsHeight = boundsHeight;
            this.boundsOffset = boundsOffset;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }
    }

    private static final class BoneDefinition {

        private final String name;
        private final String parent;
        private final GeoVector pivot;
        private final GeoVector rotation;
        private final Boolean mirror;
        private final Float inflate;
        private final JsonArray cubes;
        private final JsonObject locators;

        private BoneDefinition(String name, String parent, GeoVector pivot, GeoVector rotation, Boolean mirror,
            Float inflate, JsonArray cubes, JsonObject locators) {
            this.name = name;
            this.parent = parent;
            this.pivot = pivot;
            this.rotation = rotation;
            this.mirror = mirror;
            this.inflate = inflate;
            this.cubes = cubes;
            this.locators = locators;
        }
    }

    private static final class UvPair {

        private final double u;
        private final double v;

        private UvPair(double u, double v) {
            this.u = u;
            this.v = v;
        }
    }

    private static final class FaceUv {

        private final UvPair uv;
        private final UvPair size;
        private final UvRotation rotation;

        private FaceUv(UvPair uv, UvPair size, UvRotation rotation) {
            this.uv = uv;
            this.size = size;
            this.rotation = rotation;
        }
    }

    private static final class UvDefinition {

        private final UvPair box;
        private final EnumMap<ForgeDirection, FaceUv> faces;

        private UvDefinition(UvPair box, EnumMap<ForgeDirection, FaceUv> faces) {
            this.box = box;
            this.faces = faces;
        }

        private static UvDefinition box(UvPair box) {
            return new UvDefinition(box, null);
        }

        private static UvDefinition faces(EnumMap<ForgeDirection, FaceUv> faces) {
            return new UvDefinition(null, faces);
        }

        private GeoQuad bake(VertexSet vertices, GeoVector cubeSize, ForgeDirection direction, boolean mirror,
            int textureWidth, int textureHeight) {
            if (this.box != null) {
                GeoVector floored = new GeoVector(
                    Math.floor(cubeSize.x()),
                    Math.floor(cubeSize.y()),
                    Math.floor(cubeSize.z()));
                UvPair uv = boxUvCoordinates(direction, this.box, floored);
                UvPair size = boxUvSize(direction, floored);

                return bakeQuad(
                    vertices,
                    direction,
                    uv,
                    size,
                    UvRotation.NONE,
                    true,
                    mirror,
                    textureWidth,
                    textureHeight);
            }

            FaceUv face = this.faces.get(direction);

            return face == null ? null
                : bakeQuad(
                    vertices,
                    direction,
                    face.uv,
                    face.size,
                    face.rotation,
                    false,
                    mirror,
                    textureWidth,
                    textureHeight);
        }

        private static UvPair boxUvCoordinates(ForgeDirection direction, UvPair uv, GeoVector size) {
            switch (direction) {
                case WEST:
                    return new UvPair(uv.u + size.z() + size.x(), uv.v + size.z());
                case EAST:
                    return new UvPair(uv.u, uv.v + size.z());
                case NORTH:
                    return new UvPair(uv.u + size.z(), uv.v + size.z());
                case SOUTH:
                    return new UvPair(uv.u + size.z() + size.x() + size.z(), uv.v + size.z());
                case UP:
                    return new UvPair(uv.u + size.z(), uv.v);
                case DOWN:
                    return new UvPair(uv.u + size.z() + size.x(), uv.v + size.z());
                default:
                    throw new IllegalArgumentException("Unsupported face: " + direction);
            }
        }

        private static UvPair boxUvSize(ForgeDirection direction, GeoVector size) {
            switch (direction) {
                case WEST:
                case EAST:
                    return new UvPair(size.z(), size.y());
                case NORTH:
                case SOUTH:
                    return new UvPair(size.x(), size.y());
                case UP:
                    return new UvPair(size.x(), size.z());
                case DOWN:
                    return new UvPair(size.x(), -size.z());
                default:
                    throw new IllegalArgumentException("Unsupported face: " + direction);
            }
        }
    }

    private enum UvRotation {

        NONE,
        CLOCKWISE_90,
        CLOCKWISE_180,
        CLOCKWISE_270;

        private static UvRotation fromDegrees(int degrees) {
            int normalized = degrees < 0 ? 360 - (-degrees % 360) : degrees % 360;

            return values()[(normalized % 360) / 90];
        }

        private double[] rotate(double u, double v, double uWidth, double vHeight) {
            switch (this) {
                case NONE:
                    return new double[] { u, v, uWidth, v, uWidth, vHeight, u, vHeight };
                case CLOCKWISE_90:
                    return new double[] { uWidth, v, uWidth, vHeight, u, vHeight, u, v };
                case CLOCKWISE_180:
                    return new double[] { uWidth, vHeight, u, vHeight, u, v, uWidth, v };
                case CLOCKWISE_270:
                    return new double[] { u, vHeight, u, v, uWidth, v, uWidth, vHeight };
                default:
                    throw new AssertionError(this);
            }
        }
    }

    private static final class VertexSet {

        private final GeoVertex bottomLeftBack;
        private final GeoVertex bottomRightBack;
        private final GeoVertex topLeftBack;
        private final GeoVertex topRightBack;
        private final GeoVertex topLeftFront;
        private final GeoVertex topRightFront;
        private final GeoVertex bottomLeftFront;
        private final GeoVertex bottomRightFront;

        private VertexSet(GeoVector origin, GeoVector size, double inflation) {
            this.bottomLeftBack = new GeoVertex(origin.x() - inflation, origin.y() - inflation, origin.z() - inflation);
            this.bottomRightBack = new GeoVertex(
                origin.x() - inflation,
                origin.y() - inflation,
                origin.z() + size.z() + inflation);
            this.topLeftBack = new GeoVertex(
                origin.x() - inflation,
                origin.y() + size.y() + inflation,
                origin.z() - inflation);
            this.topRightBack = new GeoVertex(
                origin.x() - inflation,
                origin.y() + size.y() + inflation,
                origin.z() + size.z() + inflation);
            this.topLeftFront = new GeoVertex(
                origin.x() + size.x() + inflation,
                origin.y() + size.y() + inflation,
                origin.z() - inflation);
            this.topRightFront = new GeoVertex(
                origin.x() + size.x() + inflation,
                origin.y() + size.y() + inflation,
                origin.z() + size.z() + inflation);
            this.bottomLeftFront = new GeoVertex(
                origin.x() + size.x() + inflation,
                origin.y() - inflation,
                origin.z() - inflation);
            this.bottomRightFront = new GeoVertex(
                origin.x() + size.x() + inflation,
                origin.y() - inflation,
                origin.z() + size.z() + inflation);
        }

        private GeoVertex[] verticesForQuad(ForgeDirection direction, boolean boxUv, boolean mirror) {
            switch (direction) {
                case WEST:
                    return mirror ? east() : west();
                case EAST:
                    return mirror ? west() : east();
                case NORTH:
                    return north();
                case SOUTH:
                    return south();
                case UP:
                    return mirror && !boxUv ? down() : up();
                case DOWN:
                    return mirror && !boxUv ? up() : down();
                default:
                    throw new IllegalArgumentException("Unsupported face: " + direction);
            }
        }

        private GeoVertex[] west() {
            return new GeoVertex[] { this.topRightBack, this.topLeftBack, this.bottomLeftBack, this.bottomRightBack };
        }

        private GeoVertex[] east() {
            return new GeoVertex[] { this.topLeftFront, this.topRightFront, this.bottomRightFront,
                this.bottomLeftFront };
        }

        private GeoVertex[] north() {
            return new GeoVertex[] { this.topLeftBack, this.topLeftFront, this.bottomLeftFront, this.bottomLeftBack };
        }

        private GeoVertex[] south() {
            return new GeoVertex[] { this.topRightFront, this.topRightBack, this.bottomRightBack,
                this.bottomRightFront };
        }

        private GeoVertex[] up() {
            return new GeoVertex[] { this.topRightBack, this.topRightFront, this.topLeftFront, this.topLeftBack };
        }

        private GeoVertex[] down() {
            return new GeoVertex[] { this.bottomLeftBack, this.bottomLeftFront, this.bottomRightFront,
                this.bottomRightBack };
        }
    }
}
