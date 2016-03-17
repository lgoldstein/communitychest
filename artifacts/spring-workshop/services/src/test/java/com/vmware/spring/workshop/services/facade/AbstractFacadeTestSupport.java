package com.vmware.spring.workshop.services.facade;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;

import com.vmware.spring.workshop.services.AbstractServicesTestSupport;

/**
 * @author lgoldstein
 */
public abstract class AbstractFacadeTestSupport extends AbstractServicesTestSupport {
    protected AbstractFacadeTestSupport() {
        super();
    }

    protected <FCD,DTO,FND extends FacadeValueFinder<FCD,DTO>>
            void runFacadeValueFinderTest (final FCD                        facade,
                                           final Collection<? extends DTO>    dtosList,
                                           final FND                        finder) {
        Assert.assertFalse("No DTO(s) values", CollectionUtils.isEmpty(dtosList));

        for (final DTO dtoValue : dtosList) {
            final DTO    foundValue=finder.findDtoValue(facade, dtoValue);
            Assert.assertNotNull("No match found for " + dtoValue, foundValue);
            Assert.assertEquals("Mismatched retrieved instances", dtoValue, foundValue);
        }
    }
}
