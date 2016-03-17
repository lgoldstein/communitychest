package com.vmware.spring.workshop.services.convert.impl;

/**
 * @author lgoldstein
 */
public interface ValueConverter<SRC, DST> {
    DST convertValue (SRC srcValue);
}
