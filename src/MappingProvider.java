import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class MappingProvider {
	private HashMap<String, String> classmap = new HashMap<String, String>();
	private HashMap<String, HashMap<String, String>> fieldmap = new HashMap<String, HashMap<String,String>>();
	private HashMap<String, HashMap<String, HashMap<String, String>>> methodmap = new HashMap<String, HashMap<String,HashMap<String,String>>>();
	private String _mCurrentClass;
	
	private MappingProvider(BufferedReader mappings) throws FileNotFoundException, IOException {
		mappings.lines().forEachOrdered((String z)->{
			String[] data = z.trim().split("\\t");
			if(data[0].equals("tiny"))
			{
				// dont care + didnt ask + L + ratio + get a life + no bitches
			}
			else if(data[0].equals("c"))
			{
				this._mCurrentClass = data[2];
				this.classmap.put(data[2], data[1]);
			}
			else if(data[0].equals("f"))
			{
				if(this.fieldmap.get(this._mCurrentClass) == null)
				{
					this.fieldmap.put(this._mCurrentClass, new HashMap<String, String>());
				}
				this.fieldmap.get(this._mCurrentClass).put(data[3], data[2]);
			}
			else if(data[0].equals("m"))
			{
				if(this.methodmap.get(this._mCurrentClass) == null)
				{
					this.methodmap.put(this._mCurrentClass, new HashMap<String, HashMap<String, String>>());
				}
				if(this.methodmap.get(this._mCurrentClass).get(data[3]) == null)
				{
					this.methodmap.get(this._mCurrentClass).put(data[3], new HashMap<String, String>());
				}
				this.methodmap.get(this._mCurrentClass).get(data[3]).put(data[1], data[2]);
			}
		});
	}
	
	public static MappingProvider fromFile(File mappings) throws FileNotFoundException, IOException {
		return new MappingProvider(new BufferedReader(new FileReader(mappings)));
	}
	public static MappingProvider fromResource(String mappings) throws FileNotFoundException, IOException {
		return new MappingProvider(new BufferedReader(new InputStreamReader(MappingProvider.class.getResourceAsStream(mappings))));
	}
	
	public String getClass(String clazz) {
		return this.classmap.get(clazz);
	}
	public String getMethod(String clazz, String method, String types) {
		return this.methodmap.get(clazz).get(method).get(types);
	}
	public String getField(String clazz, String field) {
		return this.fieldmap.get(clazz).get(field);
	}
}
