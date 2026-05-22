using System;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;

internal static class CodeCleanSingleFileLauncher
{
    private const string Version = "1.0.0";

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

        try
        {
            string appDir = EnsureExtracted();
            string innerExe = Path.Combine(appDir, "codeclean.exe");
            if (!File.Exists(innerExe))
            {
                Console.Error.WriteLine("embedded codeclean.exe not found after extraction: " + innerExe);
                return 1;
            }

            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = innerExe;
            startInfo.Arguments = QuoteArgs(args);
            startInfo.UseShellExecute = false;
            Process process = Process.Start(startInfo);
            process.WaitForExit();
            return process.ExitCode;
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("failed to start embedded codeclean: " + ex.Message);
            return 1;
        }
    }

    private static string EnsureExtracted()
    {
        string localAppData = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
        string baseDir = Path.Combine(localAppData, "codeclean", Version);
        string appDir = Path.Combine(baseDir, "codeclean");
        string marker = Path.Combine(baseDir, ".extracted");

        if (File.Exists(marker) && File.Exists(Path.Combine(appDir, "codeclean.exe")))
        {
            return appDir;
        }

        if (Directory.Exists(baseDir))
        {
            Directory.Delete(baseDir, true);
        }

        Directory.CreateDirectory(baseDir);
        string zipPath = Path.Combine(baseDir, "codeclean-app.zip");
        using (Stream input = Assembly.GetExecutingAssembly().GetManifestResourceStream("codeclean-app.zip"))
        {
            if (input == null)
            {
                throw new InvalidOperationException("embedded resource codeclean-app.zip not found");
            }

            using (FileStream output = File.Create(zipPath))
            {
                input.CopyTo(output);
            }
        }

        ZipFile.ExtractToDirectory(zipPath, baseDir);
        File.Delete(zipPath);
        File.WriteAllText(marker, DateTime.UtcNow.ToString("O"), Encoding.UTF8);
        return appDir;
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
