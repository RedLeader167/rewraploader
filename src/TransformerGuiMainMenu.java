
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import javassist.*;

public class TransformerGuiMainMenu implements ClassFileTransformer {
	String targetClassName;
	ClassLoader targetClassLoader;
    public TransformerGuiMainMenu(String name, ClassLoader classLoader) {
		this.targetClassLoader = classLoader;
		this.targetClassName = name;
	}

	@Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        byte[] byteCode = classfileBuffer;
        String finalTargetClassName = this.targetClassName.replaceAll("\\.", "/"); 
        if (!className.equals(finalTargetClassName)) {
        	return byteCode;
        }

        if (className.equals(finalTargetClassName) && loader.equals(targetClassLoader)) {
        	try {
        		ClassPool cp = ClassPool.getDefault();
        		CtClass cc = cp.get(targetClassName);
        		CtMethod m = cc.getDeclaredMethod("a", new CtClass[] {cp.getCtClass(int.class.getName()), cp.getCtClass(int.class.getName()), cp.getCtClass(float.class.getName())});

        		m.insertAt(119, "this.b(this.g, \"WrapLoader \" + ReWrapLoader.RWLVersion, 2, 12, 5263440);"
        				+ "this.b(this.g, Integer.toString(ReWrapLoader.modlist.size()) + \" mods loaded\", 2, 22, 5263440);");
        		m.insertBefore("WrapLoaderAPI.dispatchEvent(new EventMainMenuRender(this, this.g, true));");
        		m.insertAfter("WrapLoaderAPI.dispatchEvent(new EventMainMenuRender(this, this.g, false));");
                
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (NotFoundException | CannotCompileException | IOException e) {
               e.printStackTrace();
            }
        }
        return byteCode;
	}
}

