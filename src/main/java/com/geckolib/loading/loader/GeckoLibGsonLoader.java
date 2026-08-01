package com.geckolib.loading.loader;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.util.ResourceLocation;

import com.geckolib.animation.object.EasingType;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.animation.Keyframe;
import com.geckolib.cache.animation.KeyframeStack;
import com.geckolib.cache.animation.keyframeevent.CustomInstructionKeyframeData;
import com.geckolib.cache.animation.keyframeevent.ParticleKeyframeData;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.definition.animation.DoubleOrString;
import com.geckolib.loading.math.MathParser;
import com.geckolib.loading.math.MathValue;
import com.geckolib.loading.math.value.Negative;
import com.geckolib.loading.math.value.ScaledValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/** Gson-backed GeckoLib 5 animation loader with 1.7.10-safe reader entry points. */
public final class GeckoLibGsonLoader implements GeckoLibLoader {

    private static final double KEYFRAME_EPSILON = 1.0E-5;
    private static final double DEG_TO_RAD = Math.PI / 180d;

    @Override
    public String[] supportedExtensions() {
        return new String[] { "json" };
    }

    @Override
    public BakedGeoModel loadModel(ResourceLocation resourcePath, Reader reader) {
        JsonElement json = new JsonParser().parse(reader);

        if (!json.isJsonObject()) throw new JsonParseException("Geometry file root must be a JSON object");

        return loadModel(resourcePath, json.getAsJsonObject());
    }

    public BakedGeoModel loadModel(ResourceLocation resourcePath, JsonObject root) {
        if (resourcePath.getResourcePath()
            .endsWith(".animation.json"))
            throw new JsonParseException("Found animation file in models folder: " + resourcePath);

        return GeometryParser.parse(resourcePath, root);
    }

    public BakedAnimations loadAnimations(Reader reader) {
        return loadAnimations(reader, MathParser.createWithDeduplication());
    }

    @Override
    public BakedAnimations loadAnimations(ResourceLocation resourcePath, Reader reader, MathParser mathParser) {
        if (resourcePath.getResourcePath()
            .endsWith(".geo.json"))
            throw new JsonParseException("Found geometry file in animations folder: " + resourcePath);

        return loadAnimations(reader, mathParser);
    }

    public BakedAnimations loadAnimations(Reader reader, MathParser mathParser) {
        JsonElement json = new JsonParser().parse(reader);

        if (!json.isJsonObject()) throw new JsonParseException("Animation file root must be a JSON object");

        return loadAnimations(json.getAsJsonObject(), mathParser);
    }

    public BakedAnimations loadAnimations(JsonObject root, MathParser mathParser) {
        requireString(root, "format_version");
        JsonObject animationDefinitions = requireObject(root, "animations");
        Map<String, Animation> animations = new LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> entry : animationDefinitions.entrySet()) {
            if (!entry.getValue()
                .isJsonObject())
                throw new JsonParseException("Animation '" + entry.getKey() + "' must be a JSON object");

            animations.put(
                entry.getKey(),
                bakeAnimation(
                    entry.getKey(),
                    entry.getValue()
                        .getAsJsonObject(),
                    mathParser));
        }

