package org.fentanylsolutions.salamander.cem;

import static org.junit.Assert.*;

import org.fentanylsolutions.salamander.cem.animation.CemMovement;
import org.junit.Test;

public class CemMovementTest {

    @Test
    public void directionsFollowBodyYawAndNormalizeDiagonalMotion() {
        assertArrayEquals(new double[] { 0, 0 }, CemMovement.direction(0, 0, 37), 1e-8);
        assertArrayEquals(new double[] { 1, 0 }, CemMovement.direction(0, .2, 0), 1e-8);
        assertArrayEquals(new double[] { -1, 0 }, CemMovement.direction(0, -.2, 0), 1e-8);
        assertArrayEquals(new double[] { 0, 1 }, CemMovement.direction(-.2, 0, 0), 1e-8);
        assertArrayEquals(new double[] { 1, 0 }, CemMovement.direction(-.2, 0, 90), 1e-8);
        assertArrayEquals(new double[] { Math.sqrt(.5), Math.sqrt(.5) }, CemMovement.direction(-1, 1, 0), 1e-8);
    }
}
