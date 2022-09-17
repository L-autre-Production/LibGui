package io.github.cottonmc.cotton.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.impl.client.NarrationMessages;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WTextArea extends WWidget {
	public static final int TEXT_PADDING_X = 4;
	public static final int TEXT_PADDING_Y = 6;
	public static final int CURSOR_PADDING_Y = 4;
	public static final int CURSOR_HEIGHT = 12;
	public static final int LINE_HEIGHT = 12 + 1;
	public static final int PADDING = 1;

	@Environment(EnvType.CLIENT)
	private TextRenderer font;

	// private String text = "";
	private List<String> lines = new ArrayList<String>();
	private int selectedLine = 0;
	private int maxLines = 4;
	// private int maxLength = 16;
	private boolean editable = true;
	private int tickCount = 0;

	private int disabledColor = 0x707070;
	private int enabledColor = 0x303030;
	private int suggestionColor = 0x808080;

	private static final int BACKGROUND_COLOR = 0x557a7a7a;
	private static final int BORDER_COLOR_SELECTED = 0x00FFFFA0;
	private static final int BORDER_COLOR_UNSELECTED = 0x00A0A0A0;
	private static final int CURSOR_COLOR = 0xFF919191;

	@Nullable
	private Text suggestion = null;

	// Index of the leftmost character to be rendered.
	private int scrollOffset = 0;

	private int cursor = 0;
	/**
	 * If not -1, select is the "anchor point" of a selection. That is, if you hit
	 * shift+left with no existing
	 * selection, the selection will be anchored to where you were, but the cursor
	 * will move left, expanding the
	 * selection as you continue to move left. If you move to the right, eventually
	 * you'll overtake the anchor, drop the
	 * anchor at the same place and start expanding the selection rightwards
	 * instead.
	 */
	private int select = -1;

	private Consumer<String> onChanged;

	private Predicate<String> textPredicate;

	@Environment(EnvType.CLIENT)
	@Nullable
	private BackgroundPainter backgroundPainter;

	public WTextArea() {
	}

	public WTextArea(Text suggestion) {
		this.suggestion = suggestion;
	}

	/**
	 * Sets the text of this text field.
	 * If the text is more than the {@linkplain #getMaxLength() max length},
	 * it'll be shortened to the max length.
	 *
	 * @param s the new text
	 */
	public void setText(String s) {
		// setTextWithResult(s);
		parseTextIntoLines(s);
	}

	public String getLine(int line) {
		if (line > lines.size() - 1)
			return "";
		if (lines.get(line) == null)
			return "";
		return lines.get(line);
	}

	public void setLines(String text) {
		List<String> data = Arrays.asList(text.split("\n"));
		for (int i = 0; i < data.size(); i++) {
			setLine(i, data.get(i));
		}
	}

	public void setLine(int line, String text) {
		if (line < 0 || line >= maxLines)
			return;
		if (lines.size() - 1 < line) {
			for (int i = Math.max(lines.size(), 0); i <= line; i++) {
				lines.add(i, "");
			}
		}
		lines.set(line, text);
		if (onChanged != null)
			onChanged.accept(getText());
	}

	public int getLineMaxWidth() {
		return getWidth() - TEXT_PADDING_X * 2;
	}

	private void parseTextIntoLines(String s) {
		if (this.font == null)
			this.font = MinecraftClient.getInstance().textRenderer;
		String[] words = s.split(" ");
		int lineIndex = 0;
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			String line = getLine(lineIndex);
			if (line != null)
				line = line + " ";
			if (this.font.getWidth(line + word) > getLineMaxWidth()) {
				lineIndex++;
				if (lineIndex == maxLines - 1)
					break;
				setLine(lineIndex, word);
			} else {
				setLine(lineIndex, line + word);
			}
		}
	}

	// private boolean setTextWithResult(String s) {
	// if (this.textPredicate == null || this.textPredicate.test(s)) {
	// this.text = (s.length() > maxLength) ? s.substring(0, maxLength) : s;
	// // Call change listener
	// if (onChanged != null) onChanged.accept(this.text);
	// // Reset cursor if needed
	// if (cursor >= this.text.length()) cursor = this.text.length() - 1;
	// return true;
	// }

	// return false;
	// }

	/**
	 * {@return the text in this text field}
	 */
	public String getText() {
		// return this.text;
		return this.lines.stream().reduce(null, (s1, s2) -> {
			if (s1 == null)
				return s2;
			else
				return s1 + "\n" + s2;
		});
	}

	@Override
	public boolean canResize() {
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		this.tickCount++;
	}

	@Override
	public void setSize(int x, int y) {
		super.setSize(x, y);
	}

	public void setCursorPos(int location) {
		// TODO
		// this.cursor = MathHelper.clamp(location, 0, this.text.length());
		// scrollCursorIntoView();
	}

	// public int getMaxLength() {
	// return this.maxLength;
	// }

	public int getCursor() {
		return this.cursor;
	}

	// @Environment(EnvType.CLIENT)
	// public void scrollCursorIntoView() {
	// if (scrollOffset > cursor) {
	// scrollOffset = cursor;
	// }
	// if (scrollOffset < cursor && font.trimToWidth(text.substring(scrollOffset),
	// width - TEXT_PADDING_X * 2).length()
	// + scrollOffset < cursor) {
	// scrollOffset = cursor;
	// }

	// checkScrollOffset();
	// }

	// @Environment(EnvType.CLIENT)
	// private void checkScrollOffset() {
	// int rightMostScrollOffset = text.length() - font.trimToWidth(text, width -
	// TEXT_PADDING_X * 2, true).length();
	// scrollOffset = Math.min(rightMostScrollOffset, scrollOffset);
	// }

	@Nullable
	public String getSelection() {
		if (select < 0)
			return null;
		if (select == cursor)
			return null;

		String text = getLine(selectedLine);

		// Tidy some things
		if (select > text.length())
			select = text.length();
		if (cursor < 0)
			cursor = 0;
		if (cursor > text.length())
			cursor = text.length();

		int start = Math.min(select, cursor);
		int end = Math.max(select, cursor);

		return text.substring(start, end);
	}

	public boolean isEditable() {
		return this.editable;
	}

	@Environment(EnvType.CLIENT)
	protected void renderBox(MatrixStack matrices, int x, int y) {
		int borderColor = this.isFocused() ? BORDER_COLOR_SELECTED : BORDER_COLOR_UNSELECTED;
		ScreenDrawing.coloredRect(matrices, x - 1, y - 1, width + 2, height + 2, borderColor);
		ScreenDrawing.coloredRect(matrices, x + PADDING, y + PADDING, width - 2 * PADDING, height - 2 * PADDING,
				BACKGROUND_COLOR);
	}

	@Environment(EnvType.CLIENT)
	protected void renderText(MatrixStack matrices, int x, int y) {
		int textColor = this.editable ? this.enabledColor : this.disabledColor;
		for (int i = 0; i < lines.size(); i++) {
			String line = getLine(i);
			this.font.draw(matrices, line, x + TEXT_PADDING_X, y + TEXT_PADDING_Y + i * LINE_HEIGHT,
					textColor);
		}
	}

	@Environment(EnvType.CLIENT)
	protected void renderCursor(MatrixStack matrices, int x, int y) {
		if (this.tickCount / 6 % 2 == 0)
			return;
		if (this.cursor < this.scrollOffset)
			return;
		// if (this.cursor > this.scrollOffset + visibleText.length())
		// return;
		String text = getLine(selectedLine);
		int cursorOffset = this.font
				.getWidth(text.substring(0, Math.min(this.cursor - this.scrollOffset, text.length())));
		ScreenDrawing.coloredRect(matrices, x + TEXT_PADDING_X + cursorOffset,
				y + CURSOR_PADDING_Y + selectedLine * LINE_HEIGHT, 1, CURSOR_HEIGHT,
				CURSOR_COLOR);
	}

	@Environment(EnvType.CLIENT)
	protected void renderSuggestion(MatrixStack matrices, int x, int y) {
		if (this.suggestion == null)
			return;
		this.font.draw(matrices, this.suggestion, x + TEXT_PADDING_X, y + TEXT_PADDING_Y,
				this.suggestionColor);
	}

	@Environment(EnvType.CLIENT)
	protected void renderSelection(MatrixStack matrices, int x, int y) {
		if (select == cursor || select == -1)
			return;

		String text = getLine(selectedLine);

		int textLength = text.length();

		int left = Math.min(cursor, select);
		int right = Math.max(cursor, select);

		if (right < scrollOffset || left > scrollOffset + textLength)
			return;

		int normalizedLeft = Math.max(scrollOffset, left) - scrollOffset;
		int normalizedRight = Math.min(scrollOffset + textLength, right) - scrollOffset;

		int leftCaret = font.getWidth(text.substring(0, normalizedLeft));
		int selectionWidth = font.getWidth(text.substring(normalizedLeft, normalizedRight));

		invertedRect(matrices, x + TEXT_PADDING_X + leftCaret, y + CURSOR_PADDING_Y + selectedLine * LINE_HEIGHT,
				selectionWidth, CURSOR_HEIGHT);
	}

	private boolean hasText() {
		for (String line : lines) {
			if (line != null && !line.isEmpty() && !line.isBlank())
				return true;
		}
		return false;
	}

	@Environment(EnvType.CLIENT)
	protected void renderTextField(MatrixStack matrices, int x, int y) {
		if (this.font == null)
			this.font = MinecraftClient.getInstance().textRenderer;

		// checkScrollOffset();
		// String visibleText = font.trimToWidth(this.text.substring(this.scrollOffset),
		// this.width - 2 * TEXT_PADDING_X);
		renderBox(matrices, x, y);
		renderText(matrices, x, y);
		if (!this.hasText() && !this.isFocused()) {
			renderSuggestion(matrices, x, y);
		}
		if (this.isFocused()) {
			renderCursor(matrices, x, y);
		}
		renderSelection(matrices, x, y);
	}

	@Environment(EnvType.CLIENT)
	private void invertedRect(MatrixStack matrices, int x, int y, int width, int height) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		Matrix4f model = matrices.peek().getPositionMatrix();
		RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.disableTexture();
		RenderSystem.enableColorLogicOp();
		RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
		buffer.vertex(model, x, y + height, 0).next();
		buffer.vertex(model, x + width, y + height, 0).next();
		buffer.vertex(model, x + width, y, 0).next();
		buffer.vertex(model, x, y, 0).next();
		BufferRenderer.drawWithShader(buffer.end());
		RenderSystem.disableColorLogicOp();
		RenderSystem.enableTexture();
	}

	public WTextArea setTextPredicate(Predicate<String> predicate_1) {
		this.textPredicate = predicate_1;
		return this;
	}

	public WTextArea setChangedListener(Consumer<String> listener) {
		this.onChanged = listener;
		return this;
	}

	// public WTextArea setMaxLength(int max) {
	// this.maxLength = max;
	// if (this.text.length() > max) {
	// setText(this.text.substring(0, max));
	// }
	// return this;
	// }

	public WTextArea setMaxLines(int max) {
		this.maxLines = max;
		if (this.lines.size() > max) {
			this.lines = this.lines.subList(0, max);
		}
		return this;
	}

	public WTextArea setEnabledColor(int col) {
		this.enabledColor = col;
		return this;
	}

	public WTextArea setSuggestionColor(int suggestionColor) {
		this.suggestionColor = suggestionColor;
		return this;
	}

	public WTextArea setDisabledColor(int col) {
		this.disabledColor = col;
		return this;
	}

	public WTextArea setEditable(boolean editable) {
		this.editable = editable;
		return this;
	}

	@Nullable
	public Text getSuggestion() {
		return suggestion;
	}

	/**
	 * @deprecated Use {@link #setSuggestion(Text)} instead.
	 */
	@Deprecated(forRemoval = true, since = "5.4.0")
	@ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
	public WTextArea setSuggestion(@Nullable String suggestion) {
		this.suggestion = suggestion != null ? Text.literal(suggestion) : null;
		return this;
	}

	public WTextArea setSuggestion(@Nullable Text suggestion) {
		this.suggestion = suggestion;
		return this;
	}

	@Environment(EnvType.CLIENT)
	public WTextArea setBackgroundPainter(BackgroundPainter painter) {
		this.backgroundPainter = painter;
		return this;
	}

	public boolean canFocus() {
		return true;
	}

	@Override
	public void onFocusGained() {
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		renderTextField(matrices, x, y);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public InputResult onClick(int x, int y, int button) {
		if (!isEditable())
			return InputResult.IGNORED;
		requestFocus();
		setCaretPosition(x - TEXT_PADDING_X, y - TEXT_PADDING_Y);
		// scrollCursorIntoView();
		return InputResult.PROCESSED;
	}

	@Environment(EnvType.CLIENT)
	public void setCaretPosition(int clickX, int clickY) {
		if (clickX < 0) {
			cursor = 0;
			return;
		}
		// Line
		selectedLine = MathHelper.clamp(clickY / LINE_HEIGHT, 0, maxLines - 1);
		String text = getLine(selectedLine);
		// Cursor
		int lastPos = 0;
		// checkScrollOffset();
		String string = text.substring(scrollOffset);
		for (int i = 0; i < string.length(); i++) {
			int w = font.getWidth(string.charAt(i) + "");
			if (lastPos + w >= clickX) {
				if (clickX - lastPos < w / 2) {
					cursor = i + scrollOffset;
					return;
				}
			}
			lastPos += w;
		}
		cursor = string.length();
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void onCharTyped(char ch) {
		insertText(ch + "");
	}

	@Environment(EnvType.CLIENT)
	private void insertText(String toInsert) {
		if (!isEditable())
			return;

		String before, after;
		String text = getLine(selectedLine);
		cursor = MathHelper.clamp(cursor, 0, text.length());
		if (select != -1 && select != cursor) {
			int left = Math.min(cursor, select);
			int right = Math.max(cursor, select);
			before = text.substring(0, left);
			after = text.substring(right);
		} else {
			before = text.substring(0, cursor);
			after = text.substring(cursor);
		}

		if (this.font.getWidth(before + toInsert + after) > getLineMaxWidth()) {
			if (selectedLine + 1 >= maxLines)
				return;
			String[] newWords = (before + toInsert + after).split(" ");
			String lastWord = newWords[newWords.length - 1];
			if (getLine(selectedLine + 1) == null || getLine(selectedLine + 1).isEmpty()
					|| this.font.getWidth(lastWord + " " + getLine(selectedLine + 1)) < getLineMaxWidth()) {
				String newLine = Arrays.asList(newWords).subList(0, newWords.length - 1).stream().reduce(null,
						(s1, s2) -> {
							if (s1 == null)
								return s2;
							else
								return s1 + " " + s2;
						});
				setLine(selectedLine, newLine);
				setLine(selectedLine + 1, lastWord);
				select = -1;
				cursor = (before + toInsert).length();
				if (newLine != null && cursor > newLine.length()) {
					cursor = cursor - newLine.length() - 1;
					selectedLine++;
				}
			}
		} else {
			select = -1;
			cursor = (before + toInsert).length();
			setLine(selectedLine, before + toInsert + after);
		}
	}

	@Environment(EnvType.CLIENT)
	private void copySelection() {
		String selection = getSelection();
		if (selection != null) {
			MinecraftClient.getInstance().keyboard.setClipboard(selection);
		}
	}

	@Environment(EnvType.CLIENT)
	private void paste() {
		String clip = MinecraftClient.getInstance().keyboard.getClipboard();
		insertText(clip);
	}

	@Environment(EnvType.CLIENT)
	private void deleteSelection() {
		int left = Math.min(cursor, select);
		int right = Math.max(cursor, select);

		String text = getLine(selectedLine);
		setLine(selectedLine, text.substring(0, left) + text.substring(right));
		select = -1;
		cursor = left;

		// if (setTextWithResult(text.substring(0, left) + text.substring(right))) {
		// select = -1;
		// cursor = left;
		// scrollCursorIntoView();
		// }
	}

	@Environment(EnvType.CLIENT)
	private void delete(int modifiers, boolean backwards) {
		if (select == -1 || select == cursor) {
			select = skipCharaters((GLFW.GLFW_MOD_CONTROL & modifiers) != 0, backwards ? -1 : 1);
		}
		deleteSelection();
	}

	@Environment(EnvType.CLIENT)
	private int skipCharaters(boolean skipMany, int direction) {
		if (direction != -1 && direction != 1)
			return cursor;
		int position = cursor;
		String text = getLine(selectedLine);
		while (true) {
			position += direction;
			if (position < 0) {
				return 0;
			}
			if (position > text.length()) {
				return text.length();
			}
			if (!skipMany)
				return position;
			if (position < text.length() && Character.isWhitespace(text.charAt(position))) {
				return position;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public void onDirectionalKey(int direction, int modifiers) {
		if ((GLFW.GLFW_MOD_SHIFT & modifiers) != 0) {
			if (select == -1 || select == cursor)
				select = cursor;
			cursor = skipCharaters((GLFW.GLFW_MOD_CONTROL & modifiers) != 0, direction);
		} else {
			if (select != -1) {
				cursor = direction < 0 ? Math.min(cursor, select) : Math.max(cursor, select);
				select = -1;
			} else {
				cursor = skipCharaters((GLFW.GLFW_MOD_CONTROL & modifiers) != 0, direction);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public void onVerticalDirectionalKey(int direction) {
		int currentLine = selectedLine;
		selectedLine = MathHelper.clamp(selectedLine + direction, 0, maxLines - 1);
		select = -1;
		if (selectedLine != currentLine) {
			int x = this.font.getWidth(getLine(currentLine).substring(0, cursor));
			int y = LINE_HEIGHT * selectedLine + LINE_HEIGHT / 2;
			setCaretPosition(x, y);
		}
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void onKeyPressed(int ch, int key, int modifiers) {
		if (!isEditable())
			return;

		String text = getLine(selectedLine);
		if (Screen.isCopy(ch)) {
			copySelection();
			return;
		} else if (Screen.isPaste(ch)) {
			paste();
			return;
		} else if (Screen.isSelectAll(ch)) {
			select = 0;
			cursor = text.length();
			return;
		}

		switch (ch) {
			case GLFW.GLFW_KEY_DELETE -> delete(modifiers, false);
			case GLFW.GLFW_KEY_BACKSPACE -> delete(modifiers, true);
			case GLFW.GLFW_KEY_LEFT -> onDirectionalKey(-1, modifiers);
			case GLFW.GLFW_KEY_RIGHT -> onDirectionalKey(1, modifiers);
			case GLFW.GLFW_KEY_UP -> onVerticalDirectionalKey(-1);
			case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_ENTER -> onVerticalDirectionalKey(1);
			case GLFW.GLFW_KEY_HOME -> {
				if ((GLFW.GLFW_MOD_SHIFT & modifiers) == 0) {
					select = -1;
				}
				cursor = 0;
			}
			case GLFW.GLFW_KEY_END -> {
				if ((GLFW.GLFW_MOD_SHIFT & modifiers) == 0) {
					select = -1;
				}
				cursor = text.length();
			}
		}
		// scrollCursorIntoView();
	}

	@Override
	public void addNarrations(NarrationMessageBuilder builder) {
		String text = getLine(selectedLine);
		builder.put(NarrationPart.TITLE, Text.translatable(NarrationMessages.TEXT_FIELD_TITLE_KEY, text));

		if (suggestion != null) {
			builder.put(NarrationPart.HINT,
					Text.translatable(NarrationMessages.TEXT_FIELD_SUGGESTION_KEY, suggestion));
		}
	}
}
