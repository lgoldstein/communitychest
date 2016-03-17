package com.vmware.spring.workshop.facade.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.vmware.spring.workshop.dto.IdentifiedDTO;
import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.dto.user.UserRoleTypeDTO;
import com.vmware.spring.workshop.facade.support.AbstractController;
import com.vmware.spring.workshop.facade.support.CSVImportExport;
import com.vmware.spring.workshop.services.facade.CommonFacadeActions;

/**
 * @author lgoldstein
 */
public abstract class AbstractWebController extends AbstractController {
    @Inject @Named("GenericCSVExportView") private View _exportView;
    @Inject private Environment    _environment;

    protected AbstractWebController() {
        super();
    }

    @ModelAttribute("userRole")
    public String userModelRole () {
        final UserDTO            dto=userModelValue();
        final UserRoleTypeDTO    role=(dto == null) ? null : dto.getRole();
        return (role == null) ? null : role.name();
    }

    @ModelAttribute("user")
    public UserDTO userModelValue () {
        final SecurityContext    context=SecurityContextHolder.getContext();
        final Authentication    authentication=context.getAuthentication();
        return extractSessionUser(authentication, true);
    }

    @ModelAttribute("activeProfile")
    public String activeProfile () {
        final String[]    profiles=_environment.getActiveProfiles();
        Assert.state(profiles != null, "No active profiles");
        Assert.state(profiles.length == 1, "Multiple active profiles");
        return profiles[0];
    }

    protected <DTO> ModelAndView exportDTOList (final CSVImportExport<DTO> importer,
                                                final Collection<? extends DTO> dtoList)
    {
        Assert.notNull(importer, "No importer specified");
        final Map<String,Object>    model=new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
        model.put("importer", importer);
        model.put("dtoList", (dtoList == null) ? Collections.emptyList() : dtoList);
        return new ModelAndView(_exportView, model);
    }

    protected <DTO extends IdentifiedDTO> void importDTOList (
                final MultipartFile                file,
                final CSVImportExport<DTO>        importer,
                final CommonFacadeActions<DTO>    facade)
                        throws IOException
    {
        Assert.notNull(importer, "No importer specified");
        Assert.notNull(facade, "No facade provided");

        final String    orgName=file.getOriginalFilename();
        final Class<?>    dtoClass=importer.getDTOClass(), fcdClass=facade.getDTOClass();
        Assert.state(dtoClass == fcdClass, "Mismatched DTO classes for facade and importer");
        _logger.info("importDTOList(" + dtoClass.getSimpleName() + ") start from " + orgName);

        final BufferedReader    reader=new BufferedReader(new InputStreamReader(file.getInputStream()), 1204);
        try {
            final String    titleLine=StringUtils.strip(reader.readLine());
            if (StringUtils.isBlank(titleLine))
                throw new StreamCorruptedException("No title line");

            final String[]    props=StringUtils.split(titleLine, ',');
            if (ArrayUtils.isEmpty(props))
                throw new StreamCorruptedException("No properties in title line: " + titleLine);

            final List<String>    propsOrder=new ArrayList<String>(props.length);
            for (final String p : props) {
                final String    pName=StringUtils.strip(StringUtils.trimToEmpty(p), " ");
                if (StringUtils.isBlank(pName))
                    throw new StreamCorruptedException("Empty property name in title line: " + titleLine);
                propsOrder.add(pName);
            }

            int    numImported=0;
            for (DTO    dtoValue=importer.toDTO(reader, propsOrder); dtoValue != null; dtoValue=importer.toDTO(reader, propsOrder)) {
                try {
                    final Long    id=dtoValue.getId();
                    final DTO    prev=facade.findById(id);
                    if (prev == null) {
                        facade.create(dtoValue);
                        _logger.info("import(" + orgName + ")[" + dtoValue + "] CREATED");
                    } else if (prev.equals(dtoValue)) {
                        _logger.info("import(" + orgName + ")[" + dtoValue + "] SKIPPED");
                    } else {
                        facade.update(dtoValue);
                        _logger.info("import(" + orgName + ")[" + dtoValue + "] UPDATED - old: " + prev);
                    }
                    numImported++;
                } catch(Exception e) {
                    _logger.error("import(" + orgName + ")[" + dtoValue + "]"
                                 + " " + e.getClass().getSimpleName()
                                 + ": " + e.getMessage(), e);
                }
            }

            _logger.info("importDTOList(" + dtoClass.getSimpleName() + ") end - imported " + numImported + " DTO(s)");

        } finally {
            reader.close();
        }
    }

    protected String getTopLevelViewPath (final String subPath) {
        Assert.hasText(subPath, "No sub path specified");

        final String    mapping=getTopLevelMapping();
        if (StringUtils.isBlank(mapping) || "/".equals(mapping))
            return subPath;

        if (mapping.charAt(0) == '/')
            return mapping.substring(1) + "/" + subPath;
        else
            return mapping + "/" + subPath;
    }

    protected String getTopLevelMapping () {
        final Class<?>            clazz=getClass();
        final RequestMapping    mapping=clazz.getAnnotation(RequestMapping.class);
        final String[]            values=(mapping == null) ? null : mapping.value();
        return resolveTopLevelMapping(values);
    }

    protected String resolveTopLevelMapping (final String ... values) {
        if (ArrayUtils.isEmpty(values))
            return null;

        if (values.length == 1)
            return values[0];

        throw new IllegalStateException("Multiple top level mappings: " + values);
    }

    protected UserDTO extractSessionUser (final Principal principal) {
        return extractSessionUser(principal, true);
    }

    protected UserDTO extractSessionUser (final Principal principal, final boolean ignoreIfNotFound) {
        if (principal == null) {
            Assert.state(ignoreIfNotFound, "No current session principal");
            return null;
        }

        if (principal instanceof UserDTO)
            return (UserDTO) principal;

        if (principal instanceof Authentication)
        {
            final Object    tokenPrincipal=((Authentication) principal).getPrincipal();
            if (ignoreIfNotFound)
            {
                if (!(tokenPrincipal instanceof UserDTO))
                    return null;
            }
            else
            {
                Assert.isInstanceOf(UserDTO.class, tokenPrincipal, "Token principal not a user DTO");
            }

            return (UserDTO) tokenPrincipal;
        }

        throw new IllegalStateException("No UserDTO principal");
    }

    @Override
    public String toString ()
    {
        final Class<?>        clazz=getClass();
        final Controller    ctrl=clazz.getAnnotation(Controller.class);
        final String        name=(ctrl == null) ? null : ctrl.value();
        return StringUtils.isBlank(name) ? clazz.getSimpleName() : name;
    }
}
