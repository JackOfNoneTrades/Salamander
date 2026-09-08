package com.geckolib.renderer.base;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.lwjgl.BufferChecks;
import org.lwjgl.BufferUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class GlStateSnapshotTest {

    @SuppressWarnings("unchecked")
    private ThreadLocal<FloatBuffer> colorBuffers() throws Exception {
        Field field = GlStateSnapshot.class.getDeclaredField("COLOR_BUFFER");
        field.setAccessible(true);
        return (ThreadLocal<FloatBuffer>) field.get(null);
    }

    @Test
    public void currentColorBufferMeetsRealLwjgl2QueryContract() throws Exception {
        FloatBuffer buffer = colorBuffers().get();
        buffer.clear();
        assertTrue(buffer.isDirect());
        assertEquals(16, buffer.remaining());
        // This is the actual guard called by LWJGL 2 glGetFloat before it reaches native OpenGL.
        BufferChecks.checkBuffer(buffer, 16);
        assertThrows(
            IllegalArgumentException.class,
            () -> BufferChecks.checkBuffer(BufferUtils.createFloatBuffer(4), 16));
    }

    @Test
    public void bufferIsReusedPerThreadButNotSharedAcrossThreads() throws Exception {
        ThreadLocal<FloatBuffer> buffers = colorBuffers();
        FloatBuffer current = buffers.get();
        assertSame(current, buffers.get());
        AtomicReference<FloatBuffer> other = new AtomicReference<>();
        Thread thread = new Thread(() -> other.set(buffers.get()), "snapshot-buffer-test");
        thread.start();
        thread.join();
        assertNotNull(other.get());
        assertNotSame(current, other.get());
        BufferChecks.checkBuffer(other.get(), 16);
    }

    @Test
    public void everyRendererColorQueryAllocatesTheFullLwjgl2Buffer() throws Exception {
        for (String name : new String[] { "base/GlStateSnapshot", "GeoEntityRenderer", "GeoReplacedEntityRenderer",
            "GeoArmorRenderer" }) {
            ClassNode type = new ClassNode();
            try (InputStream stream = getClass().getResourceAsStream("/com/geckolib/renderer/" + name + ".class")) {
                assertNotNull(stream);
                new ClassReader(stream).accept(type, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
            int allocations = 0;
            for (MethodNode method : type.methods) for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (!call.owner.equals("org/lwjgl/BufferUtils") || !call.name.equals("createFloatBuffer")) continue;
                AbstractInsnNode size = call.getPrevious();
                while (size.getOpcode() < 0) size = size.getPrevious();
                assertTrue(name, size instanceof IntInsnNode);
                assertEquals(name, 16, ((IntInsnNode) size).operand);
                allocations++;
            }
            assertEquals(name, 1, allocations);
        }
    }
}
