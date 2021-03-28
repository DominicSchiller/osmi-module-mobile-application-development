package de.thb.schiller.mad2doplanner.ui.common.animation;

import android.animation.TypeEvaluator;

/**
 * @author Dominic Schiller
 * @since 03.07.17
 *
 * This class provides a soft ease out interpolation.
 * @see TypeEvaluator
 */

public class CircleEaseOutEvaluator implements TypeEvaluator<Number> {


    private final float mDuration;

    /**
     * Constructor
     * @param duration The overall interpolation duration
     */
    public CircleEaseOutEvaluator(float duration) {
        mDuration = duration;
    }

    @Override
    public Number evaluate(float fraction, Number startValue, Number endValue) {

        float t = mDuration * fraction;
        float b = startValue.floatValue();
        float c = endValue.floatValue() - startValue.floatValue();

        return calculate(t,b,c, mDuration);
    }

    /**
     * Calculates the interpolation
     * @param t the duration fraction
     * @param b The start position
     * @param c The end position
     * @param d The overall duration
     * @return The interpolated value
     */
    private Float calculate(float t, float b, float c, float d) {
        return c * (float)Math.sqrt(1 - (t=t/d-1)*t) + b;
    }
}
