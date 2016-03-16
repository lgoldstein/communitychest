/*
 *
 */
package net.community.chest.win32.core.serial;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.io.input.TrackingInputStream;
import net.community.chest.test.TestBase;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 6, 2013 2:44:52 PM
 */
public class SerialFormatTester extends TestBase {
    public SerialFormatTester ()
    {
        super();
    }

    /* -------------------------------------------------------------------- */

    private static void readClassWithMembersAndTypesRecord(final PrintStream out, final AbstractClassWithMembersAndTypes record,
                                                           final String indent, final Map<Long,ObjectIdCarrier>    refsMap, final InputStream inStream)
        throws IOException
    {
        final ClassInfo                        classInfo=record.getClassInfo();
        final List<String>                    namesList=classInfo.getMemberNames();
        final int                            numMembers=CollectionsUtils.size(namesList);
        final MemberTypeInfo                memberInfo=record.getMemberTypeInfo();
        final List<BinaryTypeEnumeration>    typesList=memberInfo.getMemberTypes();
        if (numMembers != CollectionsUtils.size(typesList))
            throw new StreamCorruptedException("Mismatched names vs. types list size for " + classInfo);

        final List<?>    additionalInfos=memberInfo.getAdditionalInfos();
        if (numMembers != CollectionsUtils.size(additionalInfos))
            throw new StreamCorruptedException("Mismatched name vs. additional infos list size for " + classInfo);

        for (int    index=0; index < numMembers; index++)
        {
            final String                name=namesList.get(index);
            final BinaryTypeEnumeration    binType=typesList.get(index);
            switch(binType)
            {
                case ObjectType    :
                case StringType    :
                case ClassType    :
                case SystemClassType:
                case ObjectArrayType:
                case PrimitiveArrayType:
                case StringArrayType:
                    readRecord(out, indent + "\t" + name + "[" + binType + "]: ", refsMap, inStream);
                    break;

                case PrimitiveType:
                    {
                        final Object    infoValue=additionalInfos.get(index);
                        if (!(infoValue instanceof PrimitiveTypeEnumeration))
                            throw new StreamCorruptedException("No primitive info (" + infoValue + ") for field=" + name + " of " + classInfo);

                        final PrimitiveTypeEnumeration    primType=(PrimitiveTypeEnumeration) infoValue;
                        final Object                    primValue=primType.readValue(inStream);
                        out.append(indent)
                           .append('\t').append(name)
                           .append('[').append(primType.toString()).append(']')
                           .append(": ").println(primValue);
                    }
                    break;

                default    :
                    throw new UnsupportedOperationException("Unknown binary type: " + binType);
            }
        }
    }

    /* -------------------------------------------------------------------- */

    private static SerializationRecord readRecord(
            final PrintStream out, final String indent, final Map<Long,ObjectIdCarrier>    refsMap, final InputStream inStream)
        throws IOException
    {
        RecordTypeEnumeration    recordType=RecordTypeEnumeration.read(inStream);
        final SerializationRecord    record;
        switch(recordType)
        {
            case SerializedStreamHeader    :
                record = new SerializationHeaderRecord();
                break;

            case ClassWithId    :
                record = new ClassWithIdRecord();
                break;

            case BinaryLibrary    :
                record = new BinaryLibraryRecord();
                break;

            case ObjectNullMultiple256    :
                record = new ObjectNullMultiple256Record();
                break;

            case ClassWithMembersAndTypes    :
                record = new ClassWithMembersAndTypesRecord();
                break;

            case ClassWithMembers    :
                record = new ClassWithMembersRecord();
                break;

            case SystemClassWithMembersAndTypes    :
                record = new SystemClassWithMembersAndTypes();
                break;

            case BinaryObjectString    :
                record = new BinaryObjectStringRecord();
                break;

            case BinaryArray:
                record = new BinaryArrayRecord();
                break;

            case MemberPrimitiveTyped    :
                record = new MemberPrimitiveTypedRecord();
                break;

            case MemberReference    :
                record = new MemberReferenceRecord();
                break;

            case ObjectNull    :
                record = new ObjectNullRecord();
                break;

            case MessageEnd    :
                record = new MessageEndRecord();
                break;

            case ArraySingleString    :
                record = new ArraySingleStringRecord();
                break;

            default    :
                throw new StreamCorruptedException("Unknown record type: " + recordType);

        }
        System.err.append("========= ").append(record.getClass().getSimpleName()).println(" =============");
        record.readRecordData(inStream);

        if (record instanceof ObjectIdCarrier)
        {
            final ObjectIdCarrier    carrier=(ObjectIdCarrier) record;
            final long                objectId=carrier.getObjectId();
            final ObjectIdCarrier    prev=refsMap.put(Long.valueOf(objectId), carrier);
            if (prev != null)
                throw new StreamCorruptedException("Multiple carriers of object ID=" + objectId + ": " + prev + " / " + carrier);
            out.append(indent).append('[').append(String.valueOf(objectId)).append("]: ").println(record);
        }
        else
        {
            out.append(indent).println(record);
        }

        SerializationRecord    valuesRecord=record;
// Need to read values of fields that contain references
//        if (valuesRecord instanceof ClassWithIdRecord)
//        {
//            final ClassWithIdRecord    idRecord=(ClassWithIdRecord) valuesRecord;
//            final long                refId=idRecord.getMetadataId();
//            final ObjectIdCarrier    refRecord=refsMap.get(Long.valueOf(refId));
//            if (refRecord == null)
//                throw new StreamCorruptedException("Unmatched referenced record: " + refId);
//            if (!(refRecord instanceof SerializationRecord))
//                throw new StreamCorruptedException("Unmatched non-stream record for ref-ID=" + refId + ": " + refRecord);
//            valuesRecord = (SerializationRecord) refRecord;
//        }

        if (valuesRecord instanceof AbstractClassWithMembersAndTypes)
            readClassWithMembersAndTypesRecord(out, (AbstractClassWithMembersAndTypes) valuesRecord, indent, refsMap, inStream);
        return record;
    }

    private static final void runSerialFormatAnalyzer(final PrintStream out, final BufferedReader in,
                                                      final String indent, final InputStream inStream)
        throws IOException
    {
        Map<Long,ObjectIdCarrier>    refsMap=new HashMap<Long,ObjectIdCarrier>();
        for (SerializationRecord record=null; !(record instanceof MessageEndRecord) ; )
        {
            record = readRecord(out, indent, refsMap, inStream);
        }
    }

    /* -------------------------------------------------------------------- */

    private static final void runSerialFormatAnalyzer(final PrintStream out, final BufferedReader in, final String filePath)
    {
        for ( ; ; )
        {
            out.println("Processing " + filePath);

            try
            {
                TrackingInputStream    inStream=null;

                try
                {
                    inStream = new TrackingInputStream(
                            new BufferedInputStream(
                                    new FileInputStream(filePath), IOCopier.DEFAULT_COPY_SIZE));
                    runSerialFormatAnalyzer(out, in, "", inStream);
                }
                finally
                {
                    FileUtil.closeAll(inStream);
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }

            final String    ans=getval(out, in, "again [y]/n");
            if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }
    }

    /* -------------------------------------------------------------------- */

    // args[i] a serialized data file path
    public static final int testSerialFormatAnalyzer (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int aIndex=0; ; aIndex++)
        {
            final String    s=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "file path (or Quit)");
            final int        sLen=(null == s) ? 0 : s.length();
            if (sLen <= 0)
                continue;
            if (isQuit(s))
                break;

            runSerialFormatAnalyzer(out, in, s);
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testSerialFormatAnalyzer(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }

}
