
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import javax.imageio.ImageIO;

import javassist.*;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class TransformerTexturePackBase implements ClassFileTransformer {
	String targetClassName;
	ClassLoader targetClassLoader;
    public TransformerTexturePackBase(String name, ClassLoader classLoader) {
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
        		
        		String getResourceAsStream = WrapLoaderAPI.mapping.getMethod("net/minecraft/src/TexturePackBase", "getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;");
        		String texturePackBase = WrapLoaderAPI.mapping.getClass("net/minecraft/src/TexturePackBase");
        		// texture overrides
        		CtMethod m = cc.getDeclaredMethod(getResourceAsStream, new CtClass[] {cp.getCtClass(String.class.getName())});
        		
        		String s1 = "return WrapLoaderAPI.__JTerrainPngOverride($1);";
        		m.insertBefore(s1);
                
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception e) {
               e.printStackTrace();
            }
        }
        return byteCode;
	}
}
