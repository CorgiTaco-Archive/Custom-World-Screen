package com.example.examplemod.mixin;

import com.example.examplemod.CustomWorldTypesScreen;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class MixinCreateWorldScreen {

//    @Shadow protected abstract <T extends Widget> T addButton(T button);
//
//    private CustomWorldTypesScreen.ImportedSettingsList importedSettingsList;
//
//
//    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
//    private void controlMinecraftCreateWorldScreen(CallbackInfo ci) {
//        this.addButton(new Button(this.width / 2 + 55, this.height - 30, 150, 20, new TranslationTextComponent("Use Default World Screen"), (button) -> {
//            this. getMinecraft().displayGuiScreen(CreateWorldScreen.func_243425_a(this));
//        }));
//
//        this.importedSettingsList = new CustomWorldTypesScreen.ImportedSettingsList(this.minecraft, this.width, this.height, 48, this.height - 64, 36);
//        this.children.add(this.importedSettingsList);
//        CustomWorldTypesScreen.ImportedSettingsList.ImportSettingsEntry entry = this.importedSettingsList.getEventListeners().stream().findFirst().orElse(null);
//        this.importedSettingsList.setSelected(entry);
//
//    }
}
