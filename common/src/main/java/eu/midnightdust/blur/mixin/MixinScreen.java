package eu.midnightdust.blur.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.midnightdust.blur.config.BlurConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import eu.midnightdust.blur.Blur;

import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen {
    @Shadow public int width;
    @Shadow public int height;
    @Shadow protected abstract void applyBlur(DrawContext context);

    @Inject(at = @At("HEAD"), method = "render")
    public void blur$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Blur.onRender(context);
    }

    @Inject(at = @At("HEAD"), method = "applyBlur")
    public void blur$onScreenApplyBlurStart(CallbackInfo ci) {
        if (!BlurConfig.forceDisabledScreens.contains(this.getClass().getCanonicalName())) {
            Blur.screenHasBlur = true;  // set if the screen has blur
        }
    }

    @Inject(at = @At("TAIL"), method = "applyBlur")
    public void blur$onScreenApplyBlurEnd(CallbackInfo ci) {
        Blur.blurApplied = true;
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderBackgroundTexture(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/util/Identifier;IIFFII)V"), method = "renderDarkening(Lnet/minecraft/client/gui/DrawContext;IIII)V")
    private void blur$applyGradient(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, Operation<Void> original) {
        if (BlurConfig.useGradient) {
            blur$renderBackground(context); // Replaces the background texture with a gradient
        } else original.call(context, texture, x, y, u, v, width, height);
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fillGradient(IIIIII)V"), method = "renderInGameBackground")
    public void blur$rotatedGradient(DrawContext context, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, Operation<Void> original) {
        blur$renderBackground(context);
    }
    @Unique
    private void blur$renderBackground(DrawContext context) {
        if (Blur.fadeTimeState < 0.001F) return;  // we have faded out at this point and don't need to render anything

        if (!Blur.blurApplied && BlurConfig.forceEnabledScreens.contains(this.getClass().getCanonicalName())) {
            this.applyBlur(context);
        }

        Blur.renderRotatedGradient(context, width, height); // Replaces the default gradient with our rotated one
    }
}
