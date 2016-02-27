/*
 * BinarySerializationStreamAnalysis - a simple demo class for parsing the 
 *  output of the BinaryFormatter class' "Serialize" method, eg counting objects and 
 *  values.
 * 
 * Copyright Tao Klerks, 2010-2011, tao@klerks.biz
 * Licensed under the modified BSD license:
 * 

Redistribution and use in source and binary forms, with or without modification, are 
permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this list of 
conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice, this list 
of conditions and the following disclaimer in the documentation and/or other materials
provided with the distribution.
 - The name of the author may not be used to endorse or promote products derived from 
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, 
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY 
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.

 * 
 */

using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

namespace BinarySerializationAnalysis
{
    //THERE'S NO ERROR-CHECKING OR EXCEPTION HANDLING!!! THIS CLASS IS EXTREMELY BRITTLE!
    // However, as all we're doing is reading from a stream into managed values and objects, there should be little security risk.
    // Worst that can happen is (hopefully) an out of memory exception.

    public class BinarySerializationStreamAnalyzer : Logger
    {
        //yeah, I know, these could be better protected...
        public Dictionary<int, SerialObject> SerialObjectsFound = null;
        public Dictionary<int, BinaryLibrary> LibrariesFound = null;

        //available to the other objects, used to read from the stream
        internal BinaryReader reader = null;

        //marks the end of the serialization stream
        private bool endRecordReached = false;

        //used for returning an arbitrary number of nulls as defined by certain record types
        private int PendingNullCounter = 0;

        public BinarySerializationStreamAnalyzer() {}

        public void Read(Stream inputStream)
        {
            //reset the state
            reader = new BinaryReader(inputStream, Encoding.UTF8);
            endRecordReached = false;
            SerialObjectsFound = new Dictionary<int, SerialObject>();
            LibrariesFound = new Dictionary<int, BinaryLibrary>();

            //dig in
            while (!endRecordReached)
            {
                ParseRecord(null);
            }
        }

