package com.vmware.spring.workshop.facade.support.views;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.springframework.util.Assert;
import org.springframework.web.servlet.view.AbstractView;

/**
 * @author lgoldstein
 */
@View("genericXMLSchemaView")
public class GenericXMLSchemaView extends AbstractView {
    public GenericXMLSchemaView() {
        super();
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        Assert.notEmpty(model, "No model values");
        Assert.isTrue(model.size() == 1, "Ambiguous model data");

        final Object    obj=model.values().iterator().next();
        Assert.notNull(obj, "No object to generate");
        response.setContentType("application/xml");

        final Class<?>        clazz=(obj instanceof Class<?>) ? (Class<?>) obj : obj.getClass();
        final JAXBContext    ctx=JAXBContext.newInstance(clazz);
        final Writer        w=response.getWriter();
        try {
            final SchemaValueGenerator    gen=new SchemaValueGenerator(w);
            ctx.generateSchema(gen);
        } finally {
            w.close();
        }
    }

    private static class SchemaValueGenerator extends SchemaOutputResolver {
        private final Writer    _output;
        protected SchemaValueGenerator (final Writer output)
        {
            _output = output;
        }

        @Override
        public Result createOutput (String namespaceUri, String suggestedFileName) throws IOException
        {
            final StreamResult    res=new StreamResult(_output);
            res.setSystemId(namespaceUri);
            return res;
        }
    }

}
