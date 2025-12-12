package eu.midnightdust.blur.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.midnightdust.blur.Blur;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameOptions.class)
public abstract class MixinGameOptions {
    @Shadow @Final private SimpleOption<Integer> menuBackgroundBlurriness;
    @Shadow @Final private SimpleOption<Double> chatLineSpacing;

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/option/SimpleOption$ValidatingIntSliderCallbacks", ordinal = 3))
    private SimpleOption.ValidatingIntSliderCallbacks blur$increaseMaxBlurriness(int minInclusive, int maxInclusive, boolean applyValueImmediately) {
        if (this.menuBackgroundBlurriness == null && this.chatLineSpacing != null)
            return new SimpleOption.ValidatingIntSliderCallbacks(minInclusive, 20);
        return new SimpleOption.ValidatingIntSliderCallbacks(minInclusive, maxInclusive);
    }

    @ModifyReturnValue(method = "getMenuBackgroundBlurrinessValue", at = @At("RETURN"))
    private int blur$applyFadeProgress(int original) {
        return (int) (original * Blur.fadeProgress);
    }
}
