package com.vmware.spring.workshop.facade.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.vmware.spring.workshop.dto.banking.BankDTO;
import com.vmware.spring.workshop.dto.banking.BranchDTO;
import com.vmware.spring.workshop.facade.beans.BranchBean;
import com.vmware.spring.workshop.facade.support.BranchDTOImportExport;
import com.vmware.spring.workshop.services.facade.BanksFacade;
import com.vmware.spring.workshop.services.facade.BranchesFacade;

/**
 * @author lgoldstein
 */
@Controller("branchesController")
@RequestMapping("/branches")
public class BranchesController extends AbstractWebController {
    private final BanksFacade        _banksFacade;
    private final BranchesFacade    _branchesFacade;
    private final BranchDTOImportExport    _importer;

    public static final String    BANK_CODE_PARAM_NAME="bankCode",
                                BRANCH_CODE_PARAM_NAME="branchCode";

    @Inject
    public BranchesController(final BanksFacade        banksFacade,
                              final BranchesFacade    branchesFacade,
                              final BranchDTOImportExport    importer) {
        _banksFacade = banksFacade;
        _branchesFacade = branchesFacade;
        _importer = importer;
    }

    @RequestMapping(method=RequestMethod.GET)
    public String listBranches (final Model model) {
        return listBranches(model, null, _branchesFacade.findAll());
    }

    @RequestMapping(method=RequestMethod.GET, value="/export")
    public ModelAndView exportBranches () {
        return exportDTOList(_importer, _branchesFacade.findAll());
    }

    @RequestMapping(method=RequestMethod.GET, value="/export/{" + BANK_CODE_PARAM_NAME + "}")
    public ModelAndView exportBankBranches (@PathVariable(BANK_CODE_PARAM_NAME) final int bankCode) {
        final BankDTO    bank=_banksFacade.findBankByBankCode(bankCode);
        return exportDTOList(_importer, (bank == null) ? Collections.<BranchDTO>emptyList() : _branchesFacade.findAllBranches(bank));
    }

    @RequestMapping(method=RequestMethod.GET, value="/{" + BANK_CODE_PARAM_NAME + "}")
    public String showBankBranches (@PathVariable(BANK_CODE_PARAM_NAME) final int bankCode, final Model model) {
        final BankDTO                            bank=_banksFacade.findBankByBankCode(bankCode);
        final Collection<? extends BranchDTO>    branches=
                (bank == null) ? Collections.<BranchDTO>emptyList() : _branchesFacade.findAllBranches(bank);
        return listBranches(model, bank, branches);
    }

    String listBranches (final Model model, final BankDTO bank, final Collection<? extends BranchDTO> branches) {
        if (bank != null)
            model.addAttribute("bank", bank);
        model.addAttribute("branchesList", createBeans(bank, branches));
        return getTopLevelViewPath("list");
    }

    List<BranchBean> createBeans (final BankDTO bank, final Collection<? extends BranchDTO> branches) {
        if (CollectionUtils.isEmpty(branches))
            return Collections.emptyList();

        List<BranchBean>    result=new ArrayList<BranchBean>(branches.size());
        for (final BranchDTO branch : branches) {
            final BankDTO    bb=(bank == null) ? _banksFacade.findById(branch.getBankId()) : bank;
            if (bb == null)
            {
                _logger.warn("createBeans(" + branch + ") no bank found");
                continue;
            }

            result.add(new BranchBean(bb, branch));
        }

        return result;
    }
}
