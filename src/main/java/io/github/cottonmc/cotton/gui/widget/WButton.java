package io.github.cottonmc.cotton.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import io.github.cottonmc.cotton.gui.client.LibGui;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.impl.client.NarrationMessages;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import org.jetbrains.annotations.Nullable;

public class WButton extends WWidget {
	private static final Identifier DARK_WIDGETS_LOCATION = new Identifier("libgui",
			"textures/widget/dark_widgets.png");

	private Text label;
	private Text tooltip;
	protected int color = WLabel.DEFAULT_TEXT_COLOR;
	protected int darkmodeColor = WLabel.DEFAULT_TEXT_COLOR;
	private boolean enabled = true;
	protected HorizontalAlignment alignment = HorizontalAlignment.CENTER;

	@Nullable
	private Runnable onClick;
	@Nullable
	private Icon icon = null;

	/**
	 * Constructs a button with no label and no icon.
	 */
	public WButton() {

	}

	/**
	 * Constructs a button with an icon.
	 *
	 * @param icon the icon
	 * @since 2.2.0
	 */
	public WButton(Icon icon) {
		this.icon = icon;
	}

	/**
	 * Constructs a button with a label.
	 *
	 * @param label the label
	 */
	public WButton(Text label) {
		this.label = label;
	}

	/**
	 * Constructs a button with an icon and a label.
	 *
	 * @param icon  the icon
	 * @param label the label
	 * @since 2.2.0
	 */
	public WButton(Icon icon, Text label) {
		this.icon = icon;
		this.label = label;
	}

	@Override
	public boolean canResize() {
		return true;
	}

	@Override
	public boolean canFocus() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
		int state = 1; // 1=regular. 2=hovered. 0=disabled.
		if (!enabled) {
			state = 0;
		} else if (hovered || isFocused()) {
			state = 2;
		}

		float px = 1 / 256f;
		float buttonLeft = 0 * px;
		float buttonTop = (46 + (state * 20)) * px;
		int halfWidth = getWidth() / 2;
		if (halfWidth > 198) {
			halfWidth = 198;
		}
		float buttonWidth = halfWidth * px;
		float buttonHeight = 20 * px;

		float buttonEndLeft = (200 - (getWidth() / 2)) * px;

		Identifier texture = getTexture();
		ScreenDrawing.texturedRect(matrices, x, y, getWidth() / 2, 20, texture, buttonLeft, buttonTop,
				buttonLeft + buttonWidth, buttonTop + buttonHeight, 0xFFFFFFFF);
		ScreenDrawing.texturedRect(matrices, x + (getWidth() / 2), y, getWidth() / 2, 20, texture, buttonEndLeft,
				buttonTop, 200 * px, buttonTop + buttonHeight, 0xFFFFFFFF);

		if (icon != null) {
			icon.paint(matrices, x + 1, y + 1, 16);
		}

		if (label != null) {
			int color = 0xE0E0E0;
			if (!enabled) {
				color = 0xA0A0A0;
			} /*
				 * else if (hovered) {
				 * color = 0xFFFFA0;
				 * }
				 */

			int xOffset = (icon != null && alignment == HorizontalAlignment.LEFT) ? 18 : 0;
			ScreenDrawing.drawStringWithShadow(matrices, label.asOrderedText(), alignment, x + xOffset,
					y + ((20 - 8) / 2), width, color); // LibGuiClient.config.darkMode ? darkmodeColor : color);
		}
	}

	@Override
	public void setSize(int x, int y) {
		super.setSize(x, 20);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public InputResult onClick(int x, int y, int button) {
		super.onClick(x, y, button);

		if (enabled && isWithinBounds(x, y)) {
			MinecraftClient.getInstance().getSoundManager()
					.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));

			if (onClick != null) {
				onClick.run();
			}
			return InputResult.PROCESSED;
		}

		return InputResult.IGNORED;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void onKeyPressed(int ch, int key, int modifiers) {
		if (isActivationKey(ch)) {
			onClick(0, 0, 0);
		}
	}

	/**
	 * Gets the click handler of this button.
	 *
	 * @return the click handler
	 * @since 2.2.0
	 */
	@Nullable
	public Runnable getOnClick() {
		return onClick;
	}

	/**
	 * Sets the click handler of this button.
	 *
	 * @param onClick the new click handler
	 * @return this button
	 */
	public WButton setOnClick(@Nullable Runnable onClick) {
		this.onClick = onClick;
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public WButton setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public Text getLabel() {
		return label;
	}

	public WButton setLabel(Text label) {
		this.label = label;
		return this;
	}

	public WButton setTooltip(Text tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	public HorizontalAlignment getAlignment() {
		return alignment;
	}

	public WButton setAlignment(HorizontalAlignment alignment) {
		this.alignment = alignment;
		return this;
	}

	/**
	 * Gets the icon of this button.
	 *
	 * @return the icon
	 * @since 2.2.0
	 */
	@Nullable
	public Icon getIcon() {
		return icon;
	}

	/**
	 * Sets the icon of this button.
	 *
	 * @param icon the new icon
	 * @return this button
	 * @since 2.2.0
	 */
	public WButton setIcon(@Nullable Icon icon) {
		this.icon = icon;
		return this;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void addNarrations(NarrationMessageBuilder builder) {
		builder.put(NarrationPart.TITLE, ClickableWidget.getNarrationMessage(getLabel()));

		if (isEnabled()) {
			if (isFocused()) {
				builder.put(NarrationPart.USAGE, NarrationMessages.Vanilla.BUTTON_USAGE_FOCUSED);
			} else if (isHovered()) {
				builder.put(NarrationPart.USAGE, NarrationMessages.Vanilla.BUTTON_USAGE_HOVERED);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static Identifier getTexture() {
		return LibGui.isDarkMode() ? DARK_WIDGETS_LOCATION : ClickableWidget.WIDGETS_TEXTURE;
	}

	@Override
	public void addTooltip(TooltipBuilder tooltip) {
		if (this.tooltip != null) {
			tooltip.add(this.tooltip);
		}
	}
}
