package eu.midnightdust.blur.animations;

import eu.midnightdust.blur.config.BlurConfig;

public abstract class AbstractEMAAnimationHandler<E extends Enum<E>> extends AbstractAnimationHandler<E> implements IAnimationHandler<E> {
    protected final double emaAlphaMillis = 0.9;

    private AnimationState calculateEma(float deltaSeconds, AnimationState oldAnimationState, AnimationState newAnimationState) {
        float emaAlpha = (float) Math.pow(emaAlphaMillis, deltaSeconds * 1000);
        float emaProgress = oldAnimationState.progress() * (1 - emaAlpha) + newAnimationState.progress() * emaAlpha;

        return new AnimationState(
                newAnimationState.timeState(),
                emaProgress
        );
    }

    @Override
    public void updateAnimation(float deltaSeconds, BlurConfig.Easing easing) {
        if (state == null || deltaSeconds <= 0) return;
        // actually update the animation
        AnimationState oldAnimationState = new AnimationState(getTimeState(), getProgress());
        AnimationState newAnimationState = stepAnimation(deltaSeconds, easing, oldAnimationState);
        // calculate ema
        AnimationState finalAnimationState = calculateEma(deltaSeconds, oldAnimationState, newAnimationState);
        timeState = finalAnimationState.timeState();
        progress = finalAnimationState.progress();

    }
}
