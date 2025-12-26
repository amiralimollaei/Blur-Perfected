package eu.midnightdust.blur.mixin;

import eu.midnightdust.blur.Blur;
import eu.midnightdust.blur.config.BlurConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Final @Shadow private MinecraftClient client;

    @Inject(at = @At("TAIL"), method = "render")
    public void blur$renderFadeOut(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) { // Adds a fade-out effect when a player is in a world and closes all screens
        if (client.currentScreen == null && client.world != null) {
            Blur.screenHasBlur = false;
            Blur.onRender(context);

            if (Blur.fadeTimeState < 0.001F) return;  // we have faded out at this point and don't need to render anything

            context.applyBlur();
            if (BlurConfig.useGradient) {
                // render the fade out gradient
                Window window = client.getWindow();
                Blur.renderRotatedGradient(context, window.getWidth(), window.getHeight());
            }
        }
    }
}
