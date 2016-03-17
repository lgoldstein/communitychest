package com.vmware.spring.workshop.facade.web;

import java.io.IOException;
import java.util.Collection;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.vmware.spring.workshop.dto.user.UserDTO;
import com.vmware.spring.workshop.dto.user.UserRoleTypeDTO;
import com.vmware.spring.workshop.facade.support.UserDTOImportExport;
import com.vmware.spring.workshop.services.facade.UsersFacade;

/**
 * @author lgoldstein
 */
@Controller("usersController")
@RequestMapping("/users")
public class UsersController extends AbstractWebController {
    private final UsersFacade    _usrFacade;
    private final UserDTOImportExport    _importer;
    @Inject
    public UsersController(final UsersFacade             usrFacade,
                           final UserDTOImportExport    importer) {
        _usrFacade = usrFacade;
        _importer = importer;
    }

    @RequestMapping(method=RequestMethod.GET)
    public String listUsers (final Model model) {
        model.addAttribute("usersList", _usrFacade.findAll());
        return getTopLevelViewPath("list");
    }

    @RequestMapping(method=RequestMethod.GET, value="/export")
    public ModelAndView exportUsers () {
        return exportDTOList(_importer, _usrFacade.findAll());
    }

    @RequestMapping(method=RequestMethod.POST, value="/import")
    public String importUsers (@RequestParam("file") final MultipartFile file, final Model model)
            throws IOException {
        importDTOList(file, _importer, _usrFacade);
        return listUsers(model);    // refresh view to reflect changes after import
    }

    @RequestMapping(method=RequestMethod.GET, value="/create")
    public String prepareUserCreateForm (final Model model) {
        return prepareUserForm(model, new UserDTO(), "create");
    }

    @RequestMapping(method=RequestMethod.GET, value="/edit/" + BY_ID_TEMPLATE)
    public String prepareUserEditForm (final Model model, @PathVariable(ID_PARAM_NAME) final Long id) {
        return prepareUserForm(model, _usrFacade.findById(id), "edit");
    }

    @RequestMapping(method=RequestMethod.POST, value="/edit/" + BY_ID_TEMPLATE)
    public String updateUser (@Valid final UserDTO         userData,
                                @PathVariable(ID_PARAM_NAME) final Long id,
                                final BindingResult         result,
                                final HttpServletRequest    request,
                                final Model                 model) {
        handleBindingResultErrors(userData, result);
        final UserDTO    curData=_usrFacade.findById(id);
        Assert.state(curData != null, "Referenced DTO no longer exists");

        final Long    idData=userData.getId();
        if (idData == null)
            userData.setId(id);
        else
            Assert.isTrue(id.equals(idData), "Mismatched user ID(s)");

        // password field is left null by the form if not edited by the user
        final String    curPassword=curData.getPassword(),
                        dataPassword=userData.getPassword();
        if (StringUtils.isBlank(dataPassword))
            userData.setPassword(curPassword);

        _usrFacade.update(userData);
        return listUsers(model);
    }

    @RequestMapping(method=RequestMethod.POST, value="/create")
    public String createUser (@Valid final UserDTO         userData,
                              final BindingResult         result,
                              final HttpServletRequest    request,
                              final Model                 model) {
        if (result.hasErrors()) {
            handleBindingResultErrors(userData, result);
            throw new IllegalStateException("Bad DTO: " + userData);
        }

        _usrFacade.create(userData);
        return listUsers(model);
    }

    @RequestMapping(method=RequestMethod.GET, value="/admin")
    public String adminView () {
        return getTopLevelViewPath("admin");
    }

    @RequestMapping(method=RequestMethod.GET, value="/guest")
    public String guestView () {
        return getTopLevelViewPath("guest");
    }

    private void handleBindingResultErrors (final UserDTO userData, final BindingResult result) {
        if (!result.hasErrors())
            return;

        final Collection<? extends ObjectError>        errsList=result.getAllErrors();
        for (final ObjectError err : errsList) {
            _logger.warn(err.getObjectName() + "[" + err.getCode() + "]: " + err.getDefaultMessage());
        }

        _logger.error("Bad DTO: " + userData);
    }

    private String prepareUserForm (final Model model, final UserDTO userData, final String actionName) {
        Assert.notNull(userData, "No user DTO");
        model.addAttribute("actionName", actionName);
        model.addAttribute("userData", userData);
        model.addAttribute("rolesList", UserRoleTypeDTO.VALUES);
        return getTopLevelViewPath("manageUser");
    }
}
