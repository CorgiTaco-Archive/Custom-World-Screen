package com.example.examplemod.mixin;

import com.example.examplemod.CustomWorldTypesScreen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionScreen.class)
public class CreateWorldScreenMixin {


    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(at = @At(value = "HEAD"), method = "lambda$init$4", cancellable = true)
    private void init(CallbackInfo info) {
        info.cancel();
        ((WorldSelectionScreen) (Object) this).getMinecraft().displayGuiScreen(CustomWorldTypesScreen.create((WorldSelectionScreen) (Object) this));
    }
}
