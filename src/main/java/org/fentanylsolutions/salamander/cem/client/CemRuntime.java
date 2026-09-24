package org.fentanylsolutions.salamander.cem.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelDragon;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.salamander.cem.model.CemModel;
import org.fentanylsolutions.salamander.config.CemConfig;

/** Render-thread scopes keep nested entity, tile, item and multipass draws isolated. */
public final class CemRuntime {

    private static final Map<ModelBase, CemBinding> BINDINGS = new IdentityHashMap<>();
    private static Subject subject;
    private static Draw draw;
    private static ResourceLocation texture;
    private static boolean rendering;

    private CemRuntime() {}

    public static void texture(ResourceLocation value) {
        texture = value;
    }

    public static void recaptureOrigin() {
        if (draw != null) draw.frame.recapture();
    }

    /** Legacy dragons calculate and reuse bones inline. Capture all named poses before evaluating expressions. */
    public static boolean collectDragonPose(boolean collect) {
        if (draw == null || draw.entry == null || !(draw.binding.nativeModel instanceof ModelDragon)) return false;
        draw.collecting = collect;
        draw.calls.clear();
        return true;
    }

    public static boolean modernBat() {
        if (draw == null || draw.entry == null) return false;
        CemModel.Node feet = draw.entry.model.originalParts.get("feet");
        return feet != null && !feet.vanillaGeometry;
    }

    public static boolean drawing(ModelBase model) {
        return draw != null && draw.entry != null && draw.binding.nativeModel == model;
    }

    public static ResourceLocation texture() {
        return texture;
    }

    public static Object subject() {
        return subject == null ? null : subject.object;
    }

    public static boolean itemFrame() {
        for (Subject scope = subject; scope != null; scope = scope.parent)
            if (scope.object instanceof net.minecraft.entity.item.EntityItemFrame) return true;
        return false;
    }

    public static boolean replaced(String name) {
        if (subject == null || subject.primary == null) return false;
        CemModel.Node node = subject.primary.model.originalParts.get(name);
        return node != null && !node.vanillaGeometry;
    }

    public static void clear() {
        BINDINGS.clear();
        subject = null;
        draw = null;
    }

    public static Subject enter(Object object, float partialTicks) {
        Subject previous = subject;
        subject = new Subject(object, partialTicks, previous);
        return previous;
    }

    public static void leave(Subject previous) {
        subject = previous;
    }

    public static double property(String name, double fallback) {
        if (subject == null || subject.primaryDraw == null) return fallback;
        double value = subject.primaryDraw.instance.renderProperties.getOrDefault(name, fallback);
        return Double.isFinite(value) && (!name.equals("render.shadow_size") || value >= 0) ? value : fallback;
    }

    public static CemResources.Entry active() {
        return subject == null ? null : subject.primary;
    }

    public static Draw begin(ModelBase model, Entity entity, float limb, float speed, float age, float yaw,
        float pitch) {
        return beginNamed(model, entity, limb, speed, age, yaw, pitch, null);
    }

    public static Draw beginNamed(ModelBase model, Entity entity, float limb, float speed, float age, float yaw,
        float pitch, List<String> candidates) {
        Draw previous = draw;
        if (previous != null && previous.binding.nativeModel == model) return previous;
        draw = null;
        if (!CemConfig.enabled || !(model instanceof CemModelParts)) return previous;
        Object object = entity != null ? entity : subject == null ? model : subject.object;
        CemBinding binding = BINDINGS
            .computeIfAbsent(model, m -> new CemBinding(m, ((CemModelParts) m).salamander$cemParts()));
        CemResources.Selection selected = CemResources.INSTANCE
            .select(candidates == null ? CemTargets.candidates(object, model, texture) : candidates, binding, object);
        CemResources.Entry entry = selected == null ? null : selected.entry;
        if (entry != null || CemResources.INSTANCE.hasTargets(CemTargets.extraCandidates(object, model))) {
            draw = new Draw(
                binding,
                entry,
                object,
                subject == null ? 0 : subject.partialTicks,
                limb,
                speed,
                age,
                yaw,
                pitch,
                texture);
            if (selected != null) draw.inputs.put("rule_index", (double) selected.rule);
            if (subject != null) {
                subject.draws.put(model, draw);
                if (subject.primary == null && entry != null) {
                    subject.primary = entry;
                    subject.primaryDraw = draw;
                }
            }
        }
        return previous;
    }

