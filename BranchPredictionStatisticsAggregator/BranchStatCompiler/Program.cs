using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace BranchStatCompiler
{
    class Program
    {
        static void Main(string[] args)
        {
            string dir = @"..\BrachPredictionSimulator\Stats";
            StreamWriter sw = new StreamWriter(dir + "\\statCompiled.txt");
            int cnt = 0;
            foreach (var file in Directory.EnumerateFiles(dir))
            {
                if (!file.EndsWith("statCompiled.txt"))
                {
                    cnt++;
                    string[] fnames = Path.GetFileNameWithoutExtension(file).Trim().Split(' ');
                    string[] conts = File.ReadAllLines(file)[3].Trim().Split(' ');
                    switch (fnames[0]) 
                    {
                        case "Bimodal": { sw.WriteLine(fnames[0] + "," + fnames[1] + "," + fnames[3] + ",0," + fnames[4] + "," + fnames[6] + "," + conts[3]); break; }
                        case "Correlation": { sw.WriteLine(fnames[0] + "," + fnames[1] + "," + fnames[2] + ",0," + fnames[3] + "," + fnames[5] + "," + conts[3]); break; }
                        case "TAGE": { sw.WriteLine(fnames[0] + "," + fnames[2] + "," + fnames[3] + "," + fnames[1] + "," + fnames[4] + "," + fnames[5] + "," + conts[3]); break; }
                        case "TwoLevel": { sw.WriteLine(fnames[0] + "," + fnames[1] + ",0,0," + fnames[2] + "," + fnames[3] + "," + conts[3]); break; }
                        case "YAGS": { sw.WriteLine(fnames[0] + "," + fnames[1] + "," + fnames[2] + ",0," + fnames[3] + "," + fnames[4] + "," + conts[3]); break; }
                    }
                }
            }
            sw.Close();
            Console.WriteLine("Compiled " + cnt + " stats.");
        }
    }
}
