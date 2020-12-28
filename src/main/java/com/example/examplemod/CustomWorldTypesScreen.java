package com.example.examplemod;

import com.example.examplemod.getters.CreateWorldScreenGetter;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.command.Commands;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.codec.DatapackCodec;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CustomWorldTypesScreen extends Screen {

    private final CreateWorldScreen parent;
    private ImportedSettingsList importedSettingsList;

    private DynamicRegistries.Impl dynamicRegistries;
    private DimensionGeneratorSettings dimensionGeneratorSettings;
    private OptionalLong seed;
    private TextFieldWidget seedTextField;
    private Path temporaryDataPackDirectory;
    protected DatapackCodec datapackCodec = DatapackCodec.VANILLA_CODEC;
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("csw");

    protected CustomWorldTypesScreen(CreateWorldScreen parent) {
        super(ITextComponent.getTextComponentOrEmpty("cwts.title"));
        this.parent = parent;
    }


    public static CustomWorldTypesScreen create(@Nullable CreateWorldScreen parent) {
        return new CustomWorldTypesScreen(parent);
    }

//
//    @Override
//    protected void init() {
//        this.addButton(new Button(this.width / 2 + 55, this.height - 30, 150, 20, new TranslationTextComponent("Use Default World Screen"), (button) -> {
//            this.getMinecraft().displayGuiScreen(CreateWorldScreen.func_243425_a(this));
//        }));
//
//        CONFIG_PATH.toFile().mkdirs();
//
//        this.importedSettingsList = new ImportedSettingsList(this.minecraft, this.width, this.height, 48, this.height - 64, 36, CONFIG_PATH);
//        this.children.add(this.importedSettingsList);
//        ImportedSettingsList.ImportSettingsEntry entry = this.importedSettingsList.getEventListeners().stream().findFirst().orElse(null);
//        this.importedSettingsList.setSelected(entry);
//
//        this.addButton(new Button(this.width / 2 - 154, this.height - 52, 150, 20, new TranslationTextComponent("selectWorld.select"), (button) -> {
//            this.importedSettingsList.getOptionalForSelected().ifPresent(importSettingsEntry -> executeWorldSettingsImport(this.getMinecraft(), importSettingsEntry.generatorSettingsPath));
//        }));
//    }
//

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }


    private void upgradeWorldData(DynamicRegistries.Impl dynamicRegistries, DimensionGeneratorSettings generatorSettings) {
        this.dynamicRegistries = dynamicRegistries;
        this.dimensionGeneratorSettings = generatorSettings;
        this.seed = OptionalLong.of(generatorSettings.getSeed());
//        this.seedTextField.setText(seedToString(this.seed));
    }

    protected Path savePath() {
        if (this.temporaryDataPackDirectory == null) {
            try {
                this.temporaryDataPackDirectory = Files.createTempDirectory("mcworld-");
            } catch (IOException ioexception) {
                ExampleMod.LOGGER.warn("Failed to create temporary dir", ioexception);
//                SystemToast.func_238539_c_(this.getMinecraft(), ((CreateWorldScreenGetter) this.parent).getSaveDirName());
                this.parent.func_243430_k();
            }
        }

        return this.temporaryDataPackDirectory;
    }

    private static String seedToString(OptionalLong seed) {
        return seed.isPresent() ? Long.toString(seed.getAsLong()) : "";
    }


    public static class ImportedSettingsList extends ExtendedList<ImportedSettingsList.ImportSettingsEntry> {
        public ImportedSettingsList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, Path configPath, FontRenderer fontRenderer) {
            super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
            this.addEntry(new ImportSettingsEntry("Nether Overworld", "Swaps Nether & Overworld dimensions", configPath.resolve("nether_overworld.json").toString(), fontRenderer, this));
        }

        public Optional<ImportedSettingsList.ImportSettingsEntry> getOptionalForSelected() {
            return Optional.ofNullable(this.getSelected());
        }


        public static class ImportSettingsEntry extends ExtendedList.AbstractListEntry<ImportSettingsEntry> {
            private final String name;
            private final String description;
            public final String generatorSettingsPath;
            private FontRenderer fontRenderer;
            private ExtendedList<ImportSettingsEntry> parent;

            public ImportSettingsEntry(String worldTypeName, String description, String generatorSettingsPath, FontRenderer fontRenderer, ExtendedList<ImportSettingsEntry> parent) {
                this.name = worldTypeName;
                this.description = description;
                this.generatorSettingsPath = generatorSettingsPath;
                this.fontRenderer = fontRenderer;
                this.parent = parent;
            }

            @Override
            public void render(MatrixStack stack, int i, int x, int text, int i2, int i3, int i4, int i5, boolean b, float f) {
                AbstractGui.drawString(stack, fontRenderer, this.name + " - " + description, text, x, 16777215);
                AbstractGui.drawString(stack, fontRenderer, this.name + " - " + description, text, x, 16777215);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    parent.setSelected(this);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}
