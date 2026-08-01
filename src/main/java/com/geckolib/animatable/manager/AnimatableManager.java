package com.geckolib.animatable.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import com.geckolib.constant.dataticket.DataTicket;

/** Controller and instance-data collection for one animatable instance. */
public final class AnimatableManager<T extends GeoAnimatable> {

    private final Map<String, AnimationController<T>> animationControllers;
    private final Map<DataTicket<?>, Object> animatableInstanceData = new LinkedHashMap<>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public AnimatableManager(GeoAnimatable animatable) {
        ControllerRegistrar registrar = new ControllerRegistrar();

        animatable.registerControllers(registrar);
        this.animationControllers = registrar.build();
    }

    public Map<String, AnimationController<T>> getAnimationControllers() {
        return this.animationControllers;
    }

    public void addController(AnimationController<T> controller) {
        this.animationControllers.put(controller.getName(), controller);
    }

    public void removeController(String name) {
        this.animationControllers.remove(name);
    }

    public <D> void setAnimatableData(DataTicket<D> dataTicket, D data) {
        this.animatableInstanceData.put(dataTicket, data);
    }

    @SuppressWarnings("unchecked")
    public <D> D getAnimatableData(DataTicket<D> dataTicket) {
        return (D) this.animatableInstanceData.get(dataTicket);
    }

    public boolean tryTriggerAnimation(String animationName) {
        for (AnimationController<T> controller : this.animationControllers.values()) {
            if (controller.triggerAnimation(animationName)) return true;
        }

        return false;
    }

    public boolean tryTriggerAnimation(String controllerName, String animationName) {
        AnimationController<T> controller = this.animationControllers.get(controllerName);

        return controller != null && controller.triggerAnimation(animationName);
    }

    public static final class ControllerRegistrar {

        private final List<AnimationController<? extends GeoAnimatable>> controllers = new ArrayList<>();

        public ControllerRegistrar add(AnimationController<?>... controllers) {
            Collections.addAll(this.controllers, controllers);

            return this;
        }

        public ControllerRegistrar add(AnimationController<?> controller) {
            this.controllers.add(controller);

            return this;
        }

        public ControllerRegistrar remove(String name) {
            this.controllers.removeIf(
                controller -> controller.getName()
                    .equals(name));

            return this;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private <A extends GeoAnimatable> Map<String, AnimationController<A>> build() {
            Map<String, AnimationController<A>> result = new LinkedHashMap<>();

            for (AnimationController controller : this.controllers) {
                result.put(controller.getName(), controller);
            }

            return result;
        }
    }
}
