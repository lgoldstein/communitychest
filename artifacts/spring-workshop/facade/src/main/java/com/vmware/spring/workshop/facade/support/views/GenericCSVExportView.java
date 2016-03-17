package com.vmware.spring.workshop.facade.support.views;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.servlet.view.AbstractView;

import com.vmware.spring.workshop.facade.beans.AbstractDTOBean;
import com.vmware.spring.workshop.facade.support.CSVImportExport;

/**
 * @author lgoldstein
 */
@View("GenericCSVExportView")
public class GenericCSVExportView extends AbstractView {
    public GenericCSVExportView () {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Assert.state(model != null, "No model");

        @SuppressWarnings("rawtypes")
        final CSVImportExport    importer=(CSVImportExport) model.get("importer");
        Assert.state(importer != null, "No importer provided");

        final Class<?>    dtoClass=importer.getDTOClass();
        final String    fileName=dtoClass.getSimpleName() + ".csv";
        response.setCharacterEncoding("utf-8");
        // according to RFC4180 this should be text/csv but the browser does not treat this correctly
        response.setContentType("application/comma-separated-values; name=" + fileName);
        response.setHeader("Content-Disposition","attachment; filename=" + fileName);

        final Collection<?>    dtoList=(Collection<?>) model.get("dtoList");
        final Writer        w=response.getWriter();
        try {
            importer.appendTitleLine(w);

            for (final Object value : dtoList) {
                final Object    dtoValue=(value instanceof AbstractDTOBean<?>)
                            ? ((AbstractDTOBean<?>) value).getDTOValue()
                            : value
                            ;
                w.append(CSVImportExport.LINE_SEP);    // separate from previous line
                importer.appendDTO(w, dtoValue);
            }
        } finally {
            w.close();
        }
    }
}
