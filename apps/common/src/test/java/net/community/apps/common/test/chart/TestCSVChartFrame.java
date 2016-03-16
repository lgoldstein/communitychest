/*
 *
 */
package net.community.apps.common.test.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import net.community.apps.common.test.TestMainFrame;
import net.community.chest.Triplet;
import net.community.chest.awt.AWTUtils;
import net.community.chest.jfree.jfreechart.data.DatasetUtils;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.math.functions.AggregateFunctions;
import net.community.chest.resources.SystemPropertiesResolver;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.component.menu.MenuUtil;
import net.community.chest.ui.components.datetime.AbstractOptionalSpinnerDateTimeControl;
import net.community.chest.ui.components.datetime.AbstractSpinnerDateTimeControl;
import net.community.chest.ui.components.datetime.OptionalDateControl;
import net.community.chest.ui.components.datetime.OptionalTimeControl;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelPanel;
import net.community.chest.util.datetime.DateUtil;
import net.community.chest.util.map.MapEntryImpl;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Values;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 11, 2009 11:48:10 AM
 */
public class TestCSVChartFrame extends TestMainFrame {
    /**
     *
     */
    private static final long serialVersionUID = 486614267288417318L;
    public static final int    DEFAULT_DISTTRIBUTION_FACTOR=5;
    private static final Map<String,? extends Number> createDistributionsMap (final TimeSeries ts)
    {
        final int    numItems=(null == ts) ? 0 : ts.getItemCount();
        if (numItems <= 0)
            return null;

        Map<String,MutableInt>    ret=null;
        for (int    i=0; i < numItems; i++)
        {
            final Number    n=ts.getValue(i);
            final int        v=(null == n) ? 0 : (n.intValue() / DEFAULT_DISTTRIBUTION_FACTOR);
            if (v <= 0)
                continue;

            if (null == ret)
                ret = new TreeMap<String,MutableInt>(String.CASE_INSENSITIVE_ORDER);

            final String    k=String.valueOf(v);
            MutableInt        c=ret.get(k);
            if (null == c)
            {
                c = new MutableInt(1);
                ret.put(k, c);
            }
            else
                c.increment();
        }

        return ret;
    }

    private static final DefaultCategoryDataset createCategoryDataset (final String ttl, final Map<String,? extends Number> distsMap)
    {
        final Collection<? extends Map.Entry<String,? extends Number>>    pl=
            ((null == distsMap) || (distsMap.size() <= 0)) ? null : distsMap.entrySet();
        if ((null == pl) || (pl.size() <= 0))
            return null;

        DefaultCategoryDataset    ds=null;
        for (final Map.Entry<String,? extends Number> vp : pl)
        {
            final String    vn=(null == vp) ? null : vp.getKey();
            final Boolean    vt=NumberTables.checkNumericalValue(vn);
            final Number    vv=(null == vp) ? null : vp.getValue();
            final int        sv=((null == vt) || (!vt.booleanValue())) ? (-1) : Integer.parseInt(vn),
                            ev=(sv >= 0) ? sv + 1 : (-1);
            if ((null == vv) || (vv.intValue() <= 0) || (sv <= 0) || (ev <= 0))
                continue;

            if (null == ds)
                ds = new DefaultCategoryDataset();

            final String    colKey=String.valueOf(sv * DEFAULT_DISTTRIBUTION_FACTOR) + "-" + String.valueOf(ev * DEFAULT_DISTTRIBUTION_FACTOR);
            ds.addValue(vv, ttl, colKey);
        }

        return ds;
    }

    private static final Component createChartPanel (final JFreeChart chart)
    {
        return (chart == null) ? null : new ChartPanel(chart);
    }