        return new BakedAnimations(animations);
    }

    private Animation bakeAnimation(String name, JsonObject definition, MathParser mathParser) {
        List<BoneAnimation> boneAnimations = new ArrayList<>();
        double calculatedLength = 0;
        JsonObject bones = optionalObject(definition, "bones");

        if (bones != null) {
            for (Map.Entry<String, JsonElement> boneEntry : bones.entrySet()) {
                if (!boneEntry.getValue()
                    .isJsonObject())
                    throw new JsonParseException(
                        "Bone '" + boneEntry.getKey() + "' in animation '" + name + "' must be a JSON object");

                JsonObject bone = boneEntry.getValue()
                    .getAsJsonObject();
                TrackResult position = bakeTrack(
                    bone.get("position"),
                    AnimationPoint.Transform.TRANSLATION,
                    mathParser);
                TrackResult rotation = bakeTrack(bone.get("rotation"), AnimationPoint.Transform.ROTATION, mathParser);
                TrackResult scale = bakeTrack(bone.get("scale"), AnimationPoint.Transform.SCALE, mathParser);

                calculatedLength = Math.max(
                    calculatedLength,
                    Math.max(position.lastTimestamp, Math.max(rotation.lastTimestamp, scale.lastTimestamp)));
                boneAnimations.add(new BoneAnimation(boneEntry.getKey(), rotation.stack, position.stack, scale.stack));
            }
        }

        double animationLength = definition.has("animation_length") ? definition.get("animation_length")
            .getAsDouble() : calculatedLength == 0 ? Double.MAX_VALUE : calculatedLength;
        LoopType loopType = LoopType.fromJson(definition.get("loop"));
        Animation.KeyframeMarkers markers = bakeMarkers(definition);

        return Animation.create(name, animationLength, loopType, boneAnimations.toArray(new BoneAnimation[0]), markers);
    }

    private TrackResult bakeTrack(JsonElement rawTrack, AnimationPoint.Transform transform, MathParser mathParser) {
        if (rawTrack == null || rawTrack.isJsonNull()) return TrackResult.EMPTY;

        List<TimedKeyframe> sourceFrames = new ArrayList<>();
        boolean forceLinear = true;
        double lastTimestamp = 0;

        if (rawTrack.isJsonPrimitive() || rawTrack.isJsonArray()) {
            sourceFrames.add(new TimedKeyframe(0, rawTrack));
        } else if (rawTrack.isJsonObject()) {
            JsonObject object = rawTrack.getAsJsonObject();

            if (isKeyframeObject(object)) {
                sourceFrames.add(new TimedKeyframe(0, rawTrack));
            } else {
                TreeMap<Double, JsonElement> sortedFrames = new TreeMap<>();

                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    double timestamp;

                    try {
                        timestamp = Double.parseDouble(entry.getKey());
                    } catch (NumberFormatException exception) {
                        throw new JsonParseException("Invalid animation timestamp '" + entry.getKey() + "'", exception);
                    }

                    sortedFrames.put(timestamp, entry.getValue());
                    lastTimestamp = Math.max(lastTimestamp, timestamp);
                }

                for (Map.Entry<Double, JsonElement> entry : sortedFrames.entrySet()) {
                    sourceFrames.add(new TimedKeyframe(entry.getKey(), entry.getValue()));
                }

                forceLinear = sourceFrames.size() == 1;
            }
        } else {
            throw new JsonParseException("Unknown animation keyframe track type: " + rawTrack);
        }

        List<FrameTriplet> bakedFrames = new ArrayList<>();

        for (TimedKeyframe sourceFrame : sourceFrames) {
            bakeKeyframe(sourceFrame.timestamp, sourceFrame.value, transform, forceLinear, bakedFrames, mathParser);
        }

        if (bakedFrames.isEmpty()) return TrackResult.EMPTY;

        Keyframe[] xFrames = extractFrames(bakedFrames, 0);
        Keyframe[] yFrames = extractFrames(bakedFrames, 1);
        Keyframe[] zFrames = extractFrames(bakedFrames, 2);

        applyEasingMetadata(xFrames, mathParser);
        applyEasingMetadata(yFrames, mathParser);
        applyEasingMetadata(zFrames, mathParser);

        return new TrackResult(new KeyframeStack(xFrames, yFrames, zFrames), lastTimestamp);
    }

    private void bakeKeyframe(double timestamp, JsonElement rawKeyframe, AnimationPoint.Transform transform,
        boolean forceLinear, List<FrameTriplet> bakedFrames, MathParser mathParser) {
        JsonElement values = rawKeyframe;
        JsonElement pre = null;
        JsonElement post = null;
        String easingName = null;
        JsonArray easingArguments = null;

        if (rawKeyframe.isJsonObject()) {
            JsonObject object = rawKeyframe.getAsJsonObject();

            values = object.get("vector");
            pre = object.get("pre");
            post = object.get("post");

            if (object.has("lerp_mode")) easingName = object.get("lerp_mode")
                .getAsString();
            else if (object.has("easing")) easingName = object.get("easing")
                .getAsString();

            if (object.has("easingArgs") && object.get("easingArgs")
                .isJsonArray()) easingArguments = object.getAsJsonArray("easingArgs");
        }

        if (pre != null && !pre.isJsonNull())
            bakeKeyframe(timestamp - KEYFRAME_EPSILON, pre, transform, forceLinear, bakedFrames, mathParser);

        if (values != null && !values.isJsonNull()) {
            MathValue[] vector = parseVector(values, transform, mathParser);
            MathValue[] easingArgs = parseEasingArguments(easingArguments, mathParser);
            EasingType easingType = easingName == null || forceLinear ? EasingType.LINEAR
                : EasingType.fromString(easingName);
            FrameTriplet previous = bakedFrames.isEmpty() ? null : bakedFrames.get(bakedFrames.size() - 1);
            double previousTime = previous == null ? 0 : previous.timestamp;
            double length = timestamp - previousTime;
            MathValue fromX = previous == null ? vector[0] : previous.x.endValue();
            MathValue fromY = previous == null ? vector[1] : previous.y.endValue();
            MathValue fromZ = previous == null ? vector[2] : previous.z.endValue();

            bakedFrames.add(
                new FrameTriplet(
                    timestamp,
                    new Keyframe(timestamp, length, fromX, vector[0], easingType, easingArgs),
                    new Keyframe(timestamp, length, fromY, vector[1], easingType, easingArgs),
                    new Keyframe(timestamp, length, fromZ, vector[2], easingType, easingArgs)));
        }

        if (post != null && !post.isJsonNull())
            bakeKeyframe(timestamp + KEYFRAME_EPSILON, post, transform, forceLinear, bakedFrames, mathParser);
    }

    private MathValue[] parseVector(JsonElement vector, AnimationPoint.Transform transform, MathParser mathParser) {
        DoubleOrString[] components = new DoubleOrString[3];

        if (vector.isJsonArray()) {
            JsonArray array = vector.getAsJsonArray();

            if (array.size() != 1 && array.size() != 3)
                throw new JsonParseException("Animation vector must contain either one or three values: " + vector);

            if (array.size() == 1) {
                DoubleOrString value = parseDoubleOrString(array.get(0));

                components[0] = value;
                components[1] = value;
                components[2] = value;
            } else {
                for (int i = 0; i < 3; i++) {
                    components[i] = parseDoubleOrString(array.get(i));
                }
            }
        } else if (vector.isJsonPrimitive()) {
            DoubleOrString value = parseDoubleOrString(vector);

            components[0] = value;
            components[1] = value;
            components[2] = value;
        } else {
            throw new JsonParseException("Animation vector must be a number, expression, or array: " + vector);
        }

        MathValue[] values = new MathValue[] { mathParser.compileDoubleOrString(components[0]),
            mathParser.compileDoubleOrString(components[1]), mathParser.compileDoubleOrString(components[2]) };

        if (transform == AnimationPoint.Transform.ROTATION) {
            values[0] = mathParser.wrap(values[0], Negative::new, value -> new ScaledValue(value, DEG_TO_RAD));
            values[1] = mathParser.wrap(values[1], Negative::new, value -> new ScaledValue(value, DEG_TO_RAD));
            values[2] = mathParser.wrap(values[2], value -> new ScaledValue(value, DEG_TO_RAD));
        }

        return values;
    }

    private MathValue[] parseEasingArguments(JsonArray arguments, MathParser mathParser) {
        if (arguments == null) return new MathValue[0];

        MathValue[] values = new MathValue[arguments.size()];

        for (int i = 0; i < values.length; i++) {
            values[i] = mathParser.compileDoubleOrString(parseDoubleOrString(arguments.get(i)));
        }

        return values;
    }

    private DoubleOrString parseDoubleOrString(JsonElement element) {
        if (!element.isJsonPrimitive())
            throw new JsonParseException("Expected a number or Molang expression: " + element);

        if (element.getAsJsonPrimitive()
            .isString()) return DoubleOrString.of(element.getAsString());

        return DoubleOrString.of(element.getAsDouble());
    }

    private Animation.KeyframeMarkers bakeMarkers(JsonObject definition) {
        List<SoundKeyframeData> sounds = new ArrayList<>();
        List<ParticleKeyframeData> particles = new ArrayList<>();
        List<CustomInstructionKeyframeData> instructions = new ArrayList<>();
        JsonObject soundDefinitions = optionalObject(definition, "sound_effects");
        JsonObject particleDefinitions = optionalObject(definition, "particle_effects");
        JsonObject timelineDefinitions = optionalObject(definition, "timeline");

        if (soundDefinitions != null) {
            for (Map.Entry<String, JsonElement> entry : soundDefinitions.entrySet()) {
                JsonObject effect = entry.getValue()
                    .getAsJsonObject();

                sounds.add(
                    new SoundKeyframeData(
                        parseTimestamp(entry.getKey()),
                        requireString(effect, "effect"),
                        optionalString(effect, "locator")));
            }
        }

        if (particleDefinitions != null) {
            for (Map.Entry<String, JsonElement> entry : particleDefinitions.entrySet()) {
                JsonObject effect = entry.getValue()
                    .getAsJsonObject();

                particles.add(
                    new ParticleKeyframeData(
                        parseTimestamp(entry.getKey()),
                        requireString(effect, "effect"),
                        optionalString(effect, "locator")));
            }
        }

        if (timelineDefinitions != null) {
            for (Map.Entry<String, JsonElement> entry : timelineDefinitions.entrySet()) {
                instructions.add(
                    new CustomInstructionKeyframeData(
                        parseTimestamp(entry.getKey()),
                        entry.getValue()
                            .getAsString()));
            }
        }

        Comparator<com.geckolib.cache.animation.keyframeevent.KeyFrameData> byTime = Comparator
            .comparingDouble(com.geckolib.cache.animation.keyframeevent.KeyFrameData::getTime);

        Collections.sort(sounds, byTime);
        Collections.sort(particles, byTime);
        Collections.sort(instructions, byTime);

        return new Animation.KeyframeMarkers(
            sounds.toArray(new SoundKeyframeData[0]),
            particles.toArray(new ParticleKeyframeData[0]),
            instructions.toArray(new CustomInstructionKeyframeData[0]));
    }

    private static Keyframe[] extractFrames(List<FrameTriplet> frames, int axis) {
        Keyframe[] result = new Keyframe[frames.size()];

        for (int i = 0; i < frames.size(); i++) {
            FrameTriplet frame = frames.get(i);

            result[i] = axis == 0 ? frame.x : axis == 1 ? frame.y : frame.z;
        }

        return result;
    }

    private static void applyEasingMetadata(Keyframe[] keyframes, MathParser mathParser) {
        for (int i = 0; i < keyframes.length; i++) {
            keyframes[i].easingType()
                .modifyKeyframes(keyframes, i, mathParser);
        }
    }

    private static boolean isKeyframeObject(JsonObject object) {
        return object.has("vector") || object.has("pre")
            || object.has("post")
            || object.has("lerp_mode")
            || object.has("easing");
    }

    private static double parseTimestamp(String timestamp) {
        try {
            return Double.parseDouble(timestamp);
        } catch (NumberFormatException exception) {
            throw new JsonParseException("Invalid animation timestamp '" + timestamp + "'", exception);
        }
    }

    private static JsonObject requireObject(JsonObject object, String member) {
        JsonObject value = optionalObject(object, member);

        if (value == null) throw new JsonParseException("Missing JSON object '" + member + "'");

        return value;
    }

    private static JsonObject optionalObject(JsonObject object, String member) {
        JsonElement value = object.get(member);

        if (value == null || value.isJsonNull()) return null;

        if (!value.isJsonObject()) throw new JsonParseException("JSON member '" + member + "' must be an object");

        return value.getAsJsonObject();
    }

    private static String requireString(JsonObject object, String member) {
        String value = optionalString(object, member);

        if (value == null) throw new JsonParseException("Missing JSON string '" + member + "'");

        return value;
    }

    private static String optionalString(JsonObject object, String member) {
        JsonElement value = object.get(member);

        return value == null || value.isJsonNull() ? null : value.getAsString();
    }

    private static final class TimedKeyframe {

        private final double timestamp;
        private final JsonElement value;

        private TimedKeyframe(double timestamp, JsonElement value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    private static final class FrameTriplet {

        private final double timestamp;
        private final Keyframe x;
        private final Keyframe y;
        private final Keyframe z;

        private FrameTriplet(double timestamp, Keyframe x, Keyframe y, Keyframe z) {
            this.timestamp = timestamp;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static final class TrackResult {

        private static final TrackResult EMPTY = new TrackResult(KeyframeStack.EMPTY, 0);

        private final KeyframeStack stack;
        private final double lastTimestamp;

        private TrackResult(KeyframeStack stack, double lastTimestamp) {
            this.stack = stack;
            this.lastTimestamp = lastTimestamp;
        }
    }
}
