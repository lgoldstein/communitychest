package com.vmware.spring.workshop.facade.web;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;

import com.vmware.spring.workshop.facade.beans.ScriptSubmissionBean;

/**
 * @author lgoldstein
 */
@Controller("groovyConsoleController")
@RequestMapping("/groovy")
@SessionAttributes(types={ ScriptSubmissionBean.class })
public class GroovyConsoleController
        extends AbstractWebController
        implements ApplicationContextAware {
    private ApplicationContext    _context;

    public GroovyConsoleController() {
        super();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        _context = applicationContext;
    }

    private static final String    SUBMISSION_ATTR="scriptSubmissionBean";

    @RequestMapping(method=RequestMethod.GET)
    public String showConsole (final Model model) {
        model.addAttribute(SUBMISSION_ATTR, new ScriptSubmissionBean());
        return getTopLevelViewPath("console");
    }

    @RequestMapping(method=RequestMethod.POST, value="/executeScript")
    public String executeScript (            final Model                 model,
           @ModelAttribute(SUBMISSION_ATTR)    final ScriptSubmissionBean    submissionBean,
                                               final SessionStatus         status) {
        // run script
        final String    script=submissionBean.getScript();
        try {
            if (_logger.isTraceEnabled())
                _logger.trace("Executing Groovy script:\n" + script);

            final GroovyShell    shell=prepareShell();
            final Object        result=shell.evaluate(script);
            if (_logger.isTraceEnabled())
                _logger.trace("Result: " + result);
            submissionBean.setResult(result);
        } catch (Exception e) {
            _logger.warn(e.getClass().getSimpleName() + " running groovy script: " + e.getMessage(), e);
            submissionBean.setResult("Exception:  (" + e.getClass().getName() + ") " + e.getMessage());
        }

        status.setComplete();
        return getTopLevelViewPath("console");
    }

    @RequestMapping(method=RequestMethod.POST, value="/executeFile")
    public String executeFile (          final Model model,
                @RequestParam("file") final MultipartFile file,
                                      final SessionStatus status) {
        final String    fileName=file.getOriginalFilename();
        _logger.info("runScript(" + fileName + ") executing script file");

        final ScriptSubmissionBean    submissionBean=new ScriptSubmissionBean();
        model.addAttribute(SUBMISSION_ATTR, submissionBean);

        try {
            final GroovyShell    shell=prepareShell();
            final Reader        inpStream=new BufferedReader(new InputStreamReader(file.getInputStream()), 1024);
            try {
                final Object    result=shell.evaluate(inpStream, fileName);
                if (_logger.isTraceEnabled())
                    _logger.trace("Result: " + result);
                submissionBean.setResult(result);
            } finally {
                inpStream.close();
            }
        } catch (Exception e) {
            _logger.warn(e.getClass().getSimpleName() + " running groovy script: " + e.getMessage(), e);
            submissionBean.setResult("Exception:  (" + e.getClass().getName() + ") " + e.getMessage());
        }

        status.setComplete();
        return getTopLevelViewPath("console");
    }

    private GroovyShell prepareShell () {
        final Map<String,Object>    beans=getBeans(_context);
        final Binding                binding=new Binding(beans);
        return new GroovyShell(binding);
    }

    private static Map<String, Object> getBeans (final ApplicationContext context) {
        final Map<String,Object>    beans=context.getBeansOfType(Object.class);
        final ApplicationContext    parent=context.getParent();
        if (parent != null)
            beans.putAll(getBeans(parent));
        return beans;
    }
}
