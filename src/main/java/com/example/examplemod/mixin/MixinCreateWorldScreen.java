package com.example.examplemod.mixin;

import com.example.examplemod.getters.CreateWorldScreenGetter;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.widget.button.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CreateWorldScreen.class)
public class MixinCreateWorldScreen implements CreateWorldScreenGetter {

    @Shadow
    private Button btnCreateWorld;

    @Shadow private Button btnMoreOptions;

    @Override
    public Button getCreateWorldScreenButton() {
        return this.btnCreateWorld;
    }

    @Override
    public Button getMoreOptionsButton() {
        return this.btnMoreOptions;
    }
}
