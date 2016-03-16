/*
 *
 */
package net.community.chest.spring.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.NumberValueStringConstructor;
import net.community.chest.spring.test.beans.DateTimeEntitySortOrder;
import net.community.chest.spring.test.beans.TestBeanService;
import net.community.chest.spring.test.beans.TestEmbeddingService;
import net.community.chest.spring.test.beans.TestTreeService;
import net.community.chest.spring.test.entities.DateTimeEntity;
import net.community.chest.spring.test.entities.EmbeddedEntity;
import net.community.chest.spring.test.entities.EmbeddingEntity;
import net.community.chest.spring.test.entities.IdableEntityComparator;
import net.community.chest.spring.test.entities.NodeEntity;
import net.community.chest.spring.test.resources.TestResourcesAnchorContext;
import net.community.chest.test.TestBase;
import net.community.chest.util.collection.CollectionsUtils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Lyor G.
 * @since Jul 20, 2010 9:34:55 AM
 */
public class SpringContextTester extends TestBase {
    protected SpringContextTester ()
    {
        super();
    }

    //////////////////////////////////////////////////////////////////////////

    public static final void testSpringApplicationContext (
            final BufferedReader in, final PrintStream out,
            final ApplicationContext ctx, final String ... args)
    {
        final int                numArgs=(null == args) ? 0 : args.length;
        final TestBeanService    svc=ctx.getBean(TestBeanService.class);
        for (int    aIndex=0; ; aIndex++)
        {
            final String    ov=
                (aIndex < numArgs) ? args[aIndex] : getval(out, in, "output value (or Quit)");
            if ((null == ov) || (ov.length() <= 0))
                continue;
            if (isQuit(ov))
                break;

            try
            {
                svc.append(ov);
                svc.println();
            }
            catch(IOException e)
            {
                System.err.append(e.getClass().getName())
                          .append(": ")
                          .append(e.getMessage())
                    .println()
                    ;
            }
        }
    }

    /* -------------------------------------------------------------------- */

    private static final List<DateTimeEntitySortOrder> promptEntitiesSortOrder (
            final BufferedReader in, final PrintStream out)
    {
        final String                    promptVal;
        final DateTimeEntitySortOrder[]    vals=DateTimeEntitySortOrder.getValues();
        final int                        numVals=(null == vals) ? 0 : vals.length;
        {
            final StringBuilder    sb=new StringBuilder(32 + Math.max(numVals,0) * 12)
                                        .append("sort order ")
                                        ;
            for (int    vIndex=0; vIndex < numVals; vIndex++)
            {
                final DateTimeEntitySortOrder    v=vals[vIndex];
                final char                        c=(null == v) ? '\0' : v.getOpChar();
                final String                    n=(null == v) ? null : v.toString();
                if ((null == n) || (n.length() <= 0) || (c <= '\0'))
                    continue;

                sb.append(c)
                  .append('-')
                  .append(n)
                  .append('/')
                  ;
            }
            sb.append("[Q]uit");
            promptVal = sb.toString();
        }

        List<DateTimeEntitySortOrder>    ret=null;
        for ( ; ; )
        {
            final String    c=getval(out, in, promptVal);
            if ((null == c) || (c.length() <= 0) || isQuit(c))
                break;

            final DateTimeEntitySortOrder    o=DateTimeEntitySortOrder.fromOpChar(c.charAt(0));
            if (null == o)
                continue;

            if (null == ret)
                ret = new ArrayList<DateTimeEntitySortOrder>(Math.max(1, numVals));
            if (!ret.add(o))
                continue;
        }

        return ret;
    }

    /* -------------------------------------------------------------------- */