    public static void end(Draw previous, float scale) {
        Draw current = draw;
        try {
            if (current != null && current != previous) {
                if (!current.extras) {
                    current.extras = true;
                    current.finish(scale);
                }
                for (Draw layer : current.layers.values()) if (layer != null && !layer.extras) {
                    layer.extras = true;
                    layer.finish(scale);
                }
            }
        } finally {
            draw = previous;
        }
    }

    public static boolean part(ModelBase model, ModelRenderer part, float scale, boolean rotationOrder) {
        if (rendering) return false;
        if (armorPart(model, part, scale, rotationOrder)) return true;
        if (draw == null && subject != null) {
            Draw previous = draw;
            Draw cached = subject.draws.get(model);
            if (cached == null && subject.object instanceof net.minecraft.tileentity.TileEntity) {
                begin(model, null, 0, 0, 0, 0, 0);
                cached = draw;
            } else draw = cached;
            if (cached == null) return false;
            try {
                boolean handled = part(model, part, scale, rotationOrder);
                if (!cached.extras) {
                    cached.extras = true;
                    cached.finish(scale);
                }
                return handled;
            } finally {
                draw = previous;
            }
        }
        if (draw == null || draw.binding.nativeModel != model) return false;
        Draw selected = draw;
        try {
            java.util.List<String> occurrences = draw.binding.occurrences.get(part);
            if (occurrences == null) return false;
            int occurrence = draw.calls.getOrDefault(part, 0);
            draw.calls.put(part, occurrence + 1);
            String name = occurrences.get(Math.min(occurrence, occurrences.size() - 1));
            if (draw.collecting) {
                draw.collectPose(name);
                return true;
            }
            List<String> layerNames = CemTargets.partCandidates(draw.object, model, name);
            if (!layerNames.isEmpty()) {
                String key = layerNames.get(0);
                if (!draw.layers.containsKey(key)) {
                    CemResources.Selection layer = CemResources.INSTANCE.select(layerNames, draw.binding, draw.object);
                    Draw layerDraw = layer == null ? null : new Draw(draw, layer);
                    draw.layers.put(key, layerDraw);
                }
                Draw layerDraw = draw.layers.get(key);
                if (layerDraw != null) {
                    selected = layerDraw;
                    if (!selected.extras) {
                        selected.extras = true;
                        selected.finish(scale);
                    }
                }
            }
            List<String> overlayNames = CemTargets.overlayCandidates(draw.object, model, draw.texture);
            Draw overlay = null;
            if (!overlayNames.isEmpty()) {
                String key = overlayNames.get(0);
                if (!draw.layers.containsKey(key)) {
                    CemResources.Selection layer = CemResources.INSTANCE
                        .select(overlayNames, draw.binding, draw.object);
                    draw.layers.put(key, layer == null ? null : new Draw(draw, layer));
                }
                overlay = draw.layers.get(key);
            }
            boolean saddle = name.contains("saddle") || name.startsWith("$group:saddle:");
            boolean handled = overlay != null && saddle || renderPart(selected, name, scale, rotationOrder);
            if (overlay != null) renderPart(overlay, name, scale, rotationOrder);
            return handled;
        } catch (Exception exception) {
            if (selected.entry != null) CemResources.INSTANCE.fail(selected.entry.model, exception);
            return false;
        } finally {
            rendering = false;
        }
    }

    private static boolean armorPart(ModelBase model, ModelRenderer part, float scale, boolean rotationOrder) {
        if (!(model instanceof net.minecraft.client.model.ModelBiped) || subject == null
            || subject.primaryDraw == null
            || texture == null
            || !texture.getResourcePath()
                .startsWith("textures/models/armor/"))
            return false;
        Draw body = subject.primaryDraw;
        if (!(body.binding.nativeModel instanceof net.minecraft.client.model.ModelBiped)
            || !(model instanceof CemModelParts)) return false;
        CemBinding binding = BINDINGS
            .computeIfAbsent(model, m -> new CemBinding(m, ((CemModelParts) m).salamander$cemParts()));
        CemModel.Node node = body.entry.model.originalParts.get(binding.names.get(part));
        if (node == null) return false;
        if (!part.showModel || part.isHidden) return true;
        body.evaluate();
        org.lwjgl.opengl.GL11.glPushMatrix();
        rendering = true;
        try {
            body.frame.insertRoot(body.entry.model.root, body.instance.pose, scale);
            CemGeometry.transform(node, body.instance.pose, scale, rotationOrder);
            for (net.minecraft.client.model.ModelBox box : part.cubeList)
                box.render(net.minecraft.client.renderer.Tessellator.instance, scale);
            if (part.childModels != null) for (ModelRenderer child : part.childModels) child.render(scale);
        } finally {
            rendering = false;
            org.lwjgl.opengl.GL11.glPopMatrix();
        }
        return true;
    }