    private static final Component populatePieChart (final String                        tabName,
                                                       final Map<String,? extends Number>    distsMap)
    {
        final CategoryDataset    ds=
            ((tabName == null) || (tabName.length() <= 0)) ? null : createCategoryDataset(tabName, distsMap);
        final PieDataset        pds=
            (null == ds) ? null : DatasetUtilities.createPieDatasetForRow(ds, tabName);
        final JFreeChart         chart=(null == pds) ? null : ChartFactory.createPieChart(
                                                            tabName,    // chart title
                                                            pds,    // dataset
                                                            false,  // legend
                                                            true,    // tooltips
                                                            false    // url(s)
                                                        );
        final Plot                p=(null == chart) ? null : chart.getPlot();
        if (p instanceof PiePlot)
            ((PiePlot) p).setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1} / {3}) {2}"));

        return createChartPanel(chart);
    }

    public static final Triplet<Number,Number,Number> calculateDatasetCounts (
            final TimeSeriesTypeCase tsType, final Collection<? extends Number>    vl)
    {
        if ((null == tsType) || (null == vl) || (vl.size() <= 0))
            return new Triplet<Number,Number,Number>(Long.valueOf(0L), Long.valueOf(0L), Long.valueOf(0L));

        final Number    minVal=AggregateFunctions.MIN.aggregate(vl),
                        maxVal=AggregateFunctions.MAX.aggregate(vl),
                        avgVal=AggregateFunctions.AVG.aggregate(vl);
        return new Triplet<Number,Number,Number>(minVal, maxVal, avgVal);
    }
    /**
     * @param tsType The {@link TimeSeriesTypeCase} whose values are being used
     * @param vals A {@link Values} dataset
     * @return A {@link Triplet} of {@link Number}-s representing the
     * min./max./avg. value(s) (in this order).
     */
    public static final Triplet<Number,Number,Number> calculateDatasetCounts (
            final TimeSeriesTypeCase tsType, final Values vals)
    {
        return calculateDatasetCounts(tsType, DatasetUtils.extractValues(vals));
    }

    private static final Component createCountEntry (
            final TimeSeriesTypeCase tsType, final String name, final Number val)
    {
        if ((null == tsType) || (null == name) || (name.length() <= 0) || (null == val))
            return null;

        final LRFieldWithLabelPanel    txtPnl=new LRFieldWithLabelPanel();
        txtPnl.setTitle(name);
        txtPnl.setEditable(false);
        txtPnl.setText(String.valueOf(val.intValue()));
        return txtPnl;
    }

    private static final Component createCountsPanel (
            final TimeSeriesTypeCase            tsType,
            final Triplet<Number,Number,Number> counts)
    {
        if ((null == tsType) || (null == counts) || counts.isEmpty())
            return null;

        final Component[]    ca={
                createCountEntry(tsType, "Min.:", counts.getV1()),
                createCountEntry(tsType, "Max.:", counts.getV2()),
                createCountEntry(tsType, "Avg.:", counts.getV3())
            };
        final JPanel    cp=new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        for (final Component c : ca)
        {
            if (null == c)
                continue;
            cp.add(c);
        }

        return cp;
    }

    private static final Component createCountChart (
            final TimeSeriesTypeCase tsType, final String title, final Number value)
    {
        if ((null == tsType)
         || (null == title) || (title.length() <= 0)
         || (null == value))
            return null;

        final EntryThermometerPlot    plot=
            EntryThermometerPlot.createEntryThermometerPlot(tsType);
        if (null == plot)
            return null;
        plot.setDataset(new DefaultValueDataset(value));

        return createChartPanel(new JFreeChart(title,      // chart title
                                                JFreeChart.DEFAULT_TITLE_FONT,
                                                 plot,      // plot
                                                 false));    // create legend
    }

    private static final Component createCountsCharts (
            final TimeSeriesTypeCase            tsType,
            final Triplet<Number,Number,Number>    counts)
    {
        if ((null == tsType) || (null == counts) || counts.isEmpty())
            return null;

        final Component[]    ca={
                createCountChart(tsType, "Min.", counts.getV1()),
                createCountChart(tsType, "Max.", counts.getV2()),
                createCountChart(tsType, "Avg.", counts.getV3())
            };
        final JPanel    p=new JPanel(new GridLayout(1, 3, 5, 0));
        for (final Component c : ca)
        {
            if (null == c)
                continue;
            p.add(c);
        }

        return p;
    }

    private static final Map.Entry<Container,JTabbedPane> updateTabbedPane (
            final String tabName, final JFreeChart chart,
            final TimeSeriesTypeCase    tsType,
            final Collection<? extends Number>    vl)
    {
        if (null == chart)
            return null;

        final Container    encloser=new JPanel(new BorderLayout(5, 5));
        encloser.setName(tabName);

        final JTabbedPane    chartsTab=
            new JTabbedPane(SwingConstants.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
        chartsTab.addTab("XY Chart", createChartPanel(chart));
        encloser.add(chartsTab, BorderLayout.CENTER);

        final Triplet<Number,Number,Number>    counts=calculateDatasetCounts(tsType, vl);
        final Component                        pnlCounts=createCountsPanel(tsType, counts);
        if (pnlCounts != null)
            encloser.add(pnlCounts, BorderLayout.SOUTH);

        final Component    chartCounts=createCountsCharts(tsType, counts);
        if (chartCounts != null)
            chartsTab.addTab("Extra", chartCounts);

        return new MapEntryImpl<Container,JTabbedPane>(encloser,chartsTab);
    }

    private static final Map.Entry<Container,JTabbedPane> updateTabbedPane (
            final String tabName, final JFreeChart chart, final TimeSeries ts)
    {
        return  updateTabbedPane(tabName, chart, TimeSeriesTypeCase.fromTimeSeries(ts), DatasetUtils.extractValues(ts));
    }

    private static final TimeSeries createAveragesTimeSeries (final TimeSeries ts)
    {
        final Collection<? extends Number>    vals=DatasetUtils.extractValues(ts);
        final int                            numValues=(null == vals) ? 0 : vals.size();
        final List<? extends Number>        avgs=AggregateFunctions.AVG.accumulate(vals);
        final int                            numAvgs=(null == avgs) ? 0 : avgs.size();
        if (numAvgs != numValues)
            throw new IllegalStateException("Mismatched number of averages (" + numAvgs + ") and values (" + numValues + ")");

        final Comparable<?>    tsKey=ts.getKey();
        final TimeSeries    tsAvg=new TimeSeries(tsKey.toString() + " [" + AggregateFunctions.AVG.name() + "]");
        for (int    vIndex=0; vIndex < numValues; vIndex++)
        {
            final RegularTimePeriod    period=ts.getTimePeriod(vIndex);
            final Number            value=avgs.get(vIndex);
            tsAvg.add(period, value);
        }

        return tsAvg;
    }

    private static final Map<String,Triplet<Container,JTabbedPane,Map<String,? extends Number>>> createXYCharts (
            final Collection<? extends TimeSeries> tsl)
    {
        if ((null == tsl) || (tsl.size() <= 0))
            return null;

        Map<String,Triplet<Container,JTabbedPane,Map<String,? extends Number>>>    panesMap=null;
        for (final TimeSeries ts : tsl)
        {
            final Comparable<?>    name=(null == ts) ? null : ts.getKey();
            final String        ttl=(null == name) ? null : String.valueOf(name);
            if ((null == ttl) || (ttl.length() <= 0))
                 continue;

            final TimeSeriesCollection    tsCollection=new TimeSeriesCollection(ts);
            tsCollection.addSeries(createAveragesTimeSeries(ts));
            final JFreeChart chart=ChartFactory.createTimeSeriesChart(
                    ttl, // title
                    "Date", // x-axis label
                    ttl, // y-axis label
                    tsCollection, // data
                    true, // create legend?
                    true, // generate tooltips?
                    false // generate URLs?
                );
            chart.setBackgroundPaint(Color.white);

            final Plot    plot=chart.getPlot();
            if (plot instanceof XYPlot)
            {
                final XYPlot    xyPlot=(XYPlot) plot;
                xyPlot.setDomainGridlinePaint(Color.white);
                xyPlot.setDomainMinorGridlinePaint(Color.white);

                xyPlot.setRangeMinorGridlinePaint(Color.white);
                xyPlot.setRangeGridlinePaint(Color.white);

                final XYItemRenderer r=xyPlot.getRenderer();
                if (r instanceof XYLineAndShapeRenderer)
                {
                    final XYLineAndShapeRenderer renderer=(XYLineAndShapeRenderer) r;
                    renderer.setBaseShapesVisible(true);
                    renderer.setBaseShapesFilled(true);
                }
            }

            final Map.Entry<Container,JTabbedPane>    paneData=updateTabbedPane(ttl, chart, ts);
            if (paneData == null)
                continue;

            final Map<String,? extends Number>    dm=createDistributionsMap(ts);
            if (null == panesMap)
                panesMap = new TreeMap<String,Triplet<Container,JTabbedPane,Map<String,? extends Number>>>(String.CASE_INSENSITIVE_ORDER);
            panesMap.put(ttl, new Triplet<Container,JTabbedPane,Map<String,? extends Number>>(paneData.getKey(), paneData.getValue(), dm));
        }

        return panesMap;
    }

    private JTabbedPane    _tabs;
    protected void populateCharts (final Collection<? extends TimeSeries> tsl)
    {
        final Map<String,? extends Triplet<? extends Component,JTabbedPane,? extends Map<String,? extends Number>>>    xyRes=createXYCharts(tsl);
        if ((xyRes == null) || xyRes.isEmpty())
            return;
        for (final Map.Entry<String,? extends Triplet<? extends Component,JTabbedPane,? extends Map<String,? extends Number>>> mapEntry : xyRes.entrySet())
        {
            final String                        tabName=(mapEntry == null) ? null : mapEntry.getKey();
            final Triplet<? extends Component, JTabbedPane,? extends Map<String,? extends Number>>    tabEntry=(mapEntry == null) ? null : mapEntry.getValue();
            final Component                        tabPane=(tabEntry == null) ? null : tabEntry.getV1();
            final JTabbedPane                    tabCharts=(tabEntry == null) ? null : tabEntry.getV2();
            final Map<String,? extends Number>    distsMap=(tabEntry == null) ? null : tabEntry.getV3();
            if (tabPane == null)
                continue;

            final Component    pieChart=(tabCharts == null) ? null : populatePieChart(tabName, distsMap);
            if (pieChart != null)
                tabCharts.addTab("Piechart", pieChart);
            _tabs.addTab(tabName, tabPane);
        }
    }

    private ChartPopulator    _popl;
    private File    _currentFile;
    public final File getCurrentFile ()
    {
        return _currentFile;
    }

    void signalChartPopulatorEnd (ChartPopulator p)
    {
        if (_popl == p)
        {
            _currentFile = p.getDataFile();
            _popl = null;
        }
        else
            getLogger().error("signalChartPopulatorEnd(" + p + ") unknown instance");
    }

    private OptionalDateControl<?>    _startDateControl;
    public Calendar getStartDate ()
    {
        if ((null == _startDateControl) || (!_startDateControl.isSelected()))
            return null;

        return  _startDateControl.getValue(false);
    }

    protected void setStartDate (Calendar c)
    {
        if (_startDateControl != null)
        {
            _startDateControl.setValue(c);
            _startDateControl.setSelected(c != null);
        }
    }

    private OptionalDateControl<?>    _endDateControl;
    public Calendar getEndDate ()
    {
        if ((null == _endDateControl) || (!_endDateControl.isSelected()))
            return null;

        return _endDateControl.getValue(true);
    }

    protected void setEndDate (Calendar c)
    {
        if (_endDateControl != null)
        {
            _endDateControl.setValue(c);
            _endDateControl.setSelected(c != null);
        }
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, String cmd, Element dlgElement)
    {
        final String filePath=(null == f) ? null : f.getAbsolutePath();
        if ((null == filePath) || (filePath.length() <= 0) || (null == _tabs))
            return;

        if (_popl != null)
        {
            JOptionPane.showMessageDialog(this, "Wait for processing end", "Processing in progress", JOptionPane.WARNING_MESSAGE);
            return;
        }

        _tabs.removeAll();
        _popl = new ChartPopulator(this, f);
        _popl.execute();
    }
    /*
     * @see net.community.apps.common.FilesLoadMainFrame#setMainMenuItemsActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer)
     */
    @Override
    protected Map<String,JMenuItem> setMainMenuItemsActionHandlers (MenuItemExplorer ie)
    {
        Map<String,JMenuItem>    im=super.setMainMenuItemsActionHandlers(ie);
        final JMenuItem            item=MenuUtil.addMenuItemActionHandler(ie, "refresh", new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent event)
                {
                    if (event != null)
                        refresh();
                }
            });

        if (item != null)
        {
            if (null == im)
                im = new TreeMap<String,JMenuItem>(String.CASE_INSENSITIVE_ORDER);
            im.put(item.getActionCommand(), item);
        }

        return im;
    }

    protected void refresh ()
    {
        loadFile(getCurrentFile(), LOAD_CMD, null);
    }

    private <S extends AbstractSpinnerDateTimeControl, C extends AbstractOptionalSpinnerDateTimeControl<S>> C updateTimeSelectionControl (
            final C c, final String text)
    {
        if (c == null)
            return null;

        c.setText(text);
        c.setSelected(false);
        return c;
    }

    private Component createDateSelectionPanel ()
    {
        final Container    c=new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        if (null == _startDateControl)
            _startDateControl = updateTimeSelectionControl(new OptionalDateControl.DefaultOptionalDateControl(), "From:");
        c.add(_startDateControl);

        if (null == _endDateControl)
            _endDateControl = updateTimeSelectionControl(new OptionalDateControl.DefaultOptionalDateControl(), "To:");
        c.add(_endDateControl);

        return c;
    }

    private JComboBox    _bikingState;
    private Component createBikingStateSelectionPanel ()
    {
        final Container    c=new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        c.add(new JLabel("Biking state: "));
        if (_bikingState == null)
            _bikingState = new JComboBox(new String[] { "None", "Bike", "No bike" });
        _bikingState.setSelectedIndex(0);
        _bikingState.addActionListener(new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    if (e != null)
                        refresh();
                }
            });
        c.add(_bikingState);
        return c;
    }

    public Boolean getBikingState ()
    {
        final Object    selValue=(_bikingState == null) ? null : _bikingState.getSelectedItem();
        final String    selString=String.valueOf(selValue);
        if ((selValue == null) || "none".equalsIgnoreCase(selString))
            return null;
        else if ("bike".equalsIgnoreCase(selString))
            return Boolean.TRUE;
        else
            return Boolean.FALSE;
    }

    private OptionalTimeControl<?>    _startTimeControl, _endTimeControl;
    public Calendar getStartTime ()
    {
        if ((_startTimeControl == null) || (!_startTimeControl.isSelected()))
            return null;

        return _startTimeControl.getValue();
    }

    public Calendar getEndTime ()
    {
        if ((_endTimeControl == null) || (!_endTimeControl.isSelected()))
            return null;

        return _endTimeControl.getValue();
    }

    private Component createTimeSelectionPanel ()
    {
        final Container    c=new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        if (_startTimeControl == null)
            _startTimeControl = updateTimeSelectionControl(new OptionalTimeControl.DefaultOptionalTimeControl(), "From:");
        c.add(_startTimeControl);

        if (_endTimeControl == null)
            _endTimeControl = updateTimeSelectionControl(new OptionalTimeControl.DefaultOptionalTimeControl(), "To:");
        c.add(_endTimeControl);

        return c;
    }

    private JPanel    _northPanel;
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Container    ctPane=getContentPane();
        if (null == _northPanel)
        {
            final Component[]    ca={
                    createDateSelectionPanel(),
                    createTimeSelectionPanel(),
                    createBikingStateSelectionPanel()
                };
            _northPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            AWTUtils.addComponents(_northPanel, ca);
            ctPane.add(_northPanel, BorderLayout.NORTH);
        }

        if (null == _tabs)
        {
            _tabs = new JTabbedPane();

            setDropTarget(new DropTarget(_tabs, this));
            ctPane.add(_tabs, BorderLayout.CENTER);
        }
    }

    public TestCSVChartFrame (String... args) throws Exception
    {
        super(args);

        if ((args != null) && (args.length > 0))
        {
            setStartDate((args.length > 1) ? DateUtil.parseStringToDate(args[1], true) : null);
            setEndDate((args.length > 2) ? DateUtil.parseStringToDate(args[2], true) : null);

            final String    filePath=SystemPropertiesResolver.SYSTEM.format(args[0]);
            loadFile(new File(filePath), LOAD_CMD, null);
        }
    }
}
