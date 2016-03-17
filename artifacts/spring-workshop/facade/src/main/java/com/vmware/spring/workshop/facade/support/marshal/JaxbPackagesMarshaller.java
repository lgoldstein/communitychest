package com.vmware.spring.workshop.facade.support.marshal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * @author lgoldstein
 */
public class JaxbPackagesMarshaller extends AbstractJAXBContextMarshaller {
    private final Set<String>    _packages;
    private final JAXBContext    _jaxbContext;

    public JaxbPackagesMarshaller(final String ... packages) throws JAXBException {
        Assert.state(!ArrayUtils.isEmpty(packages), "No packages specified");
        Assert.noNullElements(packages, "Null packages specified");

        final String    contextPath=StringUtils.join(packages, ':');
        Assert.hasText(contextPath, "Empty context path");
        Assert.state(!contextPath.contains("::"), "Empty context packages");

        _jaxbContext = JAXBContext.newInstance(contextPath);
        _packages = Collections.unmodifiableSet(new TreeSet<String>(Arrays.asList(packages)));
    }

    public final Set<String> getContextPackages () {
        return _packages;
    }

    @Override
    public boolean supports(final Class<?> clazz) {
        final Package        pkg=(clazz == null) ? null : clazz.getPackage();
        final String        pkgName=(pkg == null) ? null : pkg.getName();
        final Set<String>    ctxPackages=getContextPackages();
        if (StringUtils.isBlank(pkgName)
         || CollectionUtils.isEmpty(ctxPackages)
         || (!ctxPackages.contains(pkgName)))
            return false;    // debug breakpoint
        else
            return true;
    }

    @Override
    public JAXBContext getJAXBContext() throws JAXBException {
        return _jaxbContext;
    }

    @Override
    public String toString() {
        return String.valueOf(_packages);
    }
}
