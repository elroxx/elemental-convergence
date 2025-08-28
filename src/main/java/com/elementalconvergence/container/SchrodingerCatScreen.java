package com.elementalconvergence.container;

import com.elementalconvergence.networking.RerollPacket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SchrodingerCatScreen extends HandledScreen<SchrodingerCatScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/generic_54.png");
    private ButtonWidget rerollButton;

    public SchrodingerCatScreen(SchrodingerCatScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        // Add reroll button
        int buttonX = this.x + this.backgroundWidth + 5;
        int buttonY = this.y + 20;

        rerollButton = ButtonWidget.builder(Text.literal("Reroll"), button -> {
                    // Send reroll packet to server
                    RerollPacket.send();
                })
                .dimensions(buttonX, buttonY, 60, 20)
                .build();

        this.addDrawableChild(rerollButton);
        updateRerollButton();
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        updateRerollButton();
    }

    private void updateRerollButton() {
        if (rerollButton != null) {
            rerollButton.active = handler.canReroll();
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, 3 * 18 + 17);
        context.drawTexture(TEXTURE, x, y + 3 * 18 + 17, 0, 126, this.backgroundWidth, 96);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Draw additional UI text
        int textX = this.x + this.backgroundWidth + 5;
        int textY = this.y + 45;

        context.drawText(this.textRenderer,
                Text.literal("Preview Mode"),
                textX, textY, 0x404040, false);

        context.drawText(this.textRenderer,
                Text.literal("Items cannot"),
                textX, textY + 15, 0x808080, false);

        context.drawText(this.textRenderer,
                Text.literal("be taken"),
                textX, textY + 25, 0x808080, false);

        if (!handler.canReroll()) {
            context.drawText(this.textRenderer,
                    Text.literal("Already rerolled"),
                    textX, textY + 45, 0xFF4040, false);
        }

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
