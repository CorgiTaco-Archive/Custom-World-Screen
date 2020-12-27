package com.example.examplemod;

import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

public class CustomWorldTypesScreen extends Screen {

    private final WorldSelectionScreen parent;

    protected CustomWorldTypesScreen(WorldSelectionScreen parent) {
        super(ITextComponent.getTextComponentOrEmpty("cwts.title"));
        this.parent = parent;
    }


    public static CustomWorldTypesScreen create(@Nullable WorldSelectionScreen parent) {
        return new CustomWorldTypesScreen(parent);
    }

    @Override
    protected void init() {
        this.addButton(new Button(100, 151, 150, 20, new TranslationTextComponent("usedefaultworldscreen"), (button) -> {
            this.getMinecraft().displayGuiScreen(CreateWorldScreen.func_243425_a(this));
        }));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
