
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import javassist.*;

public class TransformerMinecraft implements ClassFileTransformer {
	String targetClassName;
	ClassLoader targetClassLoader;
    public TransformerMinecraft(String name, ClassLoader classLoader) {
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
        		
        		// directory patch
        		CtMethod m = cc.getDeclaredMethod("b", new CtClass[] {});
        		
        		m.insertBefore("if(af == null) { af = a(\"wraploadermc\"); } return af;");
                
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (NotFoundException | CannotCompileException | IOException e) {
               e.printStackTrace();
            }
        }
        return byteCode;
	}
}

