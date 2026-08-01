package com.geckolib.animation;

import java.util.LinkedHashMap;
import java.util.Map;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.object.EasingType;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ControllerState;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.loading.math.MolangContext;

/**
 * Headless GeckoLib animation controller.
 *
 * <p>
 * This owns deterministic timeline state. Client transition blending will use the same state once the model renderer
 * is available.
 */
public class AnimationController<T extends GeoAnimatable> {

    private final String name;
    private final AnimationStateHandler<T> stateHandler;
    private final Map<String, RawAnimation> triggerableAnimations = new LinkedHashMap<>();

    private int transitionTicks;
    private double animationSpeed = 1;
    private boolean handlesTriggeredAnimations;
    private EasingType easingOverride;
    private PlayState playState = PlayState.STOP;
    private RawAnimation currentRawAnimation;
    private RawAnimation initializedRawAnimation;
    private AnimationPoint animationPoint;
    private int stageIndex;
    private double stageTime;
    private double lastAnimatableAge = Double.NaN;
    private boolean playingTriggeredAnimation;
    private boolean finished;

    public AnimationController(AnimationStateHandler<T> stateHandler) {
        this("Default", 0, stateHandler);
    }

    public AnimationController(String name, AnimationStateHandler<T> stateHandler) {
        this(name, 0, stateHandler);
    }

    public AnimationController(String name, int transitionTicks, AnimationStateHandler<T> stateHandler) {
        this.name = name;
        this.transitionTicks = transitionTicks;
        this.stateHandler = stateHandler;
    }

    public String getName() {
        return this.name;
    }

    public PlayState getPlayState() {
        return this.playState;
    }

    public int getTransitionTicks() {
        return this.transitionTicks;
    }

    public double getAnimationSpeed() {
        return this.animationSpeed;
    }

    public double getCurrentTimelineTime() {
        return this.animationPoint == null ? -1 : this.stageTime;
    }

    public double getCurrentAnimationTime() {
        return this.animationPoint == null ? 0 : this.animationPoint.animTime();
    }

    public AnimationPoint getCurrentAnimationPoint() {
        return this.animationPoint;
    }

    public RawAnimation getCurrentRawAnimation() {
        return this.currentRawAnimation;
    }

    public boolean isPlayingTriggeredAnimation() {
        return this.playingTriggeredAnimation && this.animationPoint != null;
    }

    public boolean hasAnimationFinished() {
        return this.finished;
    }

    public boolean isAnimatingBones() {
        return this.animationPoint != null;
    }

    public AnimationController<T> setAnimationSpeed(double speed) {
        this.animationSpeed = speed;

        return this;
    }

    public AnimationController<T> setTransitionTicks(int ticks) {
        this.transitionTicks = ticks;

        return this;
    }

    public AnimationController<T> setOverrideEasingType(EasingType easingType) {
        this.easingOverride = easingType;

        return this;
    }

    public AnimationController<T> receiveTriggeredAnimations() {
        this.handlesTriggeredAnimations = true;

        return this;
    }

    public AnimationController<T> triggerableAnim(String name, RawAnimation animation) {
        this.triggerableAnimations.put(name, animation);

        return this;
    }

    public void setAnimation(RawAnimation rawAnimation) {
        if (rawAnimation == null || rawAnimation.getStageCount() == 0)
            throw new IllegalArgumentException("AnimationController cannot play an empty animation");

        if (rawAnimation.equals(this.currentRawAnimation)) return;

        this.currentRawAnimation = rawAnimation;
        this.playingTriggeredAnimation = false;
        this.finished = false;
    }

    public boolean triggerAnimation(String animationName) {
        RawAnimation animation = this.triggerableAnimations.get(animationName);

        if (animation == null) return false;

        this.currentRawAnimation = animation;
        this.initializedRawAnimation = null;
        this.playingTriggeredAnimation = true;
        this.playState = PlayState.CONTINUE;
        this.finished = false;

        return true;
    }

    public boolean stopTriggeredAnimation() {
        if (!this.playingTriggeredAnimation) return false;

        reset();

        return true;
    }

    public void setAnimationTime(double animationTime) {
        if (animationTime < 0) throw new IllegalArgumentException("Animation time cannot be negative");

        this.stageTime = animationTime;

        if (this.animationPoint != null) this.animationPoint = this.animationPoint.createNext(animationTime);
    }

    public void reset() {
        this.playState = PlayState.STOP;
        this.currentRawAnimation = null;
        this.initializedRawAnimation = null;
        this.animationPoint = null;
        this.stageIndex = 0;
        this.stageTime = 0;
        this.playingTriggeredAnimation = false;
        this.finished = false;
    }

