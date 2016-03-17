package org.springframework.instrument.classloading.tomcat;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Logger;

/**
 * @author lgoldstein
 */
public class IdentityClassFileTransformer implements ClassFileTransformer {
    public static final IdentityClassFileTransformer    INSTANCE=new IdentityClassFileTransformer();
    protected final Logger    logger=Logger.getLogger(getClass().getName());
    public IdentityClassFileTransformer() {
        super();
    }

    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {
        logger.info("transform(" + className + ")");
        return classfileBuffer;
    }

}
