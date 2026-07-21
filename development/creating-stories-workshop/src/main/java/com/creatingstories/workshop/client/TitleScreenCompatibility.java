package com.creatingstories.workshop.client;

import com.creatingstories.workshop.CreatingStoriesWorkshop;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.List;

/** Keeps third-party title-screen additions inside the vanilla button layout. */
@EventBusSubscriber(modid = CreatingStoriesWorkshop.MOD_ID, value = Dist.CLIENT)
public final class TitleScreenCompatibility {
    private static final String SINGLEPLAYER_KEY = "menu.singleplayer";
    private static final String CHANGELOG_KEY_PREFIX = "button.modpack-update-checker.changelog-button";
    private static final int COMPACT_BUTTON_SIZE = 20;
    private static final int BUTTON_GAP = 4;

    private TitleScreenCompatibility() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void afterTitleScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen)) return;

        List<AbstractWidget> widgets = event.getListenersList().stream()
            .filter(AbstractWidget.class::isInstance)
            .map(AbstractWidget.class::cast)
            .toList();

        AbstractWidget singleplayer = findByTranslationKey(widgets, SINGLEPLAYER_KEY);
        AbstractWidget changelog = widgets.stream()
            .filter(widget -> translationKey(widget).startsWith(CHANGELOG_KEY_PREFIX))
            .findFirst()
            .orElse(null);
        if (singleplayer == null || changelog == null) return;

        changelog.setWidth(COMPACT_BUTTON_SIZE);
        changelog.setMessage(Component.literal("i"));
        changelog.setTooltip(Tooltip.create(Component.translatable(
            "creating_stories_workshop.menu.changelog_tooltip")));

        int leftX = singleplayer.getX() - COMPACT_BUTTON_SIZE - BUTTON_GAP;
        int rightX = singleplayer.getX() + singleplayer.getWidth() + BUTTON_GAP;
        int y = singleplayer.getY();
        changelog.setX(isFree(widgets, changelog, leftX, y) ? leftX : rightX);
        changelog.setY(y);
    }

    private static AbstractWidget findByTranslationKey(List<AbstractWidget> widgets, String key) {
        return widgets.stream()
            .filter(widget -> translationKey(widget).equals(key))
            .findFirst()
            .orElse(null);
    }

    private static String translationKey(AbstractWidget widget) {
        if (widget.getMessage().getContents() instanceof TranslatableContents contents) {
            return contents.getKey();
        }
        return "";
    }

    private static boolean isFree(List<AbstractWidget> widgets, AbstractWidget ignored, int x, int y) {
        int right = x + COMPACT_BUTTON_SIZE;
        int bottom = y + COMPACT_BUTTON_SIZE;
        return widgets.stream().noneMatch(widget -> widget != ignored
            && widget.visible
            && x < widget.getX() + widget.getWidth()
            && right > widget.getX()
            && y < widget.getY() + widget.getHeight()
            && bottom > widget.getY());
    }
}
