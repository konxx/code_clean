using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;

internal static class CodeCleanLauncher
{
    [DllImport("kernel32.dll")]
    private static extern bool SetConsoleOutputCP(uint codePage);

    [DllImport("kernel32.dll")]
    private static extern bool SetConsoleCP(uint codePage);

    private static int Main(string[] args)
    {
        SetConsoleCP(65001);
        SetConsoleOutputCP(65001);
        Console.InputEncoding = Encoding.UTF8;
        Console.OutputEncoding = Encoding.UTF8;

        string appHome = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
        string jarPath = Path.Combine(appHome, "codeclean.jar");
        string libPath = Path.Combine(appHome, "lib", "*");

        if (!File.Exists(jarPath))
        {
            Console.Error.WriteLine("codeclean.jar not found: " + jarPath);
            return 1;
        }

        string javaExe = FindJava();
        string javaArgs = "-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
            + " -cp " + Quote(jarPath + ";" + libPath)
            + " com.source.CodeCleanCli " + QuoteArgs(args);

        try
        {
            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = javaExe;
            startInfo.Arguments = javaArgs;
            startInfo.UseShellExecute = false;
            Process process = Process.Start(startInfo);
            process.WaitForExit();
            return process.ExitCode;
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("failed to start Java runtime: " + ex.Message);
            Console.Error.WriteLine("please install Java or set JAVA_HOME.");
            return 1;
        }
    }

    private static string FindJava()
    {
        string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrWhiteSpace(javaHome))
        {
            string javaFromHome = Path.Combine(javaHome, "bin", "java.exe");
            if (File.Exists(javaFromHome))
            {
                return javaFromHome;
            }
        }

        return "java.exe";
    }

    private static string QuoteArgs(string[] args)
    {
        StringBuilder builder = new StringBuilder();
        foreach (string arg in args)
        {
            if (builder.Length > 0)
            {
                builder.Append(' ');
            }

            builder.Append(Quote(arg));
        }

        return builder.ToString();
    }

    private static string Quote(string value)
    {
        if (value == null)
        {
            return "\"\"";
        }

        return "\"" + value.Replace("\"", "\\\"") + "\"";
    }
}
