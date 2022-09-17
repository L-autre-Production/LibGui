package io.github.cottonmc.cotton.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import io.github.cottonmc.cotton.gui.client.LibGui;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WMultilineLabel extends WLabel {

	private static int LINE_HEIGHT = 18;
	private String textString;
	private Style style;

	public WMultilineLabel(Text text, int color) {
		super(text, color);
		this.textString = text.getString();
	}

	public WMultilineLabel(Text text) {
		super(text);
		this.textString = text.getString();
	}

	public void setText(String textString) {
		this.textString = textString;
	}

	public void setStyle(Style style) {
		this.style = style;
	}

	@Override
	public boolean canResize() {
		return true;
	}

	public List<String> getLines() {
		MinecraftClient mc = MinecraftClient.getInstance();
		TextRenderer renderer = mc.textRenderer;

		List<String> lines = new ArrayList<>();
		int currentLine = 0;
		lines.add(textString);

		while (currentLine <= lines.size() - 1) {
			while (renderer.getWidth(lines.get(currentLine)) > getWidth()) {
				String line = lines.get(currentLine);
				List<String> words = Arrays.asList(line.split(" "));
				if (words.size() <= 1) {
					break;
				}
				String lastWord = words.get(words.size() - 1);
				// Add word to next line
				if (currentLine + 1 >= lines.size()) {
					lines.add(lastWord);
				} else {
					lines.set(currentLine + 1, String.format("%s %s", lastWord, lines.get(currentLine + 1)));
				}
				// Remove word from current line
				lines.set(currentLine, words.subList(0, words.size() - 1).stream().reduce(null,
						(a, b) -> a == null ? b : a + " " + b));
			}
			currentLine++;
		}

		return lines;
	}

	public void drawText(Text text, MatrixStack matrices, int x, int y) {
		MinecraftClient mc = MinecraftClient.getInstance();
		TextRenderer renderer = mc.textRenderer;
		int yOffset = switch (verticalAlignment) {
			case CENTER -> height / 2 - renderer.fontHeight / 2;
			case BOTTOM -> height - renderer.fontHeight;
			case TOP -> 0;
		};

		ScreenDrawing.drawString(matrices, text.asOrderedText(), horizontalAlignment, x, y + yOffset, this.getWidth(),
				LibGui.isDarkMode() ? darkmodeColor : color);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
		int lineIndex = 0;
		for (String line : getLines()) {
			if (line != null) {
				drawText(style == null ? Text.of(line) : Text.of(line).getWithStyle(style).get(0), matrices, x,
						y + lineIndex * LINE_HEIGHT);
			}
			lineIndex++;
		}

		Style hoveredTextStyle = getTextStyleAt(mouseX, mouseY);
		ScreenDrawing.drawTextHover(matrices, hoveredTextStyle, x + mouseX, y + mouseY);
	}

	@Override
	public void setSize(int x, int y) {
		this.width = x;
		this.height = y;
	}

}
