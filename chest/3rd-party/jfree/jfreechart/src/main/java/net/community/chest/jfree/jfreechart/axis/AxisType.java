/*
 *
 */
package net.community.chest.jfree.jfreechart.axis;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.jfree.jfreechart.axis.category.CategoryAxis3DReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.category.CategoryAxisReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.category.ExtendedCategoryAxisReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.category.SubCategoryAxisReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.value.CyclicNumberAxisReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.value.DateAxisReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.value.LogAxisReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.value.ModuloAxisReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.value.NumberAxis3DReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.value.NumberAxisReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.value.PeriodAxisReflectiveProxy;
import net.community.chest.jfree.jfreechart.axis.value.SymbolAxisReflectiveProxy;
import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.CyclicNumberAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ExtendedCategoryAxis;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.ModuloAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 25, 2009 9:26:25 AM
 */
public enum AxisType {
    CYCLIC(CyclicNumberAxis.class, CyclicNumberAxisReflectiveProxy.CYCLIC),
    MODULO(ModuloAxis.class, ModuloAxisReflectiveProxy.MODULO),
    SYMBOL(SymbolAxis.class, SymbolAxisReflectiveProxy.SYMBOL),
    LOG(LogAxis.class, LogAxisReflectiveProxy.LOG),
    NUMBER3D(NumberAxis3D.class, NumberAxis3DReflectiveProxy.NUMAXIS3D),
    NUMBER(NumberAxis.class, NumberAxisReflectiveProxy.NUMBER),
    PERIOD(PeriodAxis.class, PeriodAxisReflectiveProxy.PERIOD),
    DATE(DateAxis.class, DateAxisReflectiveProxy.DATE),
    SUBCATEGORY(SubCategoryAxis.class, SubCategoryAxisReflectiveProxy.SUBCAT),
    EXTENDEDCATEGORY(ExtendedCategoryAxis.class, ExtendedCategoryAxisReflectiveProxy.EXTCAT),
    CATEGORY3D(CategoryAxis3D.class, CategoryAxis3DReflectiveProxy.CAT3D),
    CATEGORY(CategoryAxis.class, CategoryAxisReflectiveProxy.CATEGORY);

    private final Class<? extends Axis>    _axisClass;
    public final Class<? extends Axis> getAxisClass ()
    {
        return _axisClass;
    }

    private final AxisReflectiveProxy<? extends Axis>    _p;
    public final AxisReflectiveProxy<? extends Axis> getAxisConverter ()
    {
        return _p;
    }

    public final Axis fromXml (final Element elem) throws Exception
    {
        final AxisReflectiveProxy<? extends Axis>    p=
            (null == elem) ? null : getAxisConverter();
        return (null == p) ? null : p.fromXml(elem);
    }

    <A extends Axis> AxisType (Class<A> ac, AxisReflectiveProxy<A> p)
    {
        _axisClass = ac;
        _p = p;
    }

    public static final List<AxisType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final AxisType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final AxisReflectiveProxy<? extends Axis> getAxisConverter (final String axisType)
    {
        final AxisType    t=fromString(axisType);
        if (null == t)
            return null;
        else
            return t.getAxisConverter();
    }
}