        public string Analyze()
        {
            int classCount = 0;
            int arrayCount = 0;
            int stringCount = 0;
            long classLength = 0;
            long arrayLength = 0;
            long stringLength = 0;
            Dictionary<string, int> ObjectCounts = new Dictionary<string, int>();
            Dictionary<string, long> ObjectLengths = new Dictionary<string, long>();

            //we are only interested in top-level objects, not nested ones (otherwise would double-count lengths!).
            foreach (SerialObject someObject in SerialObjectsFound.Values)
            {
                if (someObject.ParentObjectID == null)
                {
                    if (someObject.GetType() == typeof(ClassInfo))
                    {
                        classCount++;
                        classLength += someObject.recordLength;

                        ClassInfo interestingClass = (ClassInfo)someObject;
                        if (interestingClass.ReferencedObject != null)
                            interestingClass = (ClassInfo)SerialObjectsFound[interestingClass.ReferencedObject.Value];

                        if (!ObjectCounts.ContainsKey(interestingClass.Name))
                        {
                            ObjectCounts.Add(interestingClass.Name, 0);
                            ObjectLengths.Add(interestingClass.Name, 0);
                        }

                        ObjectCounts[interestingClass.Name]++;
                        ObjectLengths[interestingClass.Name] += someObject.recordLength;
                    }
                    else if (someObject.GetType() == typeof(BinaryArray))
                    {
                        arrayCount++;
                        arrayLength += someObject.recordLength;
                    }
                    else if (someObject.GetType() == typeof(ObjectString))
                    {
                        stringCount++;
                        stringLength += someObject.recordLength;
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.AppendLine(string.Format("Total Objects: {0}", SerialObjectsFound.Count));
            sb.AppendLine(string.Format("Total Top-Level Objects: {0}", classCount + arrayCount + stringCount));
            sb.AppendLine(string.Format("Total Top-Level Length: {0}", classLength + arrayLength + stringLength));
            sb.AppendLine();
            sb.AppendLine(string.Format("Top-Level Class Count: {0}", classCount));
            sb.AppendLine(string.Format("Top-Level Class Length: {0}", classLength));
            sb.AppendLine();
            sb.AppendLine(string.Format("Top-Level Array Count: {0}", arrayCount));
            sb.AppendLine(string.Format("Top-Level Array Length: {0}", arrayLength));
            sb.AppendLine();
            sb.AppendLine(string.Format("Top-Level String Count: {0}", stringCount));
            sb.AppendLine(string.Format("Top-Level String Length: {0}", stringLength));
            sb.AppendLine();
            sb.AppendLine("Top-Level Object Counts by Name:");
            foreach (string ClassName in ObjectCounts.Keys)
            {
                sb.AppendLine(string.Format("{0}: {1}", ClassName, ObjectCounts[ClassName]));
            }
            sb.AppendLine();
            sb.AppendLine("Top-Level Object Lengths by Name:");
            foreach (string ClassName in ObjectLengths.Keys)
            {
                sb.AppendLine(string.Format("{0}: {1}", ClassName, ObjectLengths[ClassName]));
            }

            return sb.ToString();
        }

        internal int? ParseRecord(SerialObject parentObject)
        {
            int? serialObjectReferenceID = null;
            if (PendingNullCounter == 0)
            {
                long startPosition = reader.BaseStream.Position;
                SerialObject si = null;
                ClassInfo ci = null, ttccii=null;
                RecordTypeEnumeration nextRecordType = (RecordTypeEnumeration)reader.ReadByte();

                Log("============= " + Enum.GetName(typeof(RecordTypeEnumeration), nextRecordType) + " =================");
                switch (nextRecordType)
                {
                    case RecordTypeEnumeration.SerializedStreamHeader:
                        Log(new SerializationHeaderRecord(this));
                        // reader.ReadBytes(16);
                        break;
                    case RecordTypeEnumeration.ClassWithID:
                        //just two ints, read directly
                        ci = new ClassInfo();
                        ci.ObjectID = reader.ReadInt32();
                        LogInternal("ObjectID=" + ci.ObjectID);
                        ci.ReferencedObject = reader.ReadInt32();
                        LogInternal("ReferencedID=" + ci.ReferencedObject);

                        //Use the referenced object definition for data retrieval rules
                        // -> this will overwrite the original values in the referenced object, but who cares - the values are trash anyway (for now).
                        ttccii = (ClassInfo)SerialObjectsFound[ci.ReferencedObject.Value];
                        ttccii.ReadValueInfo(this);
                        si = ci;
                        break;
                    case RecordTypeEnumeration.SystemClassWithMembers:
                        //single structure, read in constructor
                        si = new ClassInfo(this);
                        //also values.
                        si.ReadValueInfo(this);
                        break;
                    case RecordTypeEnumeration.ClassWithMembers:
                        //single structure, read in constructor
                        si = new ClassInfo(this);
                        //also library ID, read into place.
                        ((ClassInfo)si).LibraryID = reader.ReadInt32();
                        LogInternal("LibraryID=" + ((ClassInfo)si).LibraryID);
                        //also values.
                        si.ReadValueInfo(this);
                        break;
                    case RecordTypeEnumeration.SystemClassWithMembersAndTypes:
                        //single structure, read in constructor
                        si = new ClassInfo(this);
                        //also member type info, read into place.
                        ((ClassInfo)si).ReadTypeInfo(this);
                        //also values.
                        si.ReadValueInfo(this);
                        break;
                    case RecordTypeEnumeration.ClassWithMembersAndTypes:
                        //single structure, read in constructor
                        si = new ClassInfo(this);
                        //also member type info, read into place.
                        ((ClassInfo)si).ReadTypeInfo(this);
                        //also library ID, read into place.
                        ((ClassInfo)si).LibraryID = reader.ReadInt32();
                        LogInternal("LibraryID=" + ((ClassInfo)si).LibraryID);
                        //also values.
                        si.ReadValueInfo(this);
                        break;
                    case RecordTypeEnumeration.BinaryObjectString:
                        //simple structure, just an ID and a string
                        si = new ObjectString();
                        si.ObjectID = reader.ReadInt32();
                        LogInternal("ObjectID=" + si.ObjectID);
                        ((ObjectString)si).String = reader.ReadString();
                        LogInternal("String=" + ((ObjectString)si).String);
                        break;
                    case RecordTypeEnumeration.BinaryArray:
                        //complex process, read in constructor.
                        si = new BinaryArray(this);
                        //also values.
                        si.ReadValueInfo(this);
                        break;
                    case RecordTypeEnumeration.MemberPrimitiveTyped:
                        MessagePrimitiveTyped msg = new MessagePrimitiveTyped(this);
                        msg.ReadValueInfo(this);
                        break;

                    case RecordTypeEnumeration.MemberReference:
                        //just return the ID that was referenced.
                        serialObjectReferenceID = reader.ReadInt32();
                        Log("::::> Reference=" + serialObjectReferenceID);
                        break;
                    case RecordTypeEnumeration.ObjectNull:
                        //a single null; do nothing, as null is the default return value.
                        Log("::::> NULL VALUE");
                        break;
                    case RecordTypeEnumeration.MessageEnd:
                        //do nothing, quit. Wasn't that fun?
                        endRecordReached = true;
                        Log(":::::> End-of-message");
                        break;
                    case RecordTypeEnumeration.BinaryLibrary:
                        int newLibraryID = reader.ReadInt32();
                        LibrariesFound.Add(newLibraryID, new BinaryLibrary());
                        LibrariesFound[newLibraryID].LibraryID = newLibraryID;
                        LibrariesFound[newLibraryID].Name = reader.ReadString();
                        LibrariesFound[newLibraryID].recordLength = reader.BaseStream.Position - startPosition;
                        Log("\t" + LibrariesFound[newLibraryID].ToString());
                        break;
                    case RecordTypeEnumeration.ObjectNullMultiple256:
                        //a sequence of nulls; return null, and start a counter to continue returning N nulls over the next calls.
                        PendingNullCounter = reader.ReadByte() - 1;
                        Log("\tPending Null256 Counter=" + (PendingNullCounter + 1));
                        break;

                    case RecordTypeEnumeration.ObjectNullMultiple:
                        //a sequence of nulls; return null, and start a counter to continue returning N nulls over the next calls.
                        PendingNullCounter = reader.ReadInt32() - 1;
                        Log("\tPending Null Counter=" + (PendingNullCounter + 1));
#if (DEBUG)
                        //not yet tested: if it happens, take a look around.
                        System.Diagnostics.Debugger.Break();
#endif
                        break;
                    case RecordTypeEnumeration.ArraySinglePrimitive:
                        //This one's pretty easy to build, do locally.
                        si = new BinaryArray();
                        si.ObjectID = reader.ReadInt32();
                        LogInternal("ObjectID=" + si.ObjectID);
                        ((BinaryArray)si).ArrayType = BinaryArrayTypeEnumeration.Single;
                        LogInternal("ArrayType=" + Enum.GetName(typeof(BinaryArrayTypeEnumeration), ((BinaryArray)si).ArrayType));
                        ((BinaryArray)si).BinaryType = BinaryTypeEnumeration.Primitive;
                        LogInternal("BinaryType=" + Enum.GetName(typeof(BinaryTypeEnumeration), ((BinaryArray)si).BinaryType));
                        ((BinaryArray)si).Rank = 1;
                        LogInternal("Rank=" + ((BinaryArray)si).Rank);
                        ((BinaryArray)si).Lengths = new List<int>();
                        ((BinaryArray)si).Lengths.Add(reader.ReadInt32());
                        LogInternal("Lenghts=" + ToString(((BinaryArray)si).Lengths));
                        ((BinaryArray)si).PrimitiveType = (PrimitiveTypeEnumeration)reader.ReadByte();
                        LogInternal("PrimitiveType=" + Enum.GetName(typeof(PrimitiveTypeEnumeration), ((BinaryArray)si).PrimitiveType));
                        //and then read the values.
                        si.ReadValueInfo(this);
                        break;
                    case RecordTypeEnumeration.ArraySingleObject:
                        //This should be pretty easy to build, do locally.
                        si = new BinaryArray();
                        si.ObjectID = reader.ReadInt32();
                        LogInternal("ObjectID=" + si.ObjectID);
                        ((BinaryArray)si).ArrayType = BinaryArrayTypeEnumeration.Single;
                        LogInternal("ArrayType=" + Enum.GetName(typeof(BinaryArrayTypeEnumeration), ((BinaryArray)si).ArrayType));
                        ((BinaryArray)si).BinaryType = BinaryTypeEnumeration.Object;
                        LogInternal("BinaryType=" + Enum.GetName(typeof(BinaryTypeEnumeration), ((BinaryArray)si).BinaryType));
                        ((BinaryArray)si).Rank = 1;
                        LogInternal("Rank=" + ((BinaryArray)si).Rank);
                        ((BinaryArray)si).Lengths = new List<int>();
                        ((BinaryArray)si).Lengths.Add(reader.ReadInt32());
                        LogInternal("Lenghts=" + ToString(((BinaryArray)si).Lengths));
                        //and then read the values.
                        si.ReadValueInfo(this);
#if (DEBUG)
                        //not yet tested: if it happens, take a look around.
                        System.Diagnostics.Debugger.Break();
#endif
                        break;
                    case RecordTypeEnumeration.ArraySingleString:
                        //This should be pretty easy to build, do locally.
                        si = new BinaryArray();
                        si.ObjectID = reader.ReadInt32();
                        LogInternal("ObjectID=" + si.ObjectID);
                        ((BinaryArray)si).ArrayType = BinaryArrayTypeEnumeration.Single;
                        LogInternal("ArrayType=" + Enum.GetName(typeof(BinaryArrayTypeEnumeration), ((BinaryArray)si).ArrayType));
                        ((BinaryArray)si).BinaryType = BinaryTypeEnumeration.String;
                        LogInternal("BinaryType=" + Enum.GetName(typeof(BinaryTypeEnumeration), ((BinaryArray)si).BinaryType));
                        ((BinaryArray)si).Rank = 1;
                        LogInternal("Rank=" + ((BinaryArray)si).Rank);
                        ((BinaryArray)si).Lengths = new List<int>();
                        ((BinaryArray)si).Lengths.Add(reader.ReadInt32());
                        LogInternal("Lenghts=" + ToString(((BinaryArray)si).Lengths));
                        //and then read the values.
                        si.ReadValueInfo(this);
#if (DEBUG)
                        //not yet tested: if it happens, take a look around.
                        System.Diagnostics.Debugger.Break();
#endif
                        break;
                    case RecordTypeEnumeration.MethodCall:
                        //messages/remoting functionality not implemented
                        throw new NotImplementedException("Method Call N/A");

                    case RecordTypeEnumeration.MethodReturn:
                        //messages/remoting functionality not implemented
                        throw new NotImplementedException("Method Return N/A");

                    default:
                        throw new Exception("Parsing appears to have failed dramatically. Unknown record type, we must be lost in the bytestream!");
                }

                //standard: if this was a serial object, add to list and record its length.
                if (si != null)
                {
                    Log(si);
                    SerialObjectsFound.Add(si.ObjectID, si);
                    SerialObjectsFound[si.ObjectID].recordLength = reader.BaseStream.Position - startPosition;
                    if (parentObject != null)
                        SerialObjectsFound[si.ObjectID].ParentObjectID = parentObject.ObjectID;
                    return si.ObjectID;
                }
            }
            else
            {
                PendingNullCounter--;
            }
            return serialObjectReferenceID;
        }
    }

    public class BinaryLibrary
    {
        public int LibraryID;
        public string Name;
        public long recordLength;

        public override string ToString()
        {
            return base.ToString()
                 + ";LibId=" + LibraryID
                 + ";Name=" + Name
                 ;
        }
    }

    public interface SerialObject
    {
        int ObjectID { get; set; }
        long? ParentObjectID { get; set; }
        long recordLength { get; set; }
        void ReadValueInfo(BinarySerializationStreamAnalyzer analyzer);
    }

    public interface TypeHoldingThing
    {
        SerialObject RelevantObject { get; set; }
        BinaryTypeEnumeration? BinaryType { get; set; }
        PrimitiveTypeEnumeration? PrimitiveType { get; set; }
        ClassTypeInfo TypeInfo { get; set; }
    }

    internal interface ValueHoldingThing
    {
        object Value { get; set; }
        object ValueRefID { get; set; }
    }

    public class MessagePrimitiveTyped : Logger, ValueHoldingThing, TypeHoldingThing
    {
        public SerialObject RelevantObject { get; set; }
        public BinaryTypeEnumeration? BinaryType { get { return BinaryTypeEnumeration.Primitive; } set { throw new NotImplementedException("Unexpected calls"); } }
        public PrimitiveTypeEnumeration? PrimitiveType { get; set; }
        public ClassTypeInfo TypeInfo { get; set; }
        public object Value { get; set; }
        public object ValueRefID { get; set; }
        
        internal MessagePrimitiveTyped() { }
        internal MessagePrimitiveTyped(BinarySerializationStreamAnalyzer analyzer)
        {
            PrimitiveType = (PrimitiveTypeEnumeration)analyzer.reader.ReadByte();
            LogInternal("PrimitiveType=" + Enum.GetName(typeof(PrimitiveTypeEnumeration), PrimitiveType));
        }

        public void ReadValueInfo(BinarySerializationStreamAnalyzer analyzer)
        {
            TypeHelper.GetTypeValue(this, this, analyzer);
        }

        public override string ToString()
        {
            return base.ToString()
                 + ";type=" + Enum.GetName(typeof(PrimitiveTypeEnumeration), PrimitiveType)
                 + ";value=" + Value
                 ;
        }
    }

    public class SerializationHeaderRecord : SerialObject
    {
        internal SerializationHeaderRecord() { }

        internal SerializationHeaderRecord(BinarySerializationStreamAnalyzer analyzer)
        {
            ObjectID = analyzer.reader.ReadInt32();
            HeaderID = analyzer.reader.ReadInt32();
            MajorVersion = analyzer.reader.ReadInt32();
            MinorVersion = analyzer.reader.ReadInt32();
            recordLength = 16;
        }

        public int ObjectID { get; set; }
        public int HeaderID { get; set; }
        public int MajorVersion { get; set; }
        public int MinorVersion { get; set; }
        public long? ParentObjectID { get; set; }
        public long recordLength { get; set; }
        public void ReadValueInfo(BinarySerializationStreamAnalyzer analyzer)
        {
            throw new NotImplementedException("Unexpected call");
        }

        public override string ToString()
        {
            return base.ToString()
                + ";RootID=" + ObjectID
                + ";HeaderID=" + HeaderID
                + ";Version=" + MajorVersion + "." + MinorVersion
                ;
        }
    }

    public class ClassInfo : Logger, SerialObject
    {
        internal ClassInfo() { }

        internal ClassInfo(BinarySerializationStreamAnalyzer analyzer)
        {
            ObjectID = analyzer.reader.ReadInt32();
            LogInternal("ObjectID=" + ObjectID);

            Name = analyzer.reader.ReadString();
            LogInternal("Name=" + Name);

            int numMembers = analyzer.reader.ReadInt32();
            LogInternal("# Members=" + numMembers);

            Members = new List<MemberInfo>(numMembers);
            List<String> names = new List<String>(numMembers);
            for (int i = 0; i < numMembers; i++)
            {
                MemberInfo info = new MemberInfo();
                info.Name = analyzer.reader.ReadString();
                info.RelevantObject = this;
                Members.Add(info);
                names.Add(info.Name);
            }

            LogInternal("Members=" + ToString(names));
        }

        internal void ReadTypeInfo(BinarySerializationStreamAnalyzer analyzer)
        {
            //first get binary types
            foreach (MemberInfo member in Members)
            {
                member.BinaryType = (BinaryTypeEnumeration)analyzer.reader.ReadByte();
                LogInternal(member.Name + " Type=" + Enum.GetName(typeof(BinaryTypeEnumeration), member.BinaryType));
            }

            //then get additional infos where appropriate
            foreach (MemberInfo member in Members)
            {
                LogInternal(" ############# GetTypeAdditionalInfo(" + member.Name + ") ################# ");
                TypeHelper.GetTypeAdditionalInfo(member, analyzer);
            }
        }

        public void ReadValueInfo(BinarySerializationStreamAnalyzer analyzer)
        {
            //then get additional infos where appropriate
            foreach (MemberInfo member in Members)
            {
                LogInternal(" ############# GetTypeValue(" + member.Name + ") ################# ");
                TypeHelper.GetTypeValue(member, member, analyzer);
            }
        }

        public int ObjectID { get; set; }
        public long? ParentObjectID { get; set; }
        public int? LibraryID;
        public int? ReferencedObject;
        public string Name;
        public List<MemberInfo> Members;
        public int ReferenceCount;

        public long recordLength { get; set; }

        public override string ToString()
        {
            return base.ToString()
                + ";class=" + Name + "@" + ObjectID
                + ";LibId=" + LibraryID
                + ";Members=" + Logger.ToString(Members)
                ;
        }
    }

    public class MemberInfo : TypeHoldingThing, ValueHoldingThing
    {
        public string Name;
        public SerialObject RelevantObject { get; set; }
        public BinaryTypeEnumeration? BinaryType { get; set; }
        public PrimitiveTypeEnumeration? PrimitiveType { get; set; }
        public ClassTypeInfo TypeInfo { get; set; }
        public object Value { get; set; }
        public object ValueRefID { get; set; }

        public override string ToString()
        {
            return "Name=" + Name
                + ";BinaryType=" + ((BinaryType == null) ? "???" : Enum.GetName(typeof(BinaryTypeEnumeration), BinaryType))
                + ";PrimitiveType=" + ((PrimitiveType == null) ? "???" : Enum.GetName(typeof(PrimitiveTypeEnumeration), PrimitiveType))
                + ";TypeInfo=" + TypeInfo
                + ";Value=" + Value
                + ";Ref=" + ValueRefID
                ;
        }
    }

    public class ClassTypeInfo
    {
        public string TypeName;
        public int? LibraryID;

        public override string ToString()
        {
            return base.ToString()
                + ";Type=" + TypeName
                ;
        }
    }

    public class ObjectString : SerialObject
    {
        public void ReadValueInfo(BinarySerializationStreamAnalyzer analyzer)
        {
            throw new NotImplementedException("Unexpected call to read");
        }

        public int ObjectID { get; set; }
        public long? ParentObjectID { get; set; }
        public string String;
        public long recordLength { get; set; }

        public override string ToString()
        {
            return base.ToString() + ": " + ObjectID + "@" + this.String;
        }
    }

    public class BinaryArray : Logger, SerialObject, TypeHoldingThing
    {
        internal BinaryArray() { }

        internal BinaryArray(BinarySerializationStreamAnalyzer analyzer)
        {
            ObjectID = analyzer.reader.ReadInt32();
            LogInternal("ObjectID=" + ObjectID);

            ArrayType = (BinaryArrayTypeEnumeration)analyzer.reader.ReadByte();
            LogInternal("ArrayType=" + Enum.GetName(typeof(BinaryArrayTypeEnumeration), ArrayType));

            Rank = analyzer.reader.ReadInt32();
            LogInternal("Rank=" + Rank);

            Lengths = new List<int>(Rank);
            for (int i = 0; i < Rank; i++)
            {
                Lengths.Add(analyzer.reader.ReadInt32());
            }
            LogInternal("Lengths=" + ToString(Lengths));

            if (ArrayType == BinaryArrayTypeEnumeration.SingleOffset ||
                ArrayType == BinaryArrayTypeEnumeration.JaggedOffset || 
                ArrayType == BinaryArrayTypeEnumeration.RectangularOffset)
            {
                LowerBounds = new List<int>(Rank);
                for (int i = 0; i < Rank; i++)
                {
                    LowerBounds.Add(analyzer.reader.ReadInt32());
                }

                LogInternal("LowerBounds=" + ToString(LowerBounds));
            }

            BinaryType = (BinaryTypeEnumeration)analyzer.reader.ReadByte();
            LogInternal("BinaryType=" + ((BinaryType == null) ? "???" : Enum.GetName(typeof(BinaryTypeEnumeration), BinaryType)));

            TypeHelper.GetTypeAdditionalInfo(this, analyzer);
        }

        public void ReadValueInfo(BinarySerializationStreamAnalyzer analyzer)
        {
            MemberInfo junk = new MemberInfo();
            for (int i = 0; i < Slots; i++)
                TypeHelper.GetTypeValue(this, junk, analyzer);
        }

        public int ObjectID { get; set; }
        public long? ParentObjectID { get; set; }
        public SerialObject RelevantObject {
            get { return this; }
            set { throw new NotImplementedException("Not allowed to override relevant object"); }
        }
        public BinaryArrayTypeEnumeration ArrayType;
        public int Rank;
        public List<int> Lengths;
        public List<int> LowerBounds;
        public BinaryTypeEnumeration? BinaryType { get; set; }
        public PrimitiveTypeEnumeration? PrimitiveType { get; set; }
        public ClassTypeInfo TypeInfo { get; set; }

        private int Slots
        {
            get
            {
                int outValue = 1;
                foreach (int length in Lengths)
                    outValue = outValue * length;
                return outValue;
            }
        }

        public long recordLength { get; set; }

        public override string ToString()
        {
            return base.ToString()
                + ";ID=" + ObjectID
                + ";Rank=" + Rank
                + ";Lengths=" + Logger.ToString(Lengths)
                + ";LowerBounds=" + Logger.ToString(LowerBounds)
                + ";ArrayType=" + Enum.GetName(typeof(BinaryArrayTypeEnumeration), ArrayType)
                + ";BinaryType=" + ((BinaryType == null) ? "???" : Enum.GetName(typeof(BinaryTypeEnumeration), BinaryType))
                + ";PrimitiveType=" + ((PrimitiveType == null) ? "???" : Enum.GetName(typeof(PrimitiveTypeEnumeration), PrimitiveType))
                + ";TypeInfo=" + TypeInfo
                ;
        }
    }

    internal class TypeHelper : Logger
    {
        internal static void GetTypeAdditionalInfo(TypeHoldingThing typeHolder, BinarySerializationStreamAnalyzer analyzer)
        {
            MemberInfo info = typeHolder as MemberInfo;
            string varName = (info == null) ? "???" : info.Name;
            switch (typeHolder.BinaryType)
            {
                case BinaryTypeEnumeration.Primitive:
                    typeHolder.PrimitiveType = (PrimitiveTypeEnumeration)analyzer.reader.ReadByte();
                    LogInternal(varName + ": PrimitiveType=" + Enum.GetName(typeof(PrimitiveTypeEnumeration), typeHolder.PrimitiveType));
                    break;
                case BinaryTypeEnumeration.String:
                    break;
                case BinaryTypeEnumeration.Object:
                    break;
                case BinaryTypeEnumeration.SystemClass:
                    typeHolder.TypeInfo = new ClassTypeInfo();
                    typeHolder.TypeInfo.TypeName = analyzer.reader.ReadString();
                    LogInternal(varName + ": SystemClass=" + typeHolder.TypeInfo.TypeName);
                    break;
                case BinaryTypeEnumeration.Class:
                    typeHolder.TypeInfo = new ClassTypeInfo();
                    typeHolder.TypeInfo.TypeName = analyzer.reader.ReadString();
                    typeHolder.TypeInfo.LibraryID = analyzer.reader.ReadInt32();
                    LogInternal(varName + ": Class=" + typeHolder.TypeInfo.TypeName + "@" + typeHolder.TypeInfo.LibraryID);
                    break;
                case BinaryTypeEnumeration.ObjectArray:
                    break;
                case BinaryTypeEnumeration.StringArray:
                    break;
                case BinaryTypeEnumeration.PrimitiveArray:
                    typeHolder.PrimitiveType = (PrimitiveTypeEnumeration)analyzer.reader.ReadByte();
                    LogInternal(varName + ": PrimitiveArray=" + Enum.GetName(typeof(PrimitiveTypeEnumeration), typeHolder.PrimitiveType));
                    break;
            }
        }

        internal static void GetTypeValue(TypeHoldingThing typeHolder, ValueHoldingThing valueHolder, BinarySerializationStreamAnalyzer analyzer)
        {
            MemberInfo member = typeHolder as MemberInfo;
            string varName = (member == null) ? "???" : member.Name;

            switch (typeHolder.BinaryType)
            {
                case BinaryTypeEnumeration.Primitive:
                    switch (typeHolder.PrimitiveType)
                    {
                        case PrimitiveTypeEnumeration.Boolean:
                            valueHolder.Value = analyzer.reader.ReadBoolean();
                            break;
                        case PrimitiveTypeEnumeration.Byte:
                            valueHolder.Value = analyzer.reader.ReadByte();
                            break;
                        case PrimitiveTypeEnumeration.Char:
                            valueHolder.Value = analyzer.reader.ReadChar();
                            break;
                        case PrimitiveTypeEnumeration.DateTime:
                            valueHolder.Value = DateTime.FromBinary(analyzer.reader.ReadInt64());
                            break;
                        case PrimitiveTypeEnumeration.Decimal:
                            string decimalValue = analyzer.reader.ReadString();
                            valueHolder.Value = decimal.Parse(decimalValue);
                            valueHolder.Value = analyzer.reader.ReadDecimal();
                            break;
                        case PrimitiveTypeEnumeration.Double:
                            valueHolder.Value = analyzer.reader.ReadDouble();
                            break;
                        case PrimitiveTypeEnumeration.Int16:
                            valueHolder.Value = analyzer.reader.ReadInt16();
                            break;
                        case PrimitiveTypeEnumeration.Int32:
                            valueHolder.Value = analyzer.reader.ReadInt32();
                            break;
                        case PrimitiveTypeEnumeration.Int64:
                            valueHolder.Value = analyzer.reader.ReadInt64();
                            break;
                        case PrimitiveTypeEnumeration.Null:
                            valueHolder.Value = null;
                            break;
                        case PrimitiveTypeEnumeration.SByte:
                            valueHolder.Value = analyzer.reader.ReadSByte();
                            break;
                        case PrimitiveTypeEnumeration.Single:
                            valueHolder.Value = analyzer.reader.ReadSingle();
                            break;
                        case PrimitiveTypeEnumeration.String:
                            valueHolder.Value = analyzer.reader.ReadString();
                            break;
                        case PrimitiveTypeEnumeration.TimeSpan:
                            valueHolder.Value = TimeSpan.FromTicks(analyzer.reader.ReadInt64());
                            break;
                        case PrimitiveTypeEnumeration.UInt16:
                            valueHolder.Value = analyzer.reader.ReadUInt16();
                            break;
                        case PrimitiveTypeEnumeration.UInt32:
                            valueHolder.Value = analyzer.reader.ReadUInt32();
                            break;
                        case PrimitiveTypeEnumeration.UInt64:
                            valueHolder.Value = analyzer.reader.ReadUInt64();
                            break;
                    }
                    LogInternal(Enum.GetName(typeof(PrimitiveTypeEnumeration), typeHolder.PrimitiveType) + " " + varName + " value=" + valueHolder.Value);
                    break;
                case BinaryTypeEnumeration.String:
                    valueHolder.ValueRefID = analyzer.ParseRecord(typeHolder.RelevantObject);
                    break;
                case BinaryTypeEnumeration.Object:
                    valueHolder.ValueRefID = analyzer.ParseRecord(typeHolder.RelevantObject);
                    break;
                case BinaryTypeEnumeration.SystemClass:
                    valueHolder.ValueRefID = analyzer.ParseRecord(typeHolder.RelevantObject);
                    break;
                case BinaryTypeEnumeration.Class:
                    valueHolder.ValueRefID = analyzer.ParseRecord(typeHolder.RelevantObject);
                    break;
                case BinaryTypeEnumeration.ObjectArray:
                    valueHolder.ValueRefID = analyzer.ParseRecord(typeHolder.RelevantObject);
                    break;
                case BinaryTypeEnumeration.StringArray:
                    valueHolder.ValueRefID = analyzer.ParseRecord(typeHolder.RelevantObject);
                    break;
                case BinaryTypeEnumeration.PrimitiveArray:
                    valueHolder.ValueRefID = analyzer.ParseRecord(typeHolder.RelevantObject);
                    break;
            }
        }
    }


    public enum RecordTypeEnumeration
    {
        SerializedStreamHeader = 0,
        ClassWithID = 1,                    //Object,
        SystemClassWithMembers = 2,         //ObjectWithMap,
        ClassWithMembers = 3,               //ObjectWithMapAssemId,
        SystemClassWithMembersAndTypes = 4, //ObjectWithMapTyped,
        ClassWithMembersAndTypes = 5,       //ObjectWithMapTypedAssemId,
        BinaryObjectString = 6,             //ObjectString,
        BinaryArray = 7,                    //Array,
        MemberPrimitiveTyped = 8,
        MemberReference = 9,
        ObjectNull = 10,
        MessageEnd = 11,
        BinaryLibrary = 12,                 //Assembly,
        ObjectNullMultiple256 = 13,
        ObjectNullMultiple = 14,
        ArraySinglePrimitive = 15,
        ArraySingleObject = 16,
        ArraySingleString = 17,
                                            //CrossAppDomainMap,
                                            //CrossAppDomainString,
                                            //CrossAppDomainAssembly,
        MethodCall = 21,
        MethodReturn = 22
    }

    public enum BinaryTypeEnumeration
    {
        Primitive = 0,
        String = 1,
        Object = 2,
        SystemClass = 3,
        Class = 4,
        ObjectArray = 5,
        StringArray = 6,
        PrimitiveArray = 7
    }

    public enum PrimitiveTypeEnumeration
    {
        Boolean = 1,
        Byte= 2,
        Char = 3,
            //unused
        Decimal = 5,
        Double = 6, 
        Int16 = 7,
        Int32 = 8,
        Int64 = 9,
        SByte = 10,
        Single = 11,
        TimeSpan = 12,
        DateTime = 13,
        UInt16 = 14,
        UInt32 = 15,
        UInt64 = 16,
        Null = 17,
        String = 18
    }

    public enum BinaryArrayTypeEnumeration 
    {
        Single = 0,
        Jagged = 1,
        Rectangular = 2,
        SingleOffset = 3,
        JaggedOffset = 4,
        RectangularOffset = 5
    }

    public class Logger
    {
        public static string ToString<T>(ICollection<T> c)
        {
            if ((c == null) || (c.Count <= 0))
            {
                return "[]";
            }

            StringBuilder sb = new StringBuilder(4 + c.Count * 32);
            sb.Append("[ ");

            int count = 0;
            foreach (object v in c)
            {
                if (count > 0)
                    sb.Append(", ");
                sb.Append(v);
                count++;
            }

            sb.Append(" ]");
            return sb.ToString();
        }

        public static void Log(SerialObject o)
        {
            if (o != null)
                Log("\t::::[" + o.ObjectID + "] " + o.ToString());
        }

        public static void LogInternal(string msg)
        {
            Console.WriteLine("\t++++++ " + msg);
        }

        public static void Log(string msg)
        {
            Console.WriteLine(" ===> " + msg);
        }
    }
}
