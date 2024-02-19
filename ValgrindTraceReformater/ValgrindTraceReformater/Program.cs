using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ValgrindTraceReformater
{
    class Program
    {
        static void Main(string[] args)
        {
            string path = @"..\CacheMemorySimulator\traces\HelloTrace.txt";
            ulong last = 0;
            ulong lastsiz = 0;
            HashSet<ulong> jumps = new HashSet<ulong>();
            HashSet<ulong> branches = new HashSet<ulong>();
            List<Tuple<ulong, bool>> trace = new List<Tuple<ulong, bool>>();
            StreamReader sr = new StreamReader(path);
            while (!sr.EndOfStream) 
            {
                string line = sr.ReadLine().Trim();
                if (line.StartsWith("I")) 
                {
                    line = line.Substring(3);
                    string[] splits = line.Split(',');
                    ulong adr= Convert.ToUInt64(splits[0], 16);
                    ulong size= Convert.ToUInt64(splits[1]);
                    if (adr != last+lastsiz && last != 0) 
                    {
                        jumps.Add(last);
                    }
                    last = adr;
                    lastsiz= size;
                }
            }
            sr.Close();
            Console.WriteLine("Found " + jumps.Count + " jumps");
            last = 0;
            sr = new StreamReader(path);
            while (!sr.EndOfStream)
            {
                string line = sr.ReadLine().Trim();
                if (line.StartsWith("I"))
                {
                    line = line.Substring(3);
                    string[] splits = line.Split(',');
                    ulong adr = Convert.ToUInt64(splits[0], 16);
                    ulong size = Convert.ToUInt64(splits[1]);
                    if (adr == last + lastsiz && last != 0 && jumps.Contains(last))
                    {
                        branches.Add(last);
                    }
                    last = adr;
                    lastsiz = size;
                }
            }
            sr.Close();
            Console.WriteLine("Found " + branches.Count + " branches");
            last = 0;
            sr = new StreamReader(path);
            StreamWriter sw = new StreamWriter(Path.GetDirectoryName(path) + "\\ProcessedTrace.trace");
            while (!sr.EndOfStream)
            {
                string line = sr.ReadLine().Trim();
                if (line.StartsWith("I"))
                {
                    line = line.Substring(3);
                    string[] splits = line.Split(',');
                    ulong adr = Convert.ToUInt64(splits[0], 16);
                    ulong size = Convert.ToUInt64(splits[1]);
                    if (branches.Contains(last))
                    {
                        trace.Add(new Tuple<ulong, bool>(last, last + lastsiz != adr));
                        sw.WriteLine(lastsiz+" "+last.ToString("X") +" -1 -1 0 - "+((last + lastsiz != adr)?"T":"N")+" - 0 0 0 "+adr.ToString("X"));
                    }
                    last = adr;
                    lastsiz = size;
                }
            }
            sr.Close();
            sw.Close();
            Console.WriteLine("Found " + trace.Count + " branch instructions");
        }
    }
}
