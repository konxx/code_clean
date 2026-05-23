package com.source.ui;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

final class WindowsFolderChooser {
   private static final int CLSCTX_INPROC_SERVER = 1;
   private static final int COINIT_APARTMENTTHREADED = 2;
   private static final int FOS_PICKFOLDERS = 32;
   private static final int FOS_FORCEFILESYSTEM = 64;
   private static final int FOS_PATHMUSTEXIST = 2048;
   private static final int HRESULT_ERROR_CANCELLED = 0x800704C7;
   private static final int SIGDN_FILESYSPATH = 0x80058000;
   private static final GUID CLSID_FILE_OPEN_DIALOG = new GUID("DC1C5A9C-E88A-4DDE-A5A1-60F82A20AEF7");
   private static final GUID IID_I_FILE_OPEN_DIALOG = new GUID("D57C7288-D4AD-4768-BE02-9D969532D960");

   private WindowsFolderChooser() {
   }

   static File chooseFolder(String title) {
      if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
         throw new UnsupportedOperationException("现代目录选择器仅支持 Windows");
      }

      FutureTask<File> task = new FutureTask<>(() -> chooseFolderOnStaThread(title));
      Thread thread = new Thread(task, "windows-modern-folder-chooser");
      thread.setDaemon(true);
      thread.start();

      try {
         return task.get();
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new IllegalStateException("选择目录被中断", e);
      } catch (ExecutionException e) {
         Throwable cause = e.getCause();
         if (cause instanceof RuntimeException) {
            throw (RuntimeException)cause;
         }

         throw new IllegalStateException("现代目录选择器启动失败", cause);
      }
   }

   private static File chooseFolderOnStaThread(String title) {
      int initResult = Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, COINIT_APARTMENTTHREADED);
      checkResult(initResult, "CoInitializeEx");
      Pointer dialog = null;
      Pointer item = null;
      Pointer pathPointer = null;

      try {
         PointerByReference dialogReference = new PointerByReference();
         int createResult = Ole32.INSTANCE.CoCreateInstance(
            CLSID_FILE_OPEN_DIALOG,
            Pointer.NULL,
            CLSCTX_INPROC_SERVER,
            IID_I_FILE_OPEN_DIALOG,
            dialogReference
         );
         checkResult(createResult, "CoCreateInstance");
         dialog = dialogReference.getValue();

         int options = FOS_PICKFOLDERS | FOS_FORCEFILESYSTEM | FOS_PATHMUSTEXIST;
         checkResult(invokeInt(dialog, 9, options), "IFileDialog.SetOptions");
         if (title != null && !title.trim().isEmpty()) {
            checkResult(invokeInt(dialog, 17, new WString(title)), "IFileDialog.SetTitle");
         }

         int showResult = invokeInt(dialog, 3, Pointer.NULL);
         if (showResult == HRESULT_ERROR_CANCELLED) {
            return null;
         }

         checkResult(showResult, "IFileDialog.Show");
         PointerByReference itemReference = new PointerByReference();
         checkResult(invokeInt(dialog, 20, itemReference), "IFileDialog.GetResult");
         item = itemReference.getValue();

         PointerByReference pathReference = new PointerByReference();
         checkResult(invokeInt(item, 5, SIGDN_FILESYSPATH, pathReference), "IShellItem.GetDisplayName");
         pathPointer = pathReference.getValue();
         return pathPointer == null ? null : new File(pathPointer.getWideString(0));
      } finally {
         if (pathPointer != null) {
            Ole32.INSTANCE.CoTaskMemFree(pathPointer);
         }

         if (item != null) {
            invokeInt(item, 2);
         }

         if (dialog != null) {
            invokeInt(dialog, 2);
         }

         Ole32.INSTANCE.CoUninitialize();
      }
   }

   private static int invokeInt(Pointer comObject, int methodIndex, Object... args) {
      Pointer vtable = comObject.getPointer(0);
      Pointer functionPointer = vtable.getPointer((long)Native.POINTER_SIZE * methodIndex);
      Function function = Function.getFunction(functionPointer, Function.ALT_CONVENTION);
      Object[] parameters = new Object[args.length + 1];
      parameters[0] = comObject;
      System.arraycopy(args, 0, parameters, 1, args.length);
      return function.invokeInt(parameters);
   }

   private static void checkResult(int result, String action) {
      if (result < 0) {
         throw new IllegalStateException(action + " 失败，HRESULT=0x" + Integer.toHexString(result));
      }
   }

   public static class GUID extends Structure {
      public int Data1;
      public short Data2;
      public short Data3;
      public byte[] Data4 = new byte[8];

      GUID(String value) {
         String[] parts = value.replace("{", "").replace("}", "").split("-");
         this.Data1 = (int)Long.parseLong(parts[0], 16);
         this.Data2 = (short)Integer.parseInt(parts[1], 16);
         this.Data3 = (short)Integer.parseInt(parts[2], 16);
         this.Data4[0] = (byte)Integer.parseInt(parts[3].substring(0, 2), 16);
         this.Data4[1] = (byte)Integer.parseInt(parts[3].substring(2, 4), 16);

         for(int i = 0; i < 6; ++i) {
            this.Data4[i + 2] = (byte)Integer.parseInt(parts[4].substring(i * 2, i * 2 + 2), 16);
         }

         this.write();
      }

      protected List<String> getFieldOrder() {
         return Arrays.asList("Data1", "Data2", "Data3", "Data4");
      }
   }

   private interface Ole32 extends Library {
      Ole32 INSTANCE = Native.load("Ole32", Ole32.class);

      int CoInitializeEx(Pointer reserved, int coInit);

      int CoCreateInstance(GUID classId, Pointer outer, int context, GUID interfaceId, PointerByReference result);

      void CoTaskMemFree(Pointer pointer);

      void CoUninitialize();
   }
}
