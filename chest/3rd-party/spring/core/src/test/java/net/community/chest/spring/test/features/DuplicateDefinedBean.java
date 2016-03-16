/*
 *
 */
package net.community.chest.spring.test.features;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.stereotype.Component;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 3, 2012 9:30:15 AM
 */
@Component
public class DuplicateDefinedBean implements BeanNameAware {
    private static final AtomicInteger    _instancesCount=new AtomicInteger(0);
    private final int    _instanceIndex;
    public DuplicateDefinedBean ()
    {
        _instanceIndex = _instancesCount.incrementAndGet();
        System.out.append(getClass().getSimpleName())
                  .append("<init>#")
              .println(_instanceIndex)
              ;
    }

    @Override
    public void setBeanName (String name)
    {
        System.out.append(getClass().getSimpleName())
                    .append("#setBeanName(")
                    .append(name)
                    .append(")[")
                    .append(String.valueOf(_instanceIndex))
                .println("]")
                ;
    }

}
