
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import javassist.*;

public class TransformerBlock implements ClassFileTransformer {
	String targetClassName;
	ClassLoader targetClassLoader;
    public TransformerBlock(String name, ClassLoader classLoader) {
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
        		
        		for(CtConstructor con : cc.getConstructors()) {
        			if(con.getParameterTypes().length == 3)
        			{
        				con.insertBefore("WrapLoaderAPI.textureIDsToBlockIDs[$2] = $1;");
        			}
        		}
                
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (NotFoundException | CannotCompileException | IOException e) {
               e.printStackTrace();
            }
        }
        return byteCode;
	}
}
