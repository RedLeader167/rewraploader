
public class Material {
	private Object mcMaterial;
	
	public static Class mcMaterialClass;
	public static Material rock;
	
	public Material(Object material)
	{
		mcMaterial = material;
	}
	
	public static void init()
	{
		try {
			mcMaterialClass = Class.forName(WrapLoaderAPI.mapping.getClass("net/minecraft/src/Material"));
			rock = new Material(mcMaterialClass.getField(WrapLoaderAPI.mapping.getField("net/minecraft/src/Material", "rock")).get(null));
			System.out.println("material " + mcMaterialClass + " " + rock);
		}
		catch (Exception e)
		{
			ReWrapLoader.err("Material initialization failed! " + e);
			e.printStackTrace();
		}
	}
	
	public Object getMC()
	{
		return mcMaterial;
	}
}
