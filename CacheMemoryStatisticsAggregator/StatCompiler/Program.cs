using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StatCompiler
{
    class Program
    {
        static void Main(string[] args)
        {
            string dir = @"..\CacheMemorySimulator\Stat";
            StreamWriter sw = new StreamWriter(dir + "\\statCompiled.txt");
            int cnt = 0;
            foreach (var file in Directory.EnumerateFiles(dir))
            {
                if (!file.EndsWith("statCompiled.txt")) 
                {
                    cnt++;
                    string[] fnames = Path.GetFileNameWithoutExtension(file).Trim().Split(' ');
                    string[] conts = File.ReadAllLines(file)[0].Trim().Split('\t');
                    sw.WriteLine(conts[0]+","+ conts[1] + "," + conts[2] + "," + conts[3] + "," + conts[4] + "," + conts[5] + "," + fnames[4] + "," + fnames[5]);
                }
            }
            sw.Close();
            Console.WriteLine("Compiled "+cnt+" stats.");
        }
    }
}
