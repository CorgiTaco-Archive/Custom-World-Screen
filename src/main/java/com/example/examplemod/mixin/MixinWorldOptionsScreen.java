package com.example.examplemod.mixin;

import com.example.examplemod.CustomWorldTypesScreen;
import com.example.examplemod.ExampleMod;
import com.example.examplemod.getters.CreateWorldScreenGetter;
import com.example.examplemod.mixin.access.CreateWorldScreenAccess;
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
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.WorldOptionsScreen;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.command.Commands;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.WorldSettingsImport;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.fml.loading.FMLPaths;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Mixin(WorldOptionsScreen.class)
public abstract class MixinWorldOptionsScreen {


    @Shadow
    private FontRenderer field_239031_e_;

    @Shadow
    private int field_239032_f_;

    @Shadow
    private TextFieldWidget field_239033_g_;

    @Shadow
    private OptionalLong field_243444_q;

    @Shadow
    protected abstract OptionalLong func_243449_f();

    @Shadow
    protected static String func_243445_a(OptionalLong p_243445_0_) {
        return new Exception("Mixin Didnt apply").toString();
    }

    private CustomWorldTypesScreen.ImportedSettingsList importedSettingsList;

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("csw");

    @Shadow
    public Button field_239027_a_;

    @Shadow
    protected abstract void func_239052_a_(DynamicRegistries.Impl p_239052_1_, DimensionGeneratorSettings p_239052_2_);

    @Shadow
    private Button field_239035_i_;

    @Shadow
    private Button field_239034_h_;

    @Shadow
    private Button field_239036_j_;

    @Shadow
    private Button field_239037_k_;

    private Button createSettings;

    private CreateWorldScreen createWorldScreen;

    private static boolean wereSettingsCreatedSuccessfully;

    @Inject(method = "func_239048_a_", at = @At("HEAD"), cancellable = true)
    private void customWorldScreen(CreateWorldScreen createWorldScreen, Minecraft mc, FontRenderer fontRenderer, CallbackInfo ci) {
        ci.cancel();

        this.createWorldScreen = createWorldScreen;

        int i = createWorldScreen.width / 2 - 155;

        this.field_239031_e_ = fontRenderer;
        this.field_239032_f_ = createWorldScreen.width;
        this.field_239033_g_ = new TextFieldWidget(this.field_239031_e_, this.field_239032_f_ / 2 - 100, 22, 200, 20, new TranslationTextComponent("selectWorld.enterSeed"));
        this.field_239033_g_.setText(func_243445_a(this.field_243444_q));
        this.field_239033_g_.setResponder((p_239058_1_) -> {
            this.field_243444_q = this.func_243449_f();
        });

        ((CreateWorldScreenAccess) createWorldScreen).getAddListener(this.field_239033_g_);


        this.field_239027_a_ = new Button(0, -25000, 0, 0, new StringTextComponent(""), (button) -> {
        });

        this.field_239035_i_ = new Button(0, -25000, 0, 0, new StringTextComponent(""), (button) -> {
        });

        this.field_239034_h_ = new Button(0, -25000, 0, 0, new StringTextComponent(""), (button) -> {
        });

        this.field_239036_j_ = new Button(0, -25000, 0, 0, new StringTextComponent(""), (button) -> {
        });

        this.field_239037_k_ = new Button(0, -25000, 0, 0, new StringTextComponent(""), (button) -> {
        });

        this.importedSettingsList = new CustomWorldTypesScreen.ImportedSettingsList(createWorldScreen.getMinecraft(), createWorldScreen.width, createWorldScreen.height, 48, createWorldScreen.height - 64, 36, CONFIG_PATH, createWorldScreen.getMinecraft().fontRenderer);
        createWorldScreen.children.add(this.importedSettingsList);
        this.importedSettingsList.setSelected(null);


        createSettings = new Button(i, createWorldScreen.height - 58, 150, 20, new TranslationTextComponent("Create Settings"), (button) -> {
            this.importedSettingsList.getOptionalForSelected().ifPresent(importSettingsEntry -> {
                wereSettingsCreatedSuccessfully = executeWorldSettingsImport(createWorldScreen, createWorldScreen.getMinecraft(), importSettingsEntry.generatorSettingsPath);
                ((CreateWorldScreenGetter) this.createWorldScreen).getMoreOptionsButton().active = wereSettingsCreatedSuccessfully;
            });
        });



        ((CreateWorldScreenAccess) createWorldScreen).invokeAddButton(createSettings);
    }

