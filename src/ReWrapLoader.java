import java.io.File;
import java.io.FileInputStream;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ReWrapLoader {
	public static final String RWLVersion = "v0.1 for b1.7.3";
	public static ArrayList<Object> modlist = new ArrayList<Object>();
	public static URLClassLoader classLoader;
	
	public static void premain(String agentargs, Instrumentation inst) throws Exception
	{
		log("ReWrapLoader agent booted up!");
		log("Initializing mappings...");
		WrapLoaderAPI.mapping = MappingProvider.fromResource("/default_b173.tiny");
		
		log("Initializing API...");
		WrapLoaderAPI.instrumentation = inst;
		WrapLoaderAPI.initTextureIDs();
		Material.init();

		log("Injecting hooks...");
		transformClass("net.minecraft.client.Minecraft", inst, TransformerMinecraft.class);
		transformClass(WrapLoaderAPI.mapping.getClass("net/minecraft/src/GuiMainMenu"), inst, TransformerGuiMainMenu.class);
		transformClass(WrapLoaderAPI.mapping.getClass("net/minecraft/src/TexturePackBase"), inst, TransformerTexturePackBase.class);
		transformClass(WrapLoaderAPI.mapping.getClass("net/minecraft/src/Block"), inst, TransformerBlock.class);

		log("Loading coremods...");
		loadMods(new File("./coremods/"));
		
		log("Loading mods...");
		loadMods(new File("./mods/"));
		
		log("All done, launching Minecraft");
	}
	
	public static void transformClass(String className, Instrumentation instrumentation, Class tr) throws Exception 
	{
		try {
			log("Transforming " + className + "...");
			Class<?> targetCls = null;
			ClassLoader targetClassLoader = null;
	
			try {
				targetCls = Class.forName(className);
				targetClassLoader = targetCls.getClassLoader();
				transform(targetCls, targetClassLoader, instrumentation, tr);
				return;
			} catch (Exception ex) {
				err("Class [" + className + "] not found with Class.forName");
			}
	
			for(Class<?> clazz: instrumentation.getAllLoadedClasses()) {
				if(clazz.getName().equals(className)) {
					targetCls = clazz;
					targetClassLoader = targetCls.getClassLoader();
					transform(targetCls, targetClassLoader, instrumentation, tr);
					return;
				}
			}
			throw new RuntimeException("Failed to find class [" + className + "]");
		}
		catch(Exception e)
		{
			err("An error occurred on " + className + " transformation: " + e);
			err("Stack trace:");
			e.printStackTrace();
		}
	}
	
	private static void transform(Class<?> clazz, ClassLoader classLoader,Instrumentation instrumentation, Class tr) throws Exception {
		Object s = tr.getConstructor(new Class[] {String.class, ClassLoader.class}).newInstance(clazz.getName(), classLoader);
		instrumentation.addTransformer((ClassFileTransformer) s, true);
		try {
			instrumentation.retransformClasses(clazz);
		} catch (Exception ex) {
			throw new RuntimeException("Transform failed for: [" + clazz.getName() + "]", ex);
		}
	}
	
	// TODO: rewrite this legacy shit and do something about classloader leak
	public static Class loadClass(String jar, String classname) throws MalformedURLException, ClassNotFoundException
	{
		File jarf = new File(jar);
	   	
	   	URL url = jarf.toURI().toURL();
	   	URL[] urls = new URL[] {url};
	   	
	   	ClassLoader cl = new URLClassLoader(urls);
	   	Class cls = cl.loadClass(classname);
	   	
	   	return cls;
	}
	   
	public static String getMainModClass(String jar) throws Exception
	{
		JarInputStream js = new JarInputStream(new FileInputStream(jar));
	   	Manifest mf = js.getManifest();
	   	Attributes as = mf.getMainAttributes();
	   	
	   	js.close();
	   	return as.getValue("Wrap-Loader-Class");
	}
	  
	// TODO: rewrite this legacy shit
	public static void loadMods(File folder) throws Exception
	{
		File[] mods = folder.listFiles();
		
		for(File mod : mods)
		{			
			Class classmod = loadClass(mod.getPath(),getMainModClass(mod.getPath()));
			if(Class.forName("BaseMod").isAssignableFrom(classmod))
			{
				try
				{
					Method modmain = classmod.getDeclaredMethod("onEnable", new Class[] {});
					Object modinst = classmod.getConstructor(new Class[] {}).newInstance(new Object[] {});

					modmain.invoke(modinst, new Object[] {});
					log("Successfully loaded " + mod.getName() + "!");
					modlist.add(modinst);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					err("Failed to load mod " + mod.getName() + ": " + ex.getMessage());
				}
			}
			else
			{
				err("Mod " + mod.getName() + " was not an instance of BaseMod, failed to load");
			}
		}
	}
	
	public static void log(String s)
	{
		System.out.println("[WrapLoader Reloaded] [*] " + s);
	}
	public static void err(String s)
	{
		System.out.println("[WrapLoader Reloaded] [!] " + s);
	}
}
