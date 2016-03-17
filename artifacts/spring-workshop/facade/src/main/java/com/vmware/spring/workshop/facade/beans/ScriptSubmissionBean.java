package com.vmware.spring.workshop.facade.beans;

import java.io.Serializable;

/**
 * @author lgoldstein
 */
public class ScriptSubmissionBean implements Serializable {
    private static final long serialVersionUID = 7815948107000722097L;

    private String    _script="";
    private Object    _result="";

    public ScriptSubmissionBean () {
        super();
    }

    public String getScript () {
        return _script;
    }

    public void setScript (String script) {
        _script = script;
    }

    public Object getResult () {
        return _result;
    }

    public void setResult (Object result) {
        _result = result;
    }


}