    @Inject(method = "func_239059_b_(Z)V", at = @At("HEAD"), cancellable = true)
    private void cancel(boolean b, CallbackInfo ci) {
        this.createSettings.visible = b;
        ((CreateWorldScreenGetter) this.createWorldScreen).getCreateWorldScreenButton().visible = !b;
        ((CreateWorldScreenGetter) this.createWorldScreen).getMoreOptionsButton().active = !b;
        if (b)
            wereSettingsCreatedSuccessfully = true;

    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelAndReplaceRenderer(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        ci.cancel();
        this.importedSettingsList.render(matrixStack, mouseX, mouseY, partialTicks);
        this.field_239033_g_.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private boolean executeWorldSettingsImport(final CreateWorldScreen createWorldScreen, Minecraft mc, @Nullable String dimensionJsonPath) {
        if (dimensionJsonPath != null) {
            DynamicRegistries.Impl dynamicregistries$impl = DynamicRegistries.func_239770_b_();
            ResourcePackList resourcepacklist = new ResourcePackList(new ServerPackFinder(), new FolderPackFinder(createWorldScreen.func_238957_j_().toFile(), IPackNameDecorator.WORLD));

            DataPackRegistries datapackregistries;
            try {
                MinecraftServer.func_240772_a_(resourcepacklist, createWorldScreen.field_238933_b_, false);
                CompletableFuture<DataPackRegistries> completablefuture = DataPackRegistries.func_240961_a_(resourcepacklist.func_232623_f_(), Commands.EnvironmentType.INTEGRATED, 2, Util.getServerExecutor(), mc);
                mc.driveUntil(completablefuture::isDone);
                datapackregistries = completablefuture.get();
            } catch (ExecutionException | InterruptedException interruptedexception) {
                ExampleMod.LOGGER.error("Error loading data packs when importing world settings", interruptedexception);
                ITextComponent itextcomponent = new TranslationTextComponent("selectWorld.import_worldgen_settings.failure");
                ITextComponent itextcomponent1 = new StringTextComponent(interruptedexception.getMessage());
                mc.getToastGui().add(SystemToast.func_238534_a_(mc, SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER, itextcomponent, itextcomponent1));
                resourcepacklist.close();
                return false;
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
                String error = dataresult.error().get().message();
                ExampleMod.LOGGER.error("Error parsing world settings: {}", error);
                ITextComponent itextcomponent3 = new StringTextComponent(error);
                mc.getToastGui().add(SystemToast.func_238534_a_(mc, SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER, itextcomponent2, itextcomponent3));
                return false;
            }

            datapackregistries.close();
            Lifecycle lifecycle = dataresult.lifecycle();
            dataresult.resultOrPartial(ExampleMod.LOGGER::error).ifPresent((genSettings) -> {
                if (false) {
                    BooleanConsumer booleanconsumer = (bool) -> {
                        mc.displayGuiScreen(createWorldScreen);
                        if (bool) {
                            this.func_239052_a_(dynamicregistries$impl, genSettings);
                        }

                    };
                    if (lifecycle == Lifecycle.stable()) {
                        this.func_239052_a_(dynamicregistries$impl, genSettings);
                    } else if (lifecycle == Lifecycle.experimental()) {
                        mc.displayGuiScreen(new ConfirmScreen(booleanconsumer, new TranslationTextComponent("selectWorld.import_worldgen_settings.experimental.title"), new TranslationTextComponent("selectWorld.import_worldgen_settings.experimental.question")));
                    } else {
                        mc.displayGuiScreen(new ConfirmScreen(booleanconsumer, new TranslationTextComponent("selectWorld.import_worldgen_settings.deprecated.title"), new TranslationTextComponent("selectWorld.import_worldgen_settings.deprecated.question")));
                    }
                } else {
                    this.func_239052_a_(dynamicregistries$impl, genSettings);
                }
            });
        }
        return true;
    }
}
