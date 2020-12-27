package com.example.examplemod.mixin;

import net.minecraft.client.gui.screen.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(CreateWorldScreen.class)
public abstract class MixinCreateWorldScreen {
}
