package com.example.examplemod.mixin.access;


import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.widget.Widget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreateWorldScreen.class)
public interface CreateWorldScreenAccess {

    @Invoker
    <T extends Widget> T invokeAddButton(T button);


}
