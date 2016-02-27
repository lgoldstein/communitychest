using System;
using System.Collections.Generic;
using System.Windows.Forms;
using System.IO;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;

namespace BinarySerializationAnalysis
{
    static class Program
    {
        static void Main(string[] args)
        {
            if ((args == null) || (args.Length <= 0))
            {
                AnalyzeTestClass();
                return;
            }

            foreach (string filePath in args)
            {
                Stream stream = new FileStream(filePath, FileMode.Open);
                try
                {
                    AnalyzeStream(filePath, stream);
                }
                finally
                {
                    stream.Close();
                }
            }
        }

        static void AnalyzeTestClass()
        {
            DummyClass test = new DummyClass();

            //set some properties
            test.someList = new List<int>();
            test.someList.Add(1);
            test.someList.Add(2);
            test.someList.Add(3);
            test.someString = "Some Value";

            //set up a recursive reference
            test.subObject = test;

            IFormatter formatter = new BinaryFormatter();
            using(Stream stream = new MemoryStream(8192))
            {
                formatter.Serialize(stream, test);

                //reset the stream to the start
                stream.Position = 0;

                //analyse the binary serialization stream
                AnalyzeStream(test.GetType().Name, stream);
            }
        }

        static void AnalyzeStream(string name, Stream stream)
        {
            BinarySerializationStreamAnalyzer analyzer = new BinarySerializationStreamAnalyzer();
            try
            {
                analyzer.Read(stream);
            }
            catch (Exception e)
            {
                Console.Error.WriteLine(e.GetType().Name + ": " + e.Message);
            }

            Console.WriteLine(" ================== " + name + "==============================");
            Console.Write(analyzer.Analyze());
            Console.WriteLine();
        }
    }
}
