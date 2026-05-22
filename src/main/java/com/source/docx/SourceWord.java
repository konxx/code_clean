package com.source.docx;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.Font;
import com.aspose.words.LayoutCollector;
import com.aspose.words.LayoutEnumerator;
import com.aspose.words.Paragraph;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourceWord {
   private static final Logger logger = LoggerFactory.getLogger(SourceWord.class);
   private String version = "V1.0";
   private List<String> fileTypes = (List)Stream.of(".java").collect(Collectors.toList());
   private String allDocumentName = "前后30页.docx";
   private String docName = "all.docx";
   private String name = "软件";
   private int sourceLength = 1700;
   private boolean showDialogs = true;
   private File outputDir = FileSystemView.getFileSystemView().getHomeDirectory();
   JOptionPane optionGanPane = new JOptionPane("正在生成中，请稍等", 1, -1, (Icon)null, new Object[0], (Object)null);

   public static InputStream getFileResource(String name) throws IOException, BadLocationException {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      return loader.getResourceAsStream(name);
   }

   public static byte[] getFileResourceByte(String name) throws IOException {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      InputStream is = loader.getResourceAsStream(name);

      byte[] var7;
      try {
         ByteArrayOutputStream bass = new ByteArrayOutputStream();

         try {
            if (is == null) {
               throw new IOException("Resource not found: " + name);
            }

            int len = 1024;
            byte[] tmp = new byte[len];

            int i;
            while((i = is.read(tmp, 0, len)) > 0) {
               bass.write(tmp, 0, i);
            }

            var7 = bass.toByteArray();
         } catch (Throwable var10) {
            try {
               bass.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         bass.close();
      } catch (Throwable var11) {
         if (is != null) {
            try {
               is.close();
            } catch (Throwable var8) {
               var11.addSuppressed(var8);
            }
         }

         throw var11;
      }

      if (is != null) {
         is.close();
      }

      return var7;
   }

   public static byte[] getResource(String url, String name) throws BadLocationException, IOException {
      OkHttpClient client = (new OkHttpClient.Builder()).readTimeout(3L, TimeUnit.SECONDS).build();

      try {
         Request request = (new Request.Builder()).url(url).build();
         Response response = client.newCall(request).execute();
         return response.isSuccessful() && response.body() != null ? response.body().bytes() : getFileResourceByte(name);
      } catch (IOException var5) {
         return getFileResourceByte(name);
      }
   }

   public void start(String name, String version, final String sourceDir, List<String> fileType, int sourceLong, final String key, final boolean random) throws Throwable {
      this.configure(name, version, fileType, sourceLong);
      this.showDialogs = true;
      this.outputDir = FileSystemView.getFileSystemView().getHomeDirectory();
      final JDialog dialog = this.optionGanPane.createDialog("提示");
      dialog.setLocationRelativeTo((Component)null);
      dialog.pack();
      SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
         protected Void doInBackground() throws Exception {
            SourceWord.logger.info("开始生成文档任务");
            dialog.setVisible(true);
            SourceWord.logger.info("扫描目录: {}", sourceDir);
            List<File> fileList = SourceWord.this.collectSourceFiles(sourceDir);
            System.out.println("共 " + fileList.size() + " 个文件");
            SourceWord.this.optionGanPane.setMessage("共 " + fileList.size() + " 个文件，请稍等");
            SourceWord.this.generateFromFileList(fileList, key, random);

            return null;
         }

         protected void done() {
            dialog.dispose();

            try {
               this.get();
               SourceWord.logger.info("文档生成任务成功完成");
               SourceWord.this.showPopup();
            } catch (Exception e) {
               SourceWord.logger.error("文档生成失败", e);
               e.printStackTrace();
               String errorMsg = e.getMessage();
               Throwable cause = e.getCause();
               if (cause != null) {
                  String var10000 = cause.getClass().getName();
                  errorMsg = var10000 + ": " + cause.getMessage();
               }

               JOptionPane.showMessageDialog((Component)null, "程序出错：" + errorMsg + "\n\n请查看控制台输出或提交软件目录logs/error.log错误报告", "错误", 0);
            }

         }
      };
      worker.execute();
      dialog.setVisible(true);
   }

   public String startCli(String name, String version, String sourceDir, List<String> fileType, int sourceLong, String key, boolean random) throws Exception {
      this.configure(name, version, fileType, sourceLong);
      this.showDialogs = false;
      this.outputDir = (new File(System.getProperty("user.dir"))).getAbsoluteFile();
      logger.info("开始执行命令行生成任务");
      logger.info("扫描目录: {}", sourceDir);
      List<File> fileList = this.collectSourceFiles(sourceDir);
      System.out.println("扫描目录: " + sourceDir);
      System.out.println("共 " + fileList.size() + " 个文件");
      String outputFilePath = this.generateFromFileList(fileList, key, random);
      System.out.println("生成完成: " + outputFilePath);
      logger.info("命令行生成任务完成: {}", outputFilePath);
      return outputFilePath;
   }

   private void configure(String name, String version, List<String> fileType, int sourceLong) {
      this.version = version;
      this.fileTypes = fileType;
      this.sourceLength = sourceLong;
      this.allDocumentName = name + "-代码(前后30页).docx";
      this.docName = name + "-代码(全量备份).docx";
      this.name = name;
   }

   private List<File> collectSourceFiles(String sourceDir) {
      List<File> fileList = FileUtil.loopFiles(sourceDir, this.createSourceFileFilter());
      logger.info("共找到 {} 个文件", fileList.size());
      return fileList;
   }

   private FileFilter createSourceFileFilter() {
      return new FileFilter() {
         public boolean accept(File pathname) {
            String[] ignoredDirs = new String[]{"node_modules", "dist", "build", ".next", ".nuxt", ".output", "bower_components", ".cache", ".parcel-cache", ".vite", "venv", ".venv", "env", ".env", "ENV", "virtualenv", "__pycache__", ".pytest_cache", ".mypy_cache", ".tox", "pip-wheel-metadata", ".eggs", "*.egg-info", "target", "build", "out", ".gradle", ".mvn", "bin", "obj", "Debug", "Release", "packages", "vendor/bundle", ".bundle", "tmp/cache", "vendor", "pkg/mod", "target/debug", "target/release", "vendor", "composer.phar", "Pods", "Carthage", "DerivedData", ".build", ".gradle", ".externalNativeBuild", ".cxx", ".dart_tool", ".flutter-plugins", ".flutter-plugins-dependencies", ".git", ".svn", ".hg", ".bzr", ".idea", ".vscode", ".vs", ".eclipse", ".settings", "*.xcworkspace", "*.xcodeproj", "coverage", ".nyc_output", "htmlcov", ".coverage", "test-results", "junit", "allure-results", "logs", "*.log", "tmp", "temp", ".tmp", ".sass-cache", ".turbo", "public/uploads", "storage", "var/cache", "var/log"};
            String path = pathname.getAbsolutePath();

            for(String ignoredDir : ignoredDirs) {
               if (path.contains(File.separator + ignoredDir + File.separator) || path.endsWith(File.separator + ignoredDir)) {
                  SourceWord.logger.debug("忽略目录: {}", pathname.getAbsolutePath());
                  return false;
               }
            }

            if (pathname.isDirectory()) {
               return true;
            } else {
               for(String fileType : SourceWord.this.fileTypes) {
                  if (pathname.getName().endsWith("." + fileType)) {
                     return true;
                  }
               }

               return false;
            }
         }
      };
   }

   private String generateFromFileList(List<File> fileList, String key, boolean random) throws Exception {
      if (random) {
         Collections.shuffle(fileList);
         logger.info("已打乱文件顺序");
      } else {
         fileList.sort(Comparator.comparing(File::getAbsolutePath));
         logger.info("已按路径排序");
      }

      String outputFilePath;
      if (key.equals("937599")) {
         logger.info("开始生成前后30页文档: {}", this.allDocumentName);
         this.generatePreciseDocument(fileList, this.allDocumentName);
         logger.info("前后30页文档生成完成");
         outputFilePath = this.getDesktopOutputPath(this.allDocumentName);
      } else {
         logger.info("开始生成全量备份文档: {}", this.docName);
         this.scanAndGenerateSourceDocAll(fileList, this.docName);
         logger.info("全量备份文档生成完成");
         outputFilePath = this.getDesktopOutputPath(this.docName);
      }

      return outputFilePath;
   }

   private String getDesktopOutputPath(String fileName) {
      return (new File(this.outputDir, fileName)).getAbsolutePath();
   }

   private void showPopup() {
      SwingUtilities.invokeLater(() -> {
         File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
         String desktopPath = desktopDir.getAbsolutePath();
         JOptionPane optionPane = new JOptionPane("已生成在" + desktopPath + "，建议关注公众号获取软件最新版（更多功能）", 1, -1, (Icon)null, new Object[0], (Object)null);
         JButton okButton = new JButton("确定");
         okButton.setEnabled(false);
         optionPane.setOptions(new Object[]{okButton});
         Timer timer = new Timer(3000, (e) -> {
            okButton.setEnabled(true);
            okButton.addActionListener((evt) -> {
               JDialog dialog = (JDialog)SwingUtilities.getWindowAncestor(okButton);
               dialog.dispose();
            });
         });
         timer.setRepeats(false);
         timer.start();
         JDialog dialog = optionPane.createDialog("提示");
         dialog.pack();
         dialog.setVisible(true);
      });
   }

   public void generatePreciseDocument(List<File> files, String outputFilePath) throws Exception {
      Document doc = new Document(getFileResource("text.docx"));
      DocumentBuilder builder = new DocumentBuilder(doc);
      Font font = builder.getFont();
      font.setName("Arial Narrow");
      doc.updatePageLayout();
      int currentPageCount = doc.getPageCount();

      for(File file : files) {
         long fileSizeInBytes = file.length();
         long fileSizeInKB = fileSizeInBytes / 1024L;
         long maxFileSizeKB = 200L;
         if (fileSizeInKB > maxFileSizeKB) {
            logger.info("文件 {} 大小为 {}KB，超过 {}KB 限制，跳过该文件", new Object[]{file.getName(), fileSizeInKB, maxFileSizeKB});
            PrintStream var10000 = System.out;
            String var10001 = file.getName();
            var10000.println("跳过大文件: " + var10001 + " (" + fileSizeInKB + "KB)");
         } else {
            String encoding = this.detectEncoding(file);
            logger.info("处理文件: {}, 使用编码: {}", file.getAbsolutePath(), encoding);

            try {
               this.readFileWithEncoding(file, encoding, builder);
            } catch (Exception e) {
               logger.error("读取文件 {} 失败，尝试使用备用编码", file.getName(), e);
               if (!this.tryAlternativeEncodings(file, builder, encoding)) {
                  logger.error("文件 {} 无法使用任何编码正确读取，跳过该文件", file.getName());
                  continue;
               }
            }

            doc.updatePageLayout();
            currentPageCount = doc.getPageCount();
            System.out.println("当前页数: " + currentPageCount);
            if (this.showDialogs) {
               this.optionGanPane.setMessage("当前页数: " + currentPageCount + "，请稍等");
            }

            if (currentPageCount >= 60) {
               break;
            }
         }
      }

      if (currentPageCount > 60) {
         System.out.println("文档页数超过60页，开始从第30页删除多余内容...");
         this.deleteFromPage(doc, 30, currentPageCount, 60);
      }

      DocumentBuilder builderAfterInsert = new DocumentBuilder(doc);
      builderAfterInsert.moveToHeaderFooter(4);
      builderAfterInsert.write(this.name + " " + this.version);
      builderAfterInsert.moveToHeaderFooter(1);
      builderAfterInsert.write(this.name + " " + this.version);
      if (currentPageCount < 60) {
         System.out.println("文件总页数少于60页，保存当前文档！");
      } else {
         System.out.println("文件已达到60页，保存文档！");
      }

      doc.save(this.getDesktopOutputPath(outputFilePath));
   }

   private void deleteFromPage(Document doc, int startPage, int currentPageCount, int targetPageCount) throws Exception {
      LayoutCollector layoutCollector = new LayoutCollector(doc);
      new LayoutEnumerator(doc);
      int pagesToDelete = currentPageCount - targetPageCount;
      if (pagesToDelete > 0) {
         for(Object node : doc.getChildNodes(8, true)) {
            Paragraph para = (Paragraph)node;
            int pageNumberOfPara = layoutCollector.getStartPageIndex(para);
            if (pageNumberOfPara >= startPage && pageNumberOfPara < startPage + pagesToDelete) {
               para.remove();
            }
         }

         doc.updatePageLayout();
      }
   }

   public void scanAndGenerateSourceDocAll(List<File> fileList, String Doc) throws Exception {
      Document doc = new Document(getFileResource("text.docx"));
      DocumentBuilder builder = new DocumentBuilder(doc);
      Font font = builder.getFont();
      font.setName("Arial Narrow");
      int lineCount = 0;

      for(File file : fileList) {
         String encoding = this.detectEncoding(file);
         logger.info("处理文件: {}, 使用编码: {}", file.getAbsolutePath(), encoding);

         try {
            lineCount = this.readFileWithEncodingAndCount(file, encoding, builder, lineCount);
         } catch (Exception e) {
            logger.error("读取文件 {} 失败，尝试使用备用编码", file.getName(), e);

            try {
               lineCount = this.tryAlternativeEncodingsWithCount(file, builder, encoding, lineCount);
            } catch (Exception var12) {
               logger.error("文件 {} 无法使用任何编码正确读取，跳过该文件", file.getName());
            }
         }
      }

      builder.moveToHeaderFooter(4);
      builder.write(this.name + " " + this.version);
      builder.moveToHeaderFooter(1);
      builder.write(this.name + " " + this.version);
      int pageCount = doc.getPageCount();
      doc.save(this.getDesktopOutputPath(Doc));
      if (pageCount < 60 && this.showDialogs) {
         JOptionPane.showMessageDialog((Component)null, "源码不足60页，请检查是否填写错误");
      } else if (pageCount < 60) {
         System.out.println("源码不足60页，请检查是否填写错误");
      }

   }

   public boolean isSourceLine(String lineText) {
      if (StrUtil.isEmpty(lineText)) {
         return false;
      } else {
         return !StrUtil.startWithAny(lineText, new CharSequence[]{"//"});
      }
   }

   private void readFileWithEncoding(File file, String encoding, DocumentBuilder builder) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

      String line;
      try {
         while((line = reader.readLine()) != null) {
            if (this.isSourceLine(StrUtil.trimToEmpty(line))) {
               builder.writeln(line);
            }
         }
      } catch (Throwable var8) {
         try {
            reader.close();
         } catch (Throwable var7) {
            var8.addSuppressed(var7);
         }

         throw var8;
      }

      reader.close();
   }

   private int readFileWithEncodingAndCount(File file, String encoding, DocumentBuilder builder, int lineCount) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

      String line;
      try {
         while((line = reader.readLine()) != null) {
            String trimLine = StrUtil.trimToEmpty(line);
            if (this.isSourceLine(trimLine)) {
               builder.writeln(line);
               ++lineCount;
               if (lineCount % 5000 == 0) {
                  System.gc();
               }
            }
         }
      } catch (Throwable var9) {
         try {
            reader.close();
         } catch (Throwable var8) {
            var9.addSuppressed(var8);
         }

         throw var9;
      }

      reader.close();
      return lineCount;
   }

   private boolean tryAlternativeEncodings(File file, DocumentBuilder builder, String failedEncoding) {
      String[] alternativeEncodings = new String[]{"UTF-8", "GBK", "GB18030", "ISO-8859-1", "Big5", "UTF-16"};

      for(String encoding : alternativeEncodings) {
         if (!encoding.equals(failedEncoding)) {
            try {
               logger.info("尝试使用备用编码 {} 读取文件 {}", encoding, file.getName());
               this.readFileWithEncoding(file, encoding, builder);
               logger.info("成功使用编码 {} 读取文件 {}", encoding, file.getName());
               return true;
            } catch (Exception var10) {
               logger.debug("编码 {} 读取文件 {} 失败", encoding, file.getName());
            }
         }
      }

      return false;
   }

   private int tryAlternativeEncodingsWithCount(File file, DocumentBuilder builder, String failedEncoding, int lineCount) throws IOException {
      String[] alternativeEncodings = new String[]{"UTF-8", "GBK", "GB18030", "ISO-8859-1", "Big5", "UTF-16"};

      for(String encoding : alternativeEncodings) {
         if (!encoding.equals(failedEncoding)) {
            try {
               logger.info("尝试使用备用编码 {} 读取文件 {}", encoding, file.getName());
               lineCount = this.readFileWithEncodingAndCount(file, encoding, builder, lineCount);
               logger.info("成功使用编码 {} 读取文件 {}", encoding, file.getName());
               return lineCount;
            } catch (Exception var11) {
               logger.debug("编码 {} 读取文件 {} 失败", encoding, file.getName());
            }
         }
      }

      throw new IOException("无法使用任何编码读取文件: " + file.getName());
   }

   public String detectEncoding(File file) {
      String encoding = this.detectByUniversalDetector(file);
      if (encoding != null) {
         logger.debug("UniversalDetector检测到文件 {} 编码为: {}", file.getName(), encoding);
         return this.normalizeEncoding(encoding);
      } else {
         encoding = this.detectByBOM(file);
         if (encoding != null) {
            logger.debug("BOM检测到文件 {} 编码为: {}", file.getName(), encoding);
            return encoding;
         } else {
            encoding = this.detectByTrialAndError(file);
            if (encoding != null) {
               logger.debug("试探检测到文件 {} 编码为: {}", file.getName(), encoding);
               return encoding;
            } else {
               logger.warn("无法检测文件 {} 的编码，使用默认 UTF-8", file.getName());
               return "UTF-8";
            }
         }
      }
   }

   private String detectByUniversalDetector(File file) {
      try {
         return UniversalDetector.detectCharset(file);
      } catch (IOException e) {
         logger.warn("UniversalDetector检测文件 {} 编码失败: {}", file.getName(), e.getMessage());
         return null;
      }
   }

   private String detectByBOM(File file) {
      try {
         FileInputStream fis = new FileInputStream(file);

         label113: {
            String var13;
            label112: {
               label111: {
                  label110: {
                     label109: {
                        label108: {
                           try {
                              byte[] bom = new byte[4];
                              int n = fis.read(bom, 0, 4);
                              if (n < 2) {
                                 var13 = null;
                                 break label112;
                              }

                              if (n >= 3 && bom[0] == -17 && bom[1] == -69 && bom[2] == -65) {
                                 var13 = "UTF-8";
                                 break label111;
                              }

                              if (bom[0] == -1 && bom[1] == -2) {
                                 var13 = "UTF-16LE";
                                 break label110;
                              }

                              if (bom[0] == -2 && bom[1] == -1) {
                                 var13 = "UTF-16BE";
                                 break label109;
                              }

                              if (n >= 4 && bom[0] == -1 && bom[1] == -2 && bom[2] == 0 && bom[3] == 0) {
                                 var13 = "UTF-32LE";
                                 break label108;
                              }

                              if (n < 4 || bom[0] != 0 || bom[1] != 0 || bom[2] != -2 || bom[3] != -1) {
                                 break label113;
                              }

                              var13 = "UTF-32BE";
                           } catch (Throwable var7) {
                              try {
                                 fis.close();
                              } catch (Throwable var6) {
                                 var7.addSuppressed(var6);
                              }

                              throw var7;
                           }

                           fis.close();
                           return var13;
                        }

                        fis.close();
                        return var13;
                     }

                     fis.close();
                     return var13;
                  }

                  fis.close();
                  return var13;
               }

               fis.close();
               return var13;
            }

            fis.close();
            return var13;
         }

         fis.close();
      } catch (IOException e) {
         logger.warn("BOM检测文件 {} 失败: {}", file.getName(), e.getMessage());
      }

      return null;
   }

   private String detectByTrialAndError(File file) {
      String[] encodings = new String[]{"UTF-8", "GBK", "GB2312", "GB18030", "ISO-8859-1", "Big5", "Shift_JIS"};

      for(String encoding : encodings) {
         try {
            if (this.isValidEncoding(file, encoding)) {
               return encoding;
            }
         } catch (Exception var8) {
         }
      }

      return null;
   }

   private boolean isValidEncoding(File file, String encoding) {
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

         boolean var16;
         try {
            int validChars = 0;
            int totalChars = 0;
            int linesToCheck = 50;
            int linesChecked = 0;

            String line;
            while((line = reader.readLine()) != null && linesChecked < linesToCheck) {
               ++linesChecked;

               for(char c : line.toCharArray()) {
                  ++totalChars;
                  if (c != '�' && c != '?' && (c >= ' ' || c == '\t' || c == '\r' || c == '\n')) {
                     ++validChars;
                  }
               }
            }

            var16 = totalChars > 0 && (double)validChars * (double)100.0F / (double)totalChars >= (double)95.0F;
         } catch (Throwable var14) {
            try {
               reader.close();
            } catch (Throwable var13) {
               var14.addSuppressed(var13);
            }

            throw var14;
         }

         reader.close();
         return var16;
      } catch (Exception var15) {
         return false;
      }
   }

   private String normalizeEncoding(String encoding) {
      if (encoding == null) {
         return null;
      } else {
         switch (encoding.toUpperCase().trim()) {
            case "GB2312":
            case "GBK":
            case "GB18030":
               return "GBK";
            case "UTF8":
               return "UTF-8";
            case "UTF16":
               return "UTF-16";
            case "WINDOWS-1252":
            case "CP1252":
               return "ISO-8859-1";
            default:
               return encoding;
         }
      }
   }
}
