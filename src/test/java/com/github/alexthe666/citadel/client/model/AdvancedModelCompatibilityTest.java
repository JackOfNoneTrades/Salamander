package com.github.alexthe666.citadel.client.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

import org.junit.After;
import org.junit.Test;

import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.client.model.basic.BasicModelPart;
import com.github.alexthe666.citadel.client.model.container.TabulaModelContainer;

public class AdvancedModelCompatibilityTest {

    private static final float EPSILON = 0.00001F;

    @After
    public void restorePartialTicks() {
        ModelAnimator.setPartialTickSupplierForTests(null);
    }

    @Test
    public void preservesFloatGeometryAndParentHierarchy() {
        TestModel model = new TestModel();
        AdvancedModelBox root = model.root;
        AdvancedModelBox child = model.child;

        root.setTextureOffset(3, 7)
            .addBox(-1.25F, -2.5F, 0.75F, 3.5F, 0, 2.25F, 0.125F, true);
        root.addChild(child);

        assertEquals(1, root.cubeList.size());
        assertEquals(-1.25F, root.cubeList.get(0).posX1, EPSILON);
        assertEquals(2.25F, root.cubeList.get(0).posX2, EPSILON);
        assertEquals(-2.5F, root.cubeList.get(0).posY2, EPSILON);
        assertEquals(3, root.textureOffsetX);
        assertEquals(7, root.textureOffsetY);
        assertSame(root, child.getParent());
        assertSame(child, root.childModels.get(0));
    }

    @Test
    public void restoresDefaultPoseAndAppliesProceduralMotion() {
        TestModel model = new TestModel();
        AdvancedModelBox box = model.root;
        box.setPos(1, 2, 3);
        box.rotateAngleX = 0.25F;
        box.rotateAngleY = -0.5F;
        model.updateDefaultPose();

        box.setPos(7, 8, 9);
        box.rotateAngleX = 2;
        box.rotateAngleY = 3;
        model.resetToDefaultPose();

        assertEquals(1, box.rotationPointX, EPSILON);
        assertEquals(2, box.rotationPointY, EPSILON);
        assertEquals(3, box.rotationPointZ, EPSILON);
        assertEquals(0.25F, box.rotateAngleX, EPSILON);
        assertEquals(-0.5F, box.rotateAngleY, EPSILON);

        float before = box.rotateAngleX;
        model.walk(box, 0.7F, 0.4F, false, 0.2F, 0.1F, 1.3F, 0.8F);
        assertTrue(box.rotateAngleX != before);
    }

    @Test
    public void interpolatesCitadelKeyframesWithRenderPartialTick() {
        TestModel model = new TestModel();
        TestAnimatedEntity entity = new TestAnimatedEntity();
        Animation animation = Animation.create(20);
        entity.animation = animation;
        entity.animationTick = 0;
        ModelAnimator.setPartialTickSupplierForTests(() -> 0.5D);

        ModelAnimator animator = ModelAnimator.create();
        animator.update(entity);
        assertTrue(animator.setAnimation(animation));
        animator.startKeyframe(10);
        animator.rotate(model.root, 1, 0, 0);
        animator.move(model.root, 0, 2, 0);
        animator.endKeyframe();

        float expectedWeight = MathHelper.sin((float) (0.05D * Math.PI / 2D));
        assertEquals(expectedWeight, model.root.rotateAngleX, EPSILON);
        assertEquals(expectedWeight * 2, model.root.rotationPointY, EPSILON);
    }

    @Test
    public void loadsTabulaHierarchyUsedByIceAndFire() {
        String modelJson = "{\"modelName\":\"fixture\",\"authorName\":\"test\",\"projVersion\":4,"
            + "\"textureWidth\":64,\"textureHeight\":32,\"scale\":[1.0,1.0,1.0],\"cubeGroups\":[],"
            + "\"cubes\":[{\"name\":\"Body\",\"identifier\":\"body-id\",\"dimensions\":[3,4,5],"
            + "\"position\":[1.0,2.0,3.0],\"offset\":[-1.5,-2.0,-2.5],\"rotation\":[10.0,20.0,30.0],"
            + "\"scale\":[1.0,1.0,1.0],\"txOffset\":[4,6],\"txMirror\":true,\"children\":[{"
            + "\"name\":\"Head\",\"identifier\":\"head-id\",\"dimensions\":[2,2,2],"
            + "\"position\":[0.0,-2.0,0.0],\"offset\":[-1.0,-1.0,-1.0],\"rotation\":[0.0,0.0,0.0],"
            + "\"scale\":[1.0,1.0,1.0],\"txOffset\":[0,0],\"txMirror\":false,\"children\":[]}]}],"
            + "\"anims\":[],\"cubeCount\":2}";
        TabulaModelContainer container = TabulaModelHandler.INSTANCE
            .loadTabulaModel(new ByteArrayInputStream(modelJson.getBytes(StandardCharsets.UTF_8)));
        TabulaModel model = new TabulaModel(container);

        assertEquals(
            2,
            TabulaModelHandler.INSTANCE.getAllCubes(container)
                .size());
        assertSame(model.getCube("Body"), model.getCubeByIdentifier("body-id"));
        assertSame(
            model.getCube("Body"),
            model.getCube("Head")
                .getParent());
        assertEquals((float) Math.toRadians(20), model.getCube("Body").rotateAngleY, EPSILON);
        assertTrue(model.getCube("Body").mirror);
        assertEquals(1, model.getCube("Body").cubeList.size());
    }

    private static final class TestModel extends AdvancedEntityModel<Entity> {

        private final AdvancedModelBox root = new AdvancedModelBox(this, "root");
        private final AdvancedModelBox child = new AdvancedModelBox(this, "child");

        @Override
        public Iterable<BasicModelPart> parts() {
            return Collections.<BasicModelPart>singletonList(this.root);
        }

        @Override
        public Iterable<AdvancedModelBox> getAllParts() {
            return Arrays.asList(this.root, this.child);
        }

        @Override
        public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch) {}
    }

    private static final class TestAnimatedEntity implements IAnimatedEntity {

        private Animation animation = NO_ANIMATION;
        private int animationTick;

        @Override
        public int getAnimationTick() {
            return this.animationTick;
        }

        @Override
        public void setAnimationTick(int tick) {
            this.animationTick = tick;
        }

        @Override
        public Animation getAnimation() {
            return this.animation;
        }

        @Override
        public void setAnimation(Animation animation) {
            this.animation = animation;
        }

        @Override
        public Animation[] getAnimations() {
            return new Animation[] { this.animation };
        }
    }
}
