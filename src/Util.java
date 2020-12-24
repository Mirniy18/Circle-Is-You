import java.awt.*;
import java.awt.image.BufferedImage;

public final class Util {
	private Util() {}

	public static BufferedImage createImageFromText(String text, int width, int height, Color color, Color background, Font font) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = image.createGraphics();

		g.setColor(background);
		g.fillRect(0, 0, width, height);

		g.setColor(color);
		g.setFont(font);

		int textWidth = g.getFontMetrics(font).stringWidth(text);

		int textHeight = font.createGlyphVector(g.getFontRenderContext(), text)
				.getPixelBounds(null, 0, 0).height;

		g.drawString(text, (width - textWidth) / 2, (height + textHeight) / 2);

		return image;
	}
}