    /** Advance this controller using an animatable age measured in Minecraft ticks. */
    public AnimationPoint tick(T animatable, AnimatableManager<T> manager, BakedAnimations animations,
        double animatableAge, MolangContext molangContext) {
        MolangContext context = molangContext == null ? MolangContext.EMPTY : molangContext;
        double tickDelta = calculateTickDelta(animatableAge);

        if (!this.playingTriggeredAnimation || this.handlesTriggeredAnimations) {
            this.playState = this.stateHandler.handle(new AnimationTest<>(animatable, manager, this, context));
        }

        if (this.playState == PlayState.STOP) {
            this.initializedRawAnimation = null;
            this.animationPoint = null;
            this.stageTime = 0;
            this.finished = false;

            return null;
        }

        if (this.currentRawAnimation == null) return null;

        if (this.initializedRawAnimation != this.currentRawAnimation) {
            this.initializedRawAnimation = this.currentRawAnimation;
            this.stageIndex = 0;
            this.stageTime = 0;
            this.finished = false;
            initializeCurrentStage(animations);
        }

        if (this.animationPoint == null) return null;

        if (this.playState != PlayState.PAUSE && !this.finished)
            advanceTimeline(tickDelta / 20d * this.animationSpeed, animations);

        return this.animationPoint;
    }

    public Map<String, BoneSnapshot> evaluateCurrentPose(MolangContext molangContext) {
        if (this.animationPoint == null) return java.util.Collections.emptyMap();

        ControllerState state = new ControllerState(molangContext).setAnimationTime(this.animationPoint.animTime());

        return AnimationProcessor.evaluate(this.animationPoint, state);
    }

    private double calculateTickDelta(double animatableAge) {
        if (Double.isNaN(this.lastAnimatableAge)) {
            this.lastAnimatableAge = animatableAge;
            return 0;
        }

        double delta = animatableAge - this.lastAnimatableAge;

        this.lastAnimatableAge = animatableAge;

        if (delta < -1) return 0;

        return delta;
    }

    private void initializeCurrentStage(BakedAnimations animations) {
        while (this.stageIndex < this.currentRawAnimation.getStageCount()) {
            RawAnimation.Stage rawStage = this.currentRawAnimation.getAnimationStages()
                .get(this.stageIndex);
            Animation animation = resolveAnimation(rawStage, animations);

            if (animation != null) {
                this.animationPoint = AnimationPoint.createFor(animation, this.easingOverride, rawStage.loopType(), 0);
                return;
            }

            this.stageIndex++;
        }

        this.animationPoint = null;
        this.finished = true;
    }

    private void advanceTimeline(double timeAdvanced, BakedAnimations animations) {
        if (timeAdvanced == 0 || this.animationPoint == null) return;

        this.stageTime += timeAdvanced;

        while (this.animationPoint != null) {
            Animation animation = this.animationPoint.animation();
            double length = animation.length();

            if (this.stageTime < length) {
                this.animationPoint = this.animationPoint.createNext(this.stageTime);
                return;
            }

            LoopType loopType = this.animationPoint.loopType() == LoopType.DEFAULT ? animation.loopType()
                : this.animationPoint.loopType();

            if (loopType == LoopType.LOOP || loopType.shouldPlayAgain(animation)) {
                this.stageTime = length == 0 ? 0 : this.stageTime % length;
                this.animationPoint = AnimationPoint
                    .createFor(animation, this.easingOverride, loopType, this.stageTime);
                return;
            }

            if (loopType == LoopType.HOLD_ON_LAST_FRAME) {
                this.stageTime = length;
                this.animationPoint = this.animationPoint.createNext(length);
                this.finished = true;
                return;
            }

            this.stageTime -= length;
            this.stageIndex++;

            if (this.stageIndex >= this.currentRawAnimation.getStageCount()) {
                this.stageTime = length;
                this.animationPoint = this.animationPoint.createNext(length);
                this.finished = true;
                return;
            }

            initializeCurrentStage(animations);
        }
    }

    private Animation resolveAnimation(RawAnimation.Stage stage, BakedAnimations animations) {
        if (RawAnimation.Stage.WAIT.equals(stage.animationName()))
            return Animation.generateWaitAnimation(stage.waitTicks());

        return animations.getAnimation(stage.animationName());
    }

    @FunctionalInterface
    public interface AnimationStateHandler<A extends GeoAnimatable> {

        PlayState handle(AnimationTest<A> animationTest);
    }
}
