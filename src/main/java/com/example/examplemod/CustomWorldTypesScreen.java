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


    public static CustomWorldTypesScreen create(@Nullable WorldSelectionScreen parent) {
        return new CustomWorldTypesScreen(CreateWorldScreen.func_243425_a(parent));
    }


    @Override
    protected void init() {
        this.addButton(new Button(this.width / 2 + 55, this.height - 30, 150, 20, new TranslationTextComponent("Use Default World Screen"), (button) -> {
            this.getMinecraft().displayGuiScreen(CreateWorldScreen.func_243425_a(this));
        }));

        CONFIG_PATH.toFile().mkdirs();

        this.importedSettingsList = new ImportedSettingsList(this.minecraft, this.width, this.height, 48, this.height - 64, 36, CONFIG_PATH);
        this.children.add(this.importedSettingsList);
        ImportedSettingsList.ImportSettingsEntry entry = this.importedSettingsList.getEventListeners().stream().findFirst().orElse(null);
        this.importedSettingsList.setSelected(entry);

        this.addButton(new Button(this.width / 2 - 154, this.height - 52, 150, 20, new TranslationTextComponent("selectWorld.select"), (button) -> {
            this.importedSettingsList.getOptionalForSelected().ifPresent(importSettingsEntry -> executeWorldSettingsImport(this.getMinecraft(), importSettingsEntry.generatorSettingsPath));
        }));
    }


    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.importedSettingsList.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    private void executeWorldSettingsImport(Minecraft minecraft, @Nullable String dimensionJsonPath) {
        if (dimensionJsonPath != null) {
            DynamicRegistries.Impl dynamicregistries$impl = DynamicRegistries.func_239770_b_();

            ResourcePackList resourcepacklist = new ResourcePackList(new ServerPackFinder(), new FolderPackFinder(savePath().toFile(), IPackNameDecorator.WORLD));

            DataPackRegistries datapackregistries;
            try {
                MinecraftServer.func_240772_a_(resourcepacklist, datapackCodec, false);
                CompletableFuture<DataPackRegistries> completablefuture = DataPackRegistries.func_240961_a_(resourcepacklist.func_232623_f_(), Commands.EnvironmentType.INTEGRATED, 2, Util.getServerExecutor(), minecraft);
                minecraft.driveUntil(completablefuture::isDone);
                datapackregistries = completablefuture.get();
            } catch (ExecutionException | InterruptedException interruptedexception) {
                ExampleMod.LOGGER.error("Error loading data packs when importing world settings", interruptedexception);
                ITextComponent itextcomponent = new TranslationTextComponent("selectWorld.import_worldgen_settings.failure");
                ITextComponent itextcomponent1 = new StringTextComponent(interruptedexception.getMessage());
                minecraft.getToastGui().add(SystemToast.func_238534_a_(minecraft, SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER, itextcomponent, itextcomponent1));
                resourcepacklist.close();
                return;
            }

            WorldSettingsImport<JsonElement> worldsettingsimport = WorldSettingsImport.create(JsonOps.INSTANCE, datapackregistries.getResourceManager(), dynamicregistries$impl);
            JsonParser jsonparser = new JsonParser();

            DataResult<DimensionGeneratorSettings> dataresult;
            try (BufferedReader bufferedreader = Files.newBufferedReader(Paths.get(dimensionJsonPath))) {
                JsonElement jsonelement = jsonparser.parse(bufferedreader);
                dataresult = DimensionGeneratorSettings.field_236201_a_.parse(worldsettingsimport, jsonelement);
            } catch (JsonIOException | JsonSyntaxException | IOException ioexception) {
                dataresult = DataResult.error("Failed to parse file: " + ioexception.getMessage());
            }

            if (dataresult.error().isPresent()) {
                ITextComponent itextcomponent2 = new TranslationTextComponent("selectWorld.import_worldgen_settings.failure");
                String s1 = dataresult.error().get().message();
                ExampleMod.LOGGER.error("Error parsing world settings: {}", s1);
                ITextComponent itextcomponent3 = new StringTextComponent(s1);
                minecraft.getToastGui().add(SystemToast.func_238534_a_(minecraft, SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER, itextcomponent2, itextcomponent3));
            }

            datapackregistries.close();
            Lifecycle lifecycle = dataresult.lifecycle();
            dataresult.resultOrPartial(ExampleMod.LOGGER::error).ifPresent((p_239046_5_) -> {
                BooleanConsumer booleanconsumer = (p_239045_5_) -> {
                    minecraft.displayGuiScreen(this);
                    if (p_239045_5_) {
                        this.upgradeWorldData(dynamicregistries$impl, p_239046_5_);
                    }

                };
                if (lifecycle == Lifecycle.stable()) {
                    this.upgradeWorldData(dynamicregistries$impl, p_239046_5_);
                } else if (lifecycle == Lifecycle.experimental()) {
                    minecraft.displayGuiScreen(new ConfirmScreen(booleanconsumer, new TranslationTextComponent("selectWorld.import_worldgen_settings.experimental.title"), new TranslationTextComponent("selectWorld.import_worldgen_settings.experimental.question")));
                } else {
                    minecraft.displayGuiScreen(new ConfirmScreen(booleanconsumer, new TranslationTextComponent("selectWorld.import_worldgen_settings.deprecated.title"), new TranslationTextComponent("selectWorld.import_worldgen_settings.deprecated.question")));
                }
            });
        }
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
                SystemToast.func_238539_c_(this.getMinecraft(), ((CreateWorldScreenGetter) this.parent).getSaveDirName());
                this.parent.func_243430_k();
            }
        }

        return this.temporaryDataPackDirectory;
    }

    private static String seedToString(OptionalLong seed) {
        return seed.isPresent() ? Long.toString(seed.getAsLong()) : "";
    }


    public class ImportedSettingsList extends ExtendedList<ImportedSettingsList.ImportSettingsEntry> {
        public ImportedSettingsList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, Path configPath) {
            super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
            this.addEntry(new ImportSettingsEntry("name1", "description", configPath.resolve("test_worldsettings.json").toString()));
        }

        public Optional<ImportedSettingsList.ImportSettingsEntry> getOptionalForSelected() {
            return Optional.ofNullable(this.getSelected());
        }


        public class ImportSettingsEntry extends ExtendedList.AbstractListEntry<ImportSettingsEntry> {
            private final String name;
            private final String description;
            private final String generatorSettingsPath;

            public ImportSettingsEntry(String worldTypeName, String description, String generatorSettingsPath) {
                this.name = worldTypeName;
                this.description = description;
                this.generatorSettingsPath = generatorSettingsPath;
            }

            @Override
            public void render(MatrixStack stack, int i, int x, int text, int i2, int i3, int i4, int i5, boolean b, float f) {
                AbstractGui.drawString(stack, CustomWorldTypesScreen.this.font, this.name, text, x, 16777215);
                AbstractGui.drawString(stack, CustomWorldTypesScreen.this.font, this.description, text + 5, x - 25, 12632256);

            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    CustomWorldTypesScreen.ImportedSettingsList.this.setSelected(this);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}
