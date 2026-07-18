package dev.reliclootviewer.jei;

import dev.reliclootviewer.RelicLootViewer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public final class RelicSourceCategory implements IRecipeCategory<RelicSourceRecipe> {
    public static final RecipeType<RelicSourceRecipe> TYPE =
            RecipeType.create(RelicLootViewer.MOD_ID, "relic_sources", RelicSourceRecipe.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 124;

    private final IDrawable background;
    private final IDrawable icon;

    public RelicSourceCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(Items.CHEST);
    }

    @Override
    public RecipeType<RelicSourceRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.relic_loot_viewer.category.relic_sources");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RelicSourceRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 2)
                .addItemStack(recipe.relic())
                .setOutputSlotBackground();
    }

    @Override
    public void draw(RelicSourceRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        int y = 24;

        if (!recipe.obtainable()) {
            Component warning = Component.translatable("jei.relic_loot_viewer.unobtainable")
                    .withStyle(ChatFormatting.RED);
            for (var line : font.split(warning, WIDTH - 8)) {
                graphics.drawString(font, line, 4, y, 0xFF5555, false);
                y += font.lineHeight + 1;
            }
            return;
        }

        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(
                "jei.relic_loot_viewer.global_chance",
                String.format(Locale.ROOT, "%.1f", recipe.globalChance() * 100.0)
        ));
        lines.add(Component.translatable(
                "jei.relic_loot_viewer.relative_weight",
                Component.translatable(recipe.weightBand().translationKey()),
                recipe.weight(), recipe.minimumWeight(), recipe.maximumWeight()
        ));
        lines.add(Component.translatable("jei.relic_loot_viewer.tables",
                summarize(recipe.tables(), SourceDescriptionFormatter.Kind.LOOT_TABLE)));
        lines.add(Component.translatable("jei.relic_loot_viewer.dimensions",
                summarize(recipe.dimensions(), SourceDescriptionFormatter.Kind.DIMENSION)));
        lines.add(Component.translatable("jei.relic_loot_viewer.biomes",
                summarize(recipe.biomes(), SourceDescriptionFormatter.Kind.BIOME)));
        lines.add(Component.translatable("jei.relic_loot_viewer.probability_note")
                .withStyle(ChatFormatting.DARK_GRAY));

        outer:
        for (Component component : lines) {
            for (var line : font.split(component, WIDTH - 8)) {
                if (y + font.lineHeight > HEIGHT) {
                    break outer;
                }
                graphics.drawString(font, line, 4, y, 0x404040, false);
                y += font.lineHeight + 1;
            }
        }
    }

    @Override
    public ResourceLocation getRegistryName(RelicSourceRecipe recipe) {
        return recipe.id();
    }

    static Component summarize(List<String> values, SourceDescriptionFormatter.Kind kind) {
        if (values.isEmpty()) {
            return Component.translatable("jei.relic_loot_viewer.any");
        }

        return Component.literal(SourceDescriptionFormatter.formatAll(values, kind));
    }
}
