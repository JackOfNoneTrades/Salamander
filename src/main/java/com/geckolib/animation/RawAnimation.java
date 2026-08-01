package com.geckolib.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.geckolib.animation.object.LoopType;

/** Ordered builder for one or more animation stages. */
public final class RawAnimation {

    private final List<Stage> animationList = new ArrayList<>();

    private RawAnimation() {}

    public static RawAnimation begin() {
        return new RawAnimation();
    }

    public RawAnimation thenPlay(String animationName) {
        return then(animationName, LoopType.DEFAULT);
    }

    public RawAnimation thenLoop(String animationName) {
        return then(animationName, LoopType.LOOP);
    }

    public RawAnimation thenWait(int ticks) {
        this.animationList.add(new Stage(Stage.WAIT, LoopType.PLAY_ONCE, ticks));

        return this;
    }

    public RawAnimation thenPlayAndHold(String animationName) {
        return then(animationName, LoopType.HOLD_ON_LAST_FRAME);
    }

    public RawAnimation thenPlayXTimes(String animationName, int playCount) {
        for (int i = 0; i < playCount; i++) {
            then(animationName, i == playCount - 1 ? LoopType.DEFAULT : LoopType.PLAY_ONCE);
        }

        return this;
    }

    public RawAnimation then(String animationName, LoopType loopType) {
        this.animationList.add(new Stage(animationName, loopType, 0));

        return this;
    }

    public List<Stage> getAnimationStages() {
        return Collections.unmodifiableList(this.animationList);
    }

    public int getStageCount() {
        return this.animationList.size();
    }

    public static RawAnimation copyOf(RawAnimation other) {
        RawAnimation copy = begin();

        copy.animationList.addAll(other.animationList);

        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RawAnimation && this.animationList.equals(((RawAnimation) obj).animationList);
    }

    @Override
    public int hashCode() {
        return this.animationList.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("RawAnimation{");

        for (int i = 0; i < this.animationList.size(); i++) {
            if (i > 0) builder.append(" -> ");

            builder.append(this.animationList.get(i));
        }

        return builder.append('}')
            .toString();
    }

    public static final class Stage {

        public static final String WAIT = "internal.wait";

        private final String animationName;
        private final LoopType loopType;
        private final int waitTicks;

        public Stage(String animationName, LoopType loopType) {
            this(animationName, loopType, 0);
        }

        public Stage(String animationName, LoopType loopType, int waitTicks) {
            this.animationName = Objects.requireNonNull(animationName, "animationName");
            this.loopType = Objects.requireNonNull(loopType, "loopType");
            this.waitTicks = waitTicks;
        }

        public String animationName() {
            return this.animationName;
        }

        public LoopType loopType() {
            return this.loopType;
        }

        public int waitTicks() {
            return this.waitTicks;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Stage)) return false;

            Stage other = (Stage) obj;

            return this.animationName.equals(other.animationName) && this.loopType == other.loopType
                && this.waitTicks == other.waitTicks;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.animationName, this.loopType, this.waitTicks);
        }

        @Override
        public String toString() {
            return this.animationName;
        }
    }
}