    private static final List<DateTimeEntity> listEntities (
            final BufferedReader in, final PrintStream out,
            final TestBeanService svc, final boolean promptSelection)
    {
        final Collection<DateTimeEntitySortOrder>    listOrder=
            promptEntitiesSortOrder(in, out);
        final List<DateTimeEntity>                    el=
            svc.listDateTimeEntities(listOrder);
        final int                                    numRes=
            (null == el) ? 0 : el.size();

        for ( ; ; )
        {
            for (int    eIndex=0; eIndex < numRes; eIndex++)
            {
                final DateTimeEntity    dte=el.get(eIndex);
                out.append('\t')
                   .append(promptSelection ? String.valueOf(eIndex) : "")
                   .append(promptSelection ? ") " : "")
                   .append(dte.toString())
                   .println()
                   ;
            }
            if ((numRes <= 0) || (!promptSelection))
                return el;

            final String    ans=getval(out, in, "selected items (or Quit)");
            if ((null == ans) || (ans.length() <= 0))
                continue;
            if (isQuit(ans))
                return null;

            final Collection<String>    vl=StringUtil.splitString(ans, ',');
            final List<Integer>            idxList;
            try
            {
                idxList = CollectionsUtils.instantiateObjects(vl, NumberValueStringConstructor.INTEGER);
            }
            catch(Exception e)
            {
                System.err.append(e.getClass().getName())
                            .append(" while parsing ")
                            .append(ans)
                            .append(": ")
                            .append(e.getMessage())
                      .println()
                      ;
                continue;
            }

            final int                numSelected=(null == idxList) ? 0 : idxList.size();
            List<DateTimeEntity>    selList=null;
            final Set<Integer>        usedIndex=new TreeSet<Integer>();
            for (int    sIndex=0; sIndex < numSelected; sIndex++)
            {
                final Integer            iVal=idxList.get(sIndex);
                final int                i=(null == iVal) ? (-1) : iVal.intValue();
                final DateTimeEntity    dte=
                    ((i < 0) || (i >= numRes) || usedIndex.contains(iVal)) ? null : el.get(i);
                if (null == dte)
                    continue;

                if (null == selList)
                    selList = new ArrayList<DateTimeEntity>(numSelected);
                if (!selList.add(dte))
                    continue;
            }

            if ((selList != null) && (selList.size() > 0))
                return selList;
        }
    }

    /* -------------------------------------------------------------------- */

    private static final Set<DateTimeEntity> deleteEntities (
            final BufferedReader in, final PrintStream out, final TestBeanService svc)
    {
        for (Set<DateTimeEntity>    delList=null ; ; )
        {
            final List<DateTimeEntity>    el=listEntities(in, out, svc, true);
            if ((null == el) || (el.size() <= 0))
                return delList;

            for (final DateTimeEntity dte : el)
            {
                if (null == dte)
                    continue;
                if ((delList != null) && delList.contains(dte))
                    continue;

                try
                {
                    svc.deleteDateTimeEntity(dte);
                }
                catch(Exception e)
                {
                    System.err.append(e.getClass().getName())
                                .append(" while delete ")
                                .append(dte.toString())
                                .append(" entry: ")
                                .append(e.getMessage())
                          .println()
                          ;
                    e.printStackTrace(System.err);
                    continue;
                }

                if (null == delList)
                    delList = new TreeSet<DateTimeEntity>(IdableEntityComparator.DEFAULT);
                if (!delList.add(dte))
                    continue;
            }
        }
    }

    /* -------------------------------------------------------------------- */

    private static final Set<DateTimeEntity> updateEntities (
            final BufferedReader in, final PrintStream out, final TestBeanService svc)
    {
        Set<DateTimeEntity>    delList=null;
        for (final Random    r=new Random(System.currentTimeMillis()) ; ; )
        {
            final List<DateTimeEntity>    el=listEntities(in, out, svc, true);
            if ((null == el) || (el.size() <= 0))
                return delList;

            for (final DateTimeEntity dte : el)
            {
                if (null == dte)
                    continue;
                if ((delList != null) && delList.contains(dte))
                    continue;

                final long    newDate=TimeUnit.HOURS.toMillis(r.nextInt(Short.MAX_VALUE));
                dte.setDateValue(new Date(newDate));
                dte.setDescription(DateTimeEntity.getDefaultDescription(dte.getDateValue()));
                try
                {
                    svc.updateDateTimeEntity(dte);
                }
                catch(Exception e)
                {
                    System.err.append(e.getClass().getName())
                                .append(" while delete ")
                                .append(dte.toString())
                                .append(" entry: ")
                                .append(e.getMessage())
                          .println()
                          ;
                    e.printStackTrace(System.err);
                    continue;
                }

                if (null == delList)
                    delList = new TreeSet<DateTimeEntity>(IdableEntityComparator.DEFAULT);
                if (!delList.add(dte))
                    continue;
            }
        }
    }

    /* -------------------------------------------------------------------- */

