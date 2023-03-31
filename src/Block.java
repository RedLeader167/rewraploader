
public class Block {
	private int textureID;
	private int blockID;
	protected float blockHardness = 1F;
	protected float blockResistance = 1F;
	protected String blockName = "nullblock";
	protected Material blockMaterial = Material.rock;
	
	public Block(int blockID, int textureID)
	{
		this.textureID = textureID;
		this.blockID = blockID;
	}
	
	public int getBlockID()
	{
		return blockID;
	}
	public int getTextureID()
	{
		return textureID;
	}
	public Material getMaterial()
	{
		return blockMaterial;
	}
	
	public Block setHardness(float hardness)
	{
		blockHardness = hardness;
		return this;
	}
	public Block setMaterial(Material material)
	{
		blockMaterial = material;
		return this;
	}
	public Block setResistance(float resistance)
	{
		blockResistance = resistance;
		return this;
	}
	public Block setBlockName(String name)
	{
		blockName = name;
		return this;
	}

	public float getResistance()
	{
		return blockResistance;
	}
	public float getHardness()
	{
		return blockHardness;
	}
	public String getBlockName()
	{
		return blockName;
	}
}
