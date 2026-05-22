package com.source.module;

public class SourceModule {
   private String name;
   private String version;
   private String sourceDir;
   private String fileType;
   private String sourceLong;
   private String key;
   private boolean random;

   public String getName() {
      return this.name;
   }

   public void setName(final String name) {
      this.name = name;
   }

   public String getVersion() {
      return this.version;
   }

   public void setVersion(final String version) {
      this.version = version;
   }

   public String getSourceDir() {
      return this.sourceDir;
   }

   public void setSourceDir(final String sourceDir) {
      this.sourceDir = sourceDir;
   }

   public String getFileType() {
      return this.fileType;
   }

   public void setFileType(final String fileType) {
      this.fileType = fileType;
   }

   public String getSourceLong() {
      return this.sourceLong;
   }

   public void setSourceLong(final String sourceLong) {
      this.sourceLong = sourceLong;
   }

   public String getKey() {
      return this.key;
   }

   public void setKey(final String key) {
      this.key = key;
   }

   public boolean isRandom() {
      return this.random;
   }

   public void setRandom(boolean random) {
      this.random = random;
   }

   public String toString() {
      return "SourceModule{name='" + this.name + "', version='" + this.version + "', sourceDir='" + this.sourceDir + "', fileType='" + this.fileType + "', sourceLong='" + this.sourceLong + "', key='" + this.key + "'}";
   }
}
