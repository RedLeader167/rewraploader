import java.util.ArrayList;
import java.util.HashMap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class WrapLoaderAPI {
	public static HashMap<String, ArrayList<Method>> events = new HashMap<String, ArrayList<Method>>();
	public static int[] textureIDsToBlockIDs = new int[256];
	public static MappingProvider mapping;
	public static HashMap<String, ArrayList<TextureOverride>> textureOverrides = new HashMap<>();
	public static Instrumentation instrumentation;
	
	public static void dispatchEvent(Event e)
	{
		ArrayList<Method> handlers = events.get(e.getEventID());
		if(handlers == null)
		{
			return;
		}
		
		for(Method handler : handlers)
		{
			try {
				handler.invoke(null, new Object[] {e});
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public static void registerCallback(String event, Method callback)
	{
		ArrayList<Method> handlers = events.get(event);
		if(handlers == null)
		{
			events.put(event, new ArrayList<Method>());
		}
		events.get(event).add(callback);
	}
	
	public static void registerListener(Class<?> clazz)
	{
		Method[] methods = clazz.getMethods();
		for(Method method : methods)
		{
			Parameter[] params = method.getParameters();
			if(params.length != 1)
				continue;
			Class<?> paramClass = params[0].getType();
			if(Event.class.isAssignableFrom(paramClass))
			{
				WrapLoaderAPI.registerCallback(paramClass.getName(), method);
			}
		}
	}
	
	public static int getFreeTextureID()
	{
		int i;
		for(i = 0; i < 256; i++)
		{
			if(i == 0 || i == 5) continue;
			if(textureIDsToBlockIDs[i] == 0) break;
		}
		return i;
	}
	public static int getFreeBlockID() throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		Class<?> blockClass = Class.forName(WrapLoaderAPI.mapping.getClass("net/minecraft/src/Block"));
		Object[] s = (Object[])blockClass.getField(WrapLoaderAPI.mapping.getField("net/minecraft/src/Block", "blocksList")).get(null);
		int i;
		for(i = 0; i < 256; i++)
		{
			if(i == 0) continue;
			if(s[i] == null) break;
		}
		return i;
	}
	
	public static void registerTerrainOverride(int textureID, BufferedImage img)
	{
		if(WrapLoaderAPI.textureOverrides.get("/terrain.png") == null)
		{
			WrapLoaderAPI.textureOverrides.put("/terrain.png", new ArrayList<TextureOverride>());
		}
		WrapLoaderAPI.textureOverrides.get("/terrain.png").add(new TextureOverride(img, textureID));
	}
	
	public static InputStream __JTerrainPngOverride(String string1) throws ClassNotFoundException, IOException
	{
			java.io.InputStream stream = Class.forName(WrapLoaderAPI.mapping.getClass("net/minecraft/src/TexturePackBase")).getResourceAsStream(string1);
			if(!string1.equals("/terrain.png"))
			{
				return stream;
			}
			java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(stream);
			java.awt.Graphics2D g = img.createGraphics();
			for(TextureOverride texovr : WrapLoaderAPI.textureOverrides.get("/terrain.png"))
			{
				ReWrapLoader.log("Texture " + string1 + " requested, overriding texture for ID " + texovr.textureID);
				texovr.apply(g);
			}
			java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
			javax.imageio.ImageIO.write(img, "png", os);
			java.io.InputStream is = new java.io.ByteArrayInputStream(os.toByteArray());
			return is;
	}
	
	public static void addRecipe(ItemStack result, Object[] recipe) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		ReWrapLoader.log("Registered a recipe for " + result + " with " + recipe);
		Class<?> craftingManager = Class.forName(mapping.getClass("net/minecraft/src/CraftingManager"));
		Method getInstance = craftingManager.getMethod(mapping.getMethod("net/minecraft/src/CraftingManager", "getInstance", "()Lhk;"), new Class[]{});
		Method addRecipe = craftingManager.getMethod(mapping.getMethod("net/minecraft/src/CraftingManager", "addRecipe", "(Liz;[Ljava/lang/Object;)V"), new Class[] {Class.forName(mapping.getClass("net/minecraft/src/ItemStack")), Object[].class});
		Object craftingManagerInstance = getInstance.invoke(null, new Object[]{});
		
		addRecipe.invoke(craftingManagerInstance, new Object[] {result.getMC(), recipe});
	}
	
	public static Object createBlock(Block baseBlock) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		ReWrapLoader.log("Registered a block with bid " + baseBlock.getBlockID() + " and tid " + baseBlock.getTextureID());
		Class<?> blockClass = Class.forName(mapping.getClass("net/minecraft/src/Block"));
		Class<?> itemClass = Class.forName(mapping.getClass("net/minecraft/src/Item"));
		Class<?> itemBlockClass = Class.forName(mapping.getClass("net/minecraft/src/ItemBlock"));
		Constructor<?> blockConstructor = blockClass.getDeclaredConstructor(new Class[] {int.class, int.class, Class.forName(mapping.getClass("net/minecraft/src/Material"))});
		Object mcMaterial = baseBlock.getMaterial().getMC();
		Object block = blockConstructor.newInstance(baseBlock.getBlockID(), baseBlock.getTextureID(), mcMaterial);
		
		Method setResistance = blockClass.getDeclaredMethod(mapping.getMethod("net/minecraft/src/Block", "setResistance", "(F)Luu;"), new Class[] {float.class});
		Method setHardness = blockClass.getDeclaredMethod(mapping.getMethod("net/minecraft/src/Block", "setHardness", "(F)Luu;"), new Class[] {float.class});
		
		setResistance.setAccessible(true);
		setHardness.setAccessible(true);
		
		setResistance.invoke(block, new Object[] {baseBlock.getResistance()});
		setHardness.invoke(block, new Object[] {baseBlock.getHardness()});
		
		Field itemsList = itemClass.getField(mapping.getField("net/minecraft/src/Item", "itemsList"));
		Field blocksList = blockClass.getField(mapping.getField("net/minecraft/src/Block", "blocksList"));
		
		if(((Object[])blocksList.get(null))[baseBlock.getBlockID()] != null && ((Object[])itemsList.get(null))[baseBlock.getBlockID()] == null) {
			((Object[])itemsList.get(null))[baseBlock.getBlockID()] = itemBlockClass.getDeclaredConstructor(new Class[] {int.class}).newInstance(new Object[] {baseBlock.getBlockID() - 256});
		}
		
		return block;
	}

	public static void initTextureIDs() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Class<?> blockClass = Class.forName(mapping.getClass("net/minecraft/src/Block"));
		for(Object block : (Object[])blockClass.getField(mapping.getField("net/minecraft/src/Block", "blocksList")).get(null))
		{
			if(block != null)
				WrapLoaderAPI.textureIDsToBlockIDs[(int)blockClass.getField(mapping.getField("net/minecraft/src/Block", "blockIndexInTexture")).get(block)] = (int)blockClass.getField(mapping.getField("net/minecraft/src/Block", "blockID")).get(block);
		}
	}

}
