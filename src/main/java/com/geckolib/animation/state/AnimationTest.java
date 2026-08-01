package com.geckolib.animation.state;

import java.util.Objects;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.loading.math.MolangContext;

/** Predicate input for an {@link AnimationController}. */
public final class AnimationTest<T extends GeoAnimatable> {

    private final T animatable;
    private final AnimatableManager<T> manager;
    private final AnimationController<T> controller;
    private final MolangContext molangContext;

    public AnimationTest(T animatable, AnimatableManager<T> manager, AnimationController<T> controller,
        MolangContext molangContext) {
        this.animatable = animatable;
        this.manager = manager;
        this.controller = controller;
        this.molangContext = molangContext;
    }

    public T animatable() {
        return this.animatable;
    }

    public AnimatableManager<T> manager() {
        return this.manager;
    }

    public AnimationController<T> controller() {
        return this.controller;
    }

    public double getMolangValue(String name) {
        return this.molangContext.resolve(name);
    }

    public boolean isMoving() {
        Boolean moving = getData(DataTickets.IS_MOVING);

        return moving != null && moving;
    }

    public <D> boolean hasData(DataTicket<D> dataTicket) {
        return this.manager.getAnimatableData(dataTicket) != null;
    }

    public <D> D getData(DataTicket<D> dataTicket) {
        return this.manager.getAnimatableData(dataTicket);
    }

    public <D> D getDataOrDefault(DataTicket<D> dataTicket, D defaultValue) {
        D value = getData(dataTicket);

        return value == null ? defaultValue : value;
    }

    public void setAnimation(RawAnimation animation) {
        this.controller.setAnimation(animation);
    }

    public PlayState setAndContinue(RawAnimation animation) {
        setAnimation(animation);

        return PlayState.CONTINUE;
    }

    public boolean isCurrentAnimation(RawAnimation animation) {
        return Objects.equals(this.controller.getCurrentRawAnimation(), animation);
    }

    public boolean isCurrentAnimationStage(String name) {
        AnimationPoint point = this.controller.getCurrentAnimationPoint();

        return point != null && point.animation()
            .name()
            .equals(name);
    }

    public void setControllerSpeed(float speed) {
        this.controller.setAnimationSpeed(speed);
    }
}
