package com.creatingstories.workshop.mixin;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Retains Ice and Fire's menu artwork without drawing its version over NeoForge's branding. */
@Pseudo
@Mixin(targets = "com.iafenvoy.iceandfire.screen.TitleScreenRenderManager", remap = false)
public abstract class IceAndFireTitleScreenRenderMixin {
    @Inject(method = "drawModName", at = @At("HEAD"), cancellable = true, require = 0)
    private static void creatingStories$hideOverlappingVersion(GuiGraphics graphics,
                                                                int width,
                                                                int height,
                                                                int alpha,
                                                                CallbackInfo callback) {
        callback.cancel();
    }
}
