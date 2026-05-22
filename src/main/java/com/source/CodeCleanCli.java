package com.source;

import com.source.docx.SourceWord;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CodeCleanCli {
   private static final String DEFAULT_VERSION = "V1.0";
   private static final String DEFAULT_FILE_TYPES = "py,sql,tsx,ts";
   private static final String DEFAULT_KEY = "937599";
   private static final int DEFAULT_SOURCE_LENGTH = 1700;

   public static void main(String[] args) {
      try {
         Options options = parseArgs(args);
         System.out.println("软件名称: " + options.name);
         System.out.println("版本号: " + options.version);
         System.out.println("项目路径: " + options.sourceDir);
         System.out.println("指定文件后缀: " + String.join(",", options.fileTypes));
         System.out.println("文件顺序: " + (options.random ? "打乱" : "不打乱"));
         new SourceWord().startCli(options.name, options.version, options.sourceDir, options.fileTypes, DEFAULT_SOURCE_LENGTH, options.key, options.random);
      } catch (IllegalArgumentException e) {
         System.err.println("参数错误: " + e.getMessage());
         printUsage();
         System.exit(2);
      } catch (Exception e) {
         System.err.println("生成失败: " + e.getMessage());
         e.printStackTrace();
         System.exit(1);
      }
   }

   private static Options parseArgs(String[] args) {
      if (args.length < 2 || args.length > 6) {
         throw new IllegalArgumentException("参数数量应为 2 到 6 个");
      }

      Options options = new Options();
      options.name = requireText(args[0], "软件名称不能为空");
      options.version = DEFAULT_VERSION;
      options.key = DEFAULT_KEY;
      options.random = true;

      int sourceIndex;
      if (isDirectory(args[1])) {
         sourceIndex = 1;
      } else {
         if (args.length < 3) {
            throw new IllegalArgumentException("项目路径不存在: " + args[1]);
         }

         options.version = defaultText(args[1], DEFAULT_VERSION);
         sourceIndex = 2;
      }

      options.sourceDir = requireDirectory(args[sourceIndex]);
      int cursor = sourceIndex + 1;
      String fileTypes = cursor < args.length ? defaultText(args[cursor++], DEFAULT_FILE_TYPES) : DEFAULT_FILE_TYPES;
      options.fileTypes = parseFileTypes(fileTypes);
      options.key = cursor < args.length ? defaultText(args[cursor++], DEFAULT_KEY) : DEFAULT_KEY;

      if (cursor < args.length) {
         options.random = parseRandom(args[cursor++]);
      }

      if (cursor < args.length) {
         throw new IllegalArgumentException("多余参数: " + args[cursor]);
      }

      return options;
   }

   private static boolean isDirectory(String value) {
      return value != null && (new File(value)).isDirectory();
   }

   private static String requireDirectory(String value) {
      String path = requireText(value, "项目路径不能为空");
      File file = new File(path);
      if (!file.isDirectory()) {
         throw new IllegalArgumentException("项目路径不存在或不是目录: " + path);
      }

      return file.getAbsolutePath();
   }

   private static String requireText(String value, String message) {
      if (value == null || value.trim().isEmpty()) {
         throw new IllegalArgumentException(message);
      }

      return value.trim();
   }

   private static String defaultText(String value, String defaultValue) {
      return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
   }

   private static List<String> parseFileTypes(String value) {
      List<String> fileTypes = Arrays.stream(value.replace('，', ',').split(","))
         .map(String::trim)
         .filter((item) -> !item.isEmpty())
         .map((item) -> item.startsWith(".") ? item.substring(1) : item)
         .collect(Collectors.toList());
      if (fileTypes.isEmpty()) {
         throw new IllegalArgumentException("指定文件后缀不能为空");
      }

      return fileTypes;
   }

   private static boolean parseRandom(String value) {
      String normalized = defaultText(value, "1");
      if ("1".equals(normalized)) {
         return true;
      } else if ("2".equals(normalized)) {
         return false;
      }

      throw new IllegalArgumentException("顺序参数只能填写 1 或 2，1 为打乱顺序，2 为不打乱");
   }

   private static void printUsage() {
      System.err.println();
      System.err.println("用法:");
      System.err.println("  codeclean \"软件名称\" \"项目路径\"");
      System.err.println("  codeclean \"软件名称\" \"版本号\" \"项目路径\" \"指定文件后缀\" \"密匙\" 1/2");
      System.err.println();
      System.err.println("默认值:");
      System.err.println("  版本号: V1.0");
      System.err.println("  指定文件后缀: py,sql,tsx,ts");
      System.err.println("  密匙: 937599");
      System.err.println("  顺序: 1，打乱顺序");
   }

   private static class Options {
      private String name;
      private String version;
      private String sourceDir;
      private List<String> fileTypes;
      private String key;
      private boolean random;
   }
}
