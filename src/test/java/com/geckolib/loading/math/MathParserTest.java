package com.geckolib.loading.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.geckolib.animation.state.ControllerState;

public class MathParserTest {

    private static final double EPSILON = 1.0E-8;

    private final MathParser parser = MathParser.createWithDeduplication();

    @Test
    public void respectsOperatorPrecedenceAndConditionals() {
        assertEquals(7, evaluate("1 + 2 * 3", MolangContext.EMPTY), EPSILON);
        assertEquals(8, evaluate("2 ^ 3", MolangContext.EMPTY), EPSILON);
        assertEquals(4, evaluate("1 < 2 ? 4 : 9", MolangContext.EMPTY), EPSILON);
        assertEquals(1, evaluate("1 < 2 && 3 != 4", MolangContext.EMPTY), EPSILON);
    }

    @Test
    public void resolvesQueryAliasesAndGeckoLibFunctions() {
        MolangContext context = name -> "query.limb_swing".equals(name) ? 30 : 0;

        assertEquals(2.5, evaluate("math.sin(q.limb_swing) + math.clamp(5, 0, 2)", context), EPSILON);
    }

    @Test
    public void scopesAssignmentsToOneControllerState() {
        ControllerState firstState = new ControllerState();
        ControllerState secondState = new ControllerState();
        MathValue assignment = this.parser.compileMolang("v.offset = 2; return v.offset * 3");
        MathValue lookup = this.parser.compileMolang("variable.offset");

        assertEquals(6, assignment.get(firstState), EPSILON);
        assertEquals(2, lookup.get(firstState), EPSILON);
        assertEquals(0, lookup.get(secondState), EPSILON);
    }

    @Test
    public void evaluatesBlockbenchStyleCompoundExpressions() {
        ControllerState state = new ControllerState().setAnimationTime(0.001);
        MathValue expression = this.parser.compileMolang(
            "0; temp.speed = 1000; temp.body = math.sin(query.anim_time * temp.speed); return temp.body;");

        assertEquals(Math.sin(Math.PI / 180), expression.get(state), EPSILON);
    }

    private double evaluate(String expression, MolangContext context) {
        return this.parser.compileMolang(expression)
            .get(new ControllerState(context));
    }
}
