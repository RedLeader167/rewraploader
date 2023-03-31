import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class TextureOverride {
	private BufferedImage image;
	int textureID;
	
	public TextureOverride(BufferedImage img, int texID) {
		image = img;
		textureID = texID;
	}
	
	public void apply(Graphics2D graphics)
	{
		int x = computeX(textureID);
		int y = computeY(textureID);
		graphics.drawImage(image, x, y, x+16, y+16, 0, 0, 16, 16, null);
	}
	
	public static int computeX(int textureID)
	{
		return (textureID % 16) * 16;
	}
	
	public static int computeY(int textureID)
	{
		int y = 0;
		while(textureID > 9)
		{
			y += 1;
			textureID -= 10;
		}
		return y * 16;
	}
}