    private static boolean renderPart(Draw selected, String name, float scale, boolean rotationOrder) {
        if (selected.entry == null) return false;
        selected.frame.capture();
        if (selected.frame.deferred() && !selected.extras) {
            selected.extras = true;
            selected.finish(scale);
        }
        selected.evaluate();
        CemModel.Node node = selected.entry.model.originalParts.get(name);
        String owner = selected.binding.groups.get(name);
        if (owner != null) {
            CemModel.Node group = selected.entry.model.originalParts.get(owner);
            if (group != null && !group.vanillaGeometry) return true;
        }
        if (node == null) return false;
        if (!selected.reachable.contains(node) || selected.instance.pose[9] == 0) return true;
        selected.binding.refresh(node, selected.instance, selected.object, selected.parts);
        rendering = true;
        selected.entry.geometry.renderNativePart(
            node,
            selected.instance.pose,
            selected.parts,
            scale,
            selected.texture,
            rotationOrder,
            selected.frame,
            selected.binding.bakedHorseBaby(node));
        return true;
    }

    /** Move legacy accessories by the animated bone's delta, preserving their native placement. */
    public static void accessory(String name, float scale) {
        Draw current = subject == null ? null : subject.primaryDraw;
        if (current == null || current.entry == null) return;
        CemModel.Node node = current.entry.model.originalParts.get(name);
        ModelRenderer nativePart = current.parts.get(name);
        if (node == null || nativePart == null) return;
        current.evaluate();
        current.frame.insertRoot(current.entry.model.root, current.instance.pose, scale);
        CemGeometry.transform(node, current.instance.pose, scale, false);
        org.lwjgl.opengl.GL11.glRotatef((float) -Math.toDegrees(nativePart.rotateAngleX), 1, 0, 0);
        org.lwjgl.opengl.GL11.glRotatef((float) -Math.toDegrees(nativePart.rotateAngleY), 0, 1, 0);
        org.lwjgl.opengl.GL11.glRotatef((float) -Math.toDegrees(nativePart.rotateAngleZ), 0, 0, 1);
        org.lwjgl.opengl.GL11.glTranslatef(
            -nativePart.rotationPointX * scale,
            -nativePart.rotationPointY * scale,
            -nativePart.rotationPointZ * scale);
    }

    public static boolean post(ModelBase model, ModelRenderer part, float scale) {
        Draw current = subject == null ? null : subject.draws.get(model);
        if (rendering || current == null || current.entry == null) return false;
        try {
            current.evaluate();
            CemModel.Node node = current.entry.model.originalParts.get(current.binding.names.get(part));
            if (node == null) return false;
            current.frame.insertRoot(current.entry.model.root, current.instance.pose, scale);
            String name = current.binding.names.get(part);
            String attachment = "left_arm".equals(name) ? "left_handheld_item"
                : "right_arm".equals(name) ? "right_handheld_item" : null;
            if (attachment == null || !current.entry.geometry.attachment(attachment, current.instance.pose, scale))
                CemGeometry.transform(node, current.instance.pose, scale, false);
            return true;
        } catch (Exception exception) {
            CemResources.INSTANCE.fail(current.entry.model, exception);
            return false;
        }
    }

    public static final class Subject {

        final Object object;
        final float partialTicks;
        final Subject parent;
        final Map<ModelBase, Draw> draws = new IdentityHashMap<>();
        CemResources.Entry primary;
        Draw primaryDraw;

        Subject(Object object, float partialTicks, Subject parent) {
            this.object = object;
            this.partialTicks = partialTicks;
            this.parent = parent;
        }
    }

    public static final class Draw {

