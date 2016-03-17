package com.vmware.spring.workshop.facade.web;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Known browser types as recognized by their user-agent header value(s)
 * @author lgoldstein
 */
public enum UserAgents {
    FIREFOX("Firefox", "http://www.mozilla.org/en-US/firefox/fx"),
    MSIE("MSIE", "http://windows.microsoft.com/en-us/internet-explorer/products/ie/home"),
    CHROME("Chrome", "http://support.google.com/chrome/bin/answer.py?hl=en&answer=95346"),
    OPERA("Opera", "http://www.opera.com/download/"),
    SAFARI("Safari","http://www.apple.com/safari/download/"),
    UNKNOWN(null,null);

    private final String    _searchPattern;
    public final String getSearchPattern () {
        return _searchPattern;
    }

    private final String    _downloadLocation;
    public final String getDownloadLocation () {
        return _downloadLocation;
    }

    UserAgents (final String searchPattern, final String dldLocation) {
        _searchPattern = searchPattern;
        _downloadLocation = dldLocation;
    }

    public static final Set<UserAgents>    VALUES=
            Collections.unmodifiableSet(EnumSet.allOf(UserAgents.class));

    public static final UserAgents findUserAgents (final String hdrValue) {
        if (StringUtils.isBlank(hdrValue))
            return UNKNOWN;

        for (final UserAgents agent : VALUES) {
            final String    pattern=agent.getSearchPattern();
            if (StringUtils.isBlank(pattern))
                continue;
            if (StringUtils.containsIgnoreCase(hdrValue, pattern))
                return agent;
        }

        return UNKNOWN;
    }
}
