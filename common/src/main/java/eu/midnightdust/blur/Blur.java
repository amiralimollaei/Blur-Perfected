package eu.midnightdust.blur;

import eu.midnightdust.blur.config.BlurConfig;
import eu.midnightdust.blur.util.RainbowColor;
import eu.midnightdust.lib.util.MidnightColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.joml.Math;
import org.joml.Matrix3x2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

import static eu.midnightdust.blur.util.RainbowColor.hue;
import static eu.midnightdust.blur.util.RainbowColor.hue2;

public class Blur {
    public static final String MOD_ID = "blurperfected";
    public static final Logger logger = LoggerFactory.getLogger(MOD_ID);
    public static void init() {
        BlurConfig.init(MOD_ID, BlurConfig.class);
    }

    public static long lastRender = -1;
    public static long deltaTime = -1;
    public static float fadeTimeState = 1.0F;
    public static float fadeProgress = 1.0F;
    public static boolean screenHasBlur = false;

    public static void onRender(DrawContext context) {
        long currentTime = System.currentTimeMillis();
        if (lastRender <= 0) {
            lastRender = currentTime;
            deltaTime = 0;
        } else {
            deltaTime = System.currentTimeMillis() - lastRender;
            lastRender = currentTime;
        }

        Blur.updateFadeAnimation(context);
    }

    public static void onScreenChange(Screen newScreen) {
        screenHasBlur = false;
    }

    public static void updateFadeAnimation(DrawContext context) {
        if (screenHasBlur) {
            fadeTimeState += deltaTime / (float) BlurConfig.fadeTimeMillis;
        }
        else {
            fadeTimeState -= deltaTime / (float) BlurConfig.fadeOutTimeMillis;
        }
        fadeTimeState = Math.clamp(0, 1, fadeTimeState);
        fadeProgress = BlurConfig.animationCurve.apply((double) fadeTimeState).floatValue();
    }

    public static int getBackgroundColor(boolean second) {
        int a = second ? BlurConfig.gradientEndAlpha : BlurConfig.gradientStartAlpha;
        var col = MidnightColorUtil.hex2Rgb(second ? BlurConfig.gradientEnd : BlurConfig.gradientStart);
        if (BlurConfig.rainbowMode) col = second ? Color.getHSBColor(hue, 1, 1) : Color.getHSBColor(hue2, 1, 1);
        int r = (col.getRGB() >> 16) & 0xFF;
        int b = (col.getRGB() >> 8) & 0xFF;
        int g = col.getRGB() & 0xFF;
        float prog = fadeProgress;
        a = (int) (prog * a);
        return a << 24 | r << 16 | b << 8 | g;
    }
    public static int getRotation() {
        if (BlurConfig.rainbowMode) return RainbowColor.rotation;
        return BlurConfig.gradientRotation;
    }
    public static void renderRotatedGradient(DrawContext context, int width, int height) {
        float diagonal = Math.sqrt((float) width*width + height*height);
        int smallestDimension = Math.min(width, height);

        context.getMatrices().pushMatrix();
        Matrix3x2f posMatrix = context.getMatrices();
        posMatrix.rotate(Math.toRadians(getRotation()));
        posMatrix.setTranslation(width / 2f, height / 2f); // Make the gradient's center the pivot point
        posMatrix.scale(diagonal / smallestDimension); // Scales the gradient to the maximum diagonal value needed
        context.fillGradient(-width / 2, -height / 2, width / 2, height / 2, Blur.getBackgroundColor(false), Blur.getBackgroundColor(true)); // Actually draw the gradient
        context.getMatrices().popMatrix();
    }
}