        final CemBinding binding;
        final Map<String, ModelRenderer> parts;
        final CemResources.Entry entry;
        final CemModel.Instance instance;
        final Map<String, Double> inputs;
        final ResourceLocation texture;
        final Object object;
        final Map<String, Draw> layers = new HashMap<>();
        final CemRenderFrame frame;
        final Set<CemModel.Node> reachable = Collections.newSetFromMap(new IdentityHashMap<>());
        final Map<ModelRenderer, Integer> calls = new IdentityHashMap<>();
        boolean evaluated, extras, collecting;
        final Map<String, float[]> collected = new HashMap<>();

        Draw(CemBinding binding, CemResources.Entry entry, Object object, float partial, float limb, float speed,
            float age, float yaw, float pitch, ResourceLocation texture) {
            this.binding = binding;
            this.parts = binding.parts(object);
            this.entry = entry;
            this.texture = texture;
            this.object = object;
            instance = entry == null ? null : CemClient.instance(object, entry.model);
            ModelBase nativeModel = binding.nativeModel;
            String type = nativeModel.getClass()
                .getName();
            frame = new CemRenderFrame(
                nativeModel instanceof ModelDragon || nativeModel instanceof ModelEnderCrystal
                    || type.endsWith(".ModelNewBoat")
                    || type.endsWith(".ModelRaft"));
            inputs = CemClient.inputs(object, partial, limb, speed, age, yaw, pitch);
            if (object instanceof net.minecraft.entity.EntityLivingBase && !CemItemContext.hand()
                && (nativeModel instanceof ModelSkeletonHead || type.endsWith(".ModelHead")))
                inputs.put("is_on_head", 1d);
            if (entry != null) collect(entry.model.root);
            if (entry != null && object instanceof Entity) {
                net.minecraft.client.renderer.entity.Render renderer = net.minecraft.client.renderer.entity.RenderManager.instance
                    .getEntityRenderObject((Entity) object);
                if (renderer instanceof CemRenderProperties && entry.model.shadowSize < 0)
                    inputs.put("render.shadow_size", (double) ((CemRenderProperties) renderer).salamander$shadowSize());
            }
        }

        Draw(Draw parent, CemResources.Selection selection) {
            binding = parent.binding;
            parts = parent.parts;
            entry = selection.entry;
            object = parent.object;
            frame = parent.frame;
            ResourceLocation layerTexture = parent.texture;
            if (entry.model.source.getResourcePath()
                .contains("_saddle")) {
                ResourceLocation saddle = new ResourceLocation(
                    "minecraft",
                    "textures/entity/equipment/" + CemTargets.target(object) + "_saddle/saddle.png");
                if (CemResources.INSTANCE.exists(saddle)) layerTexture = saddle;
            }
            texture = layerTexture;
            instance = CemClient.instance(object, entry.model);
            inputs = new HashMap<>(parent.inputs);
            inputs.put("rule_index", (double) selection.rule);
            collect(entry.model.root);
        }

        private void collect(CemModel.Node node) {
            reachable.add(node);
            for (CemModel.Node child : node.children) collect(child);
        }

        void collectPose(String name) {
            ModelRenderer part = parts.get(name);
            if (part != null) collected.put(name, binding.transform(name, part));
            for (Map.Entry<String, String> child : binding.parents.entrySet()) if (child.getValue()
                .equals(name)) collectPose(child.getKey());
        }

        void evaluate() {
            if (entry != null && !evaluated) {
                instance.evaluate(CemClient.frame(), inputs, pose -> {
                    binding.pose(entry.model, pose, object, parts);
                    for (Map.Entry<String, float[]> captured : collected.entrySet()) {
                        CemModel.Node node = entry.model.originalParts.get(captured.getKey());
                        if (node != null)
                            for (int i = 0; i < 6; i++) pose[node.index * CemModel.STRIDE + i] = captured.getValue()[i];
                    }
                });
                evaluated = true;
            }
        }

        void finish(float scale) {
            if (entry == null) return;
            try {
                evaluate();
                rendering = true;
                entry.geometry.renderRootExtras(instance.pose, parts, scale, texture, frame);
            } catch (Exception exception) {
                CemResources.INSTANCE.fail(entry.model, exception);
            } finally {
                rendering = false;
            }
        }
    }
}