    public static final void testSpringDao (final BufferedReader in, final PrintStream out,
            final ApplicationContext ctx, final String ... args)
    {
        final TestBeanService    svc=ctx.getBean(TestBeanService.class);
        if ((args != null) && (args.length > 0))
        {
            final Random    r=new Random(System.currentTimeMillis());
            for (final String aName : args)
            {
                final long    timestamp=TimeUnit.SECONDS.toMillis(r.nextInt(Short.MAX_VALUE));
                try
                {
                    final DateTimeEntity    dte=svc.createDateTimeEntity(aName, timestamp);
                    if (null == dte)
                        throw new IllegalStateException("No entity created");
                }
                catch(Exception e)
                {
                    System.err.append(e.getClass().getName())
                                .append(" while create ")
                                .append(aName)
                                .append(" entry: ")
                                .append(e.getMessage())
                          .println()
                          ;
                    e.printStackTrace(System.err);
                }
            }
        }

        for (int    aIndex=0; aIndex <= Short.MAX_VALUE ; aIndex++)
        {
            final String    op=getval(out, in, "[L]ist/(C)reate/(U)pdate/(D)elete/(Q)uit");
            if (isQuit(op))
                break;

            final char    opChar=
                ((null == op) || (op.length() <= 0)) ? '\0' : Character.toUpperCase(op.charAt(0));
            try
            {
                switch(opChar)
                {
                    case '\0'    :
                    case 'L'    :
                        listEntities(in, out, svc, false);
                        break;

                    case 'D'    :
                        deleteEntities(in, out, svc);
                        break;

                    case 'U'    :
                        updateEntities(in, out, svc);
                        break;

                    default        :
                }
            }
            catch(Exception e)
            {
                System.err.append(e.getClass().getName())
                            .append(" while handle op=")
                            .append(op)
                            .append(": ")
                            .append(e.getMessage())
                      .println()
                      ;
                e.printStackTrace(System.err);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    private static final int    DEFAULT_DEPTH=10, DEFAULT_FANOUT=5;
    private static final NodeEntity createTreeNode (
            final TestTreeService    svc, final BufferedReader in, final PrintStream out)
    {
        final String    depthVal=getval(out, in, "depth [default=" + DEFAULT_DEPTH + "]/(Q)uit");
        if (isQuit(depthVal))
            return null;

        final String    fanVal=getval(out, in, "fan-out [default=" + DEFAULT_FANOUT + "]/(Q)uit");
        if (isQuit(fanVal))
            return null;

        return svc.createNewTree(StringUtils.hasLength(depthVal) ? Integer.parseInt(depthVal) : DEFAULT_DEPTH,
                                 StringUtils.hasLength(fanVal) ? Integer.parseInt(fanVal) : DEFAULT_FANOUT);
    }

    /* -------------------------------------------------------------------- */

    private static final Long deleteTreeNode (
            final TestTreeService    svc, final BufferedReader in, final PrintStream out)
    {
        final String    idValue=getNonEmptyValue(out, in, "node ID (or Quit)");
        if (isQuit(idValue))
            return null;

        final Long            nodeId=Long.valueOf(idValue);
        final NodeEntity    node=svc.deleteTree(nodeId);
        if (node == null)
            return null;

        return nodeId;
    }

    /* -------------------------------------------------------------------- */

    public static final void testSpringTreePersistence (final BufferedReader in, final PrintStream out,
            final ApplicationContext ctx, final String ... args)
    {
        final TestTreeService    svc=ctx.getBean(TestTreeService.class);
        for (final List<NodeEntity>    trees=new ArrayList<NodeEntity>(); ; )
        {
            final String    op=getval(out, in, "[L]ist/(C)reate/(U)pdate/(D)elete/(Q)uit");
            if (isQuit(op))
                break;

            final char    opChar=
                ((null == op) || (op.length() <= 0)) ? '\0' : Character.toUpperCase(op.charAt(0));
            try
            {
                switch(opChar)
                {
                    case 'L'    :
                    case '\0'    :
                        {
                            final Collection<? extends NodeEntity>    roots=svc.listRoots();
                            final int                                numRoots=(roots == null) ? 0 : roots.size(),
                                                                    numTrees=trees.size();
                            if (numRoots != numTrees)
                                throw new IllegalStateException("Mismatched number of roots");
                            if (numRoots <= 0)
                                continue;

                            for (final NodeEntity rootNode : roots)
                            {
                                final NodeEntity    treeNode=CollectionsUtils.findElement(trees, rootNode, IdableEntityComparator.DEFAULT);
                                if (treeNode == null)
                                    throw new NoSuchElementException("Root not found");
                                out.append('\t').println(treeNode);
                            }
                        }
                        break;

                    case 'C'    :
                        {
                            final NodeEntity    root=createTreeNode(svc, in, out);
                            if (root != null)
                                trees.add(root);
                        }
                        break;

                    case 'D'    :
                        {
                            final Long    rootId=deleteTreeNode(svc, in, out);
                            if (rootId != null)
                            {
                                final int    nodeIndex=IdableEntityComparator.indexOf(rootId, trees);
                                if (nodeIndex < 0)
                                    throw new NoSuchElementException("Root not found");
                                trees.remove(nodeIndex);
                            }
                        }
                        break;

                    default        :
                }
            }
            catch(Exception e)
            {
                System.err.append(e.getClass().getName())
                            .append(" while handle op=")
                            .append(op)
                            .append(": ")
                            .append(e.getMessage())
                      .println()
                      ;
                e.printStackTrace(System.err);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    private static final EmbeddingEntity createEmbeddingEntity (
            final TestEmbeddingService    svc, final BufferedReader in, final PrintStream out)
    {
        final EmbeddingEntity    entity=new EmbeddingEntity();
        final Date                dtv=new Date(System.currentTimeMillis());
        entity.setName(String.valueOf(dtv.getTime()));
        entity.setDescription(DateFormat.getDateTimeInstance().format(dtv));

        final List<EmbeddedEntity>    embList=new ArrayList<EmbeddedEntity>();
        for (int    eIndex=0; eIndex < Byte.MAX_VALUE; eIndex++)
        {
            final String    name=getval(out, in, "embedded name (or Quit)");
            if (isQuit(name))
                break;

            final String    address=getval(out, in, "embedded address (or Quit)");
            if (isQuit(address))
                break;

            if (!embList.add(new EmbeddedEntity("".equals(name) ? null : name, "".equals(address) ? null: address)))
                continue;    // debug breakpoint
        }

        entity.setEmbeddedList(embList);
        svc.create(entity);
        return entity;
    }

    /* -------------------------------------------------------------------- */

    public static final void testEmbeddedCollectionPersistence (final BufferedReader in, final PrintStream out,
            final ApplicationContext ctx, final String ... args)
    {
        final TestEmbeddingService    svc=ctx.getBean(TestEmbeddingService.class);
        for ( ; ; )
        {
            final String    op=getval(out, in, "[L]ist/(C)reate/(U)pdate/(D)elete/(Q)uit");
            if (isQuit(op))
                break;

            final char    opChar=
                ((null == op) || (op.length() <= 0)) ? '\0' : Character.toUpperCase(op.charAt(0));
            try
            {
                switch(opChar)
                {
                    case 'L'    :
                    case '\0'    :
                        {
                            final Collection<? extends EmbeddingEntity>    eList=svc.list();
                            if (CollectionUtils.isEmpty(eList))
                                continue;

                            for (final EmbeddingEntity entity : eList)
                                out.append('\t').println(entity);
                        }
                        break;

                    case 'C'    :
                        {
                            final EmbeddingEntity    entity=createEmbeddingEntity(svc, in, out);
                            out.append('\t').println(entity);
                        }
                        break;

                    default        :
                }
            }
            catch(Exception e)
            {
                System.err.append(e.getClass().getName())
                            .append(" while handle op=")
                            .append(op)
                            .append(": ")
                            .append(e.getMessage())
                      .println()
                      ;
                e.printStackTrace(System.err);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    private static final void runTest (
            final BufferedReader in, final PrintStream out,
            final ApplicationContext ctx, final String ... args)
    {
        final DBSessionTrackerHelper    hlpr=new DBSessionTrackerHelper(ctx);
        try
        {
            if (hlpr.startSession())
                out.println("Session started");

//            testSpringApplicationContext(in, out, ctx, args);
//            testSpringDao(in, out, ctx, args);
//            testSpringTreePersistence(in, out, ctx, args);
            testEmbeddedCollectionPersistence(in, out, ctx, args);
        }
        finally
        {
            if (hlpr.endSession())
                out.println("Session ended");
        }

    }
    public static final void main (String[] args)
    {
        final ApplicationContext    ctx=new TestResourcesAnchorContext();
        if (ctx instanceof AbstractApplicationContext)
            ((AbstractApplicationContext) ctx).registerShutdownHook();

        try
        {
            runTest(getStdin(), System.out, ctx, args);
        }
        catch(Exception e)
        {
            System.err.append(e.getClass().getName())
                        .append(": ")
                        .append(e.getMessage())
                  .println()
                  ;
            e.printStackTrace(System.err);
        }
    }
}
