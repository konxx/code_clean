package com.source.ui;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.source.docx.SourceWord;
import com.source.module.KiftdDynamicWindow;
import com.source.module.SourceModule;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import li.flor.nativejfilechooser.NativeJFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ui extends KiftdDynamicWindow {
   private static Ui instance = null;
   private JPanel jPanel;
   private JButton 生成Button;
   private JButton 退出Button;
   private JFormattedTextField formattedTextField4;
   private JFormattedTextField formattedTextField2;
   private JFormattedTextField formattedTextField1;
   private JButton 选择Button;
   private JFormattedTextField a1700FormattedTextField;
   private JTextArea formattedTextField3;
   private JFormattedTextField formattedTextField6;
   private JToggleButtonIDE JTButton1;
   private boolean JTButtonBoolean;

   public Ui() {
      Logger logger = LoggerFactory.getLogger(this.getClass());
      this.$$$setupUI$$$();
      this.生成Button.addActionListener((e) -> {
         SourceModule data = new SourceModule();
         this.getData(data);
         if (!this.isAll(data)) {
            JOptionPane.showMessageDialog((Component)null, "未填写完全");
            logger.error("未填写完全");
         } else {
            String[] fileTypeArray = data.getFileType().split(",");
            List<String> fileType = Arrays.asList(fileTypeArray);
            int fileCount = this.countFiles(data.getSourceDir(), fileType);
            if (fileCount > 300) {
               int result = JOptionPane.showConfirmDialog((Component)null, "检测到项目文件过多(共 " + fileCount + " 个文件)，这可能导致处理缓慢或内存不足。\n建议复制关键代码到新建目录后再生成。\n\n是否继续生成?", "提示", 0, 2);
               if (result != 0) {
                  return;
               }
            }

            try {
               SourceWord sourceWord = new SourceWord();
               sourceWord.start(data.getName(), data.getVersion(), data.getSourceDir(), fileType, Integer.parseInt(data.getSourceLong()), data.getKey(), data.isRandom());
            } catch (Throwable ex) {
               logger.error("启动文档生成任务失败", ex);
               JOptionPane.showMessageDialog((Component)null, "启动失败：" + ex.getMessage() + "\n请提交软件目录logs/error.log错误报告", "错误", 0);
               ex.printStackTrace();
            }

         }
      });
      this.选择Button.addActionListener((e) -> {
         File selectedFile = null;
         try {
            selectedFile = WindowsFolderChooser.chooseFolder("请选择目录");
         } catch (Throwable ex) {
            logger.warn("现代目录选择器调用失败，准备回退到备用选择器", ex);
         }

         if (selectedFile == null) {
            NativeJFileChooser fileChooser = new NativeJFileChooser();
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileSelectionMode(NativeJFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("请选择目录");
            Window topLevelWindow = SwingUtilities.getWindowAncestor(this.jPanel);
            int result = fileChooser.showOpenDialog(topLevelWindow);
            if (result == NativeJFileChooser.APPROVE_OPTION) {
               selectedFile = fileChooser.getSelectedFile();
            }
         }

         if (selectedFile != null) {
            this.formattedTextField3.setText(selectedFile.getPath());
         }

      });
   }

   public static void main(String[] args) throws Exception {
      getInstance().initUI();
   }

   public static Ui getInstance() {
      if (null == instance) {
         synchronized(Ui.class) {
            if (null == instance) {
               instance = new Ui();
            }
         }
      }

      return instance;
   }

   private void createUIComponents() {
      this.formattedTextField3 = new JTextArea();
      this.formattedTextField3.setLineWrap(true);
      this.formattedTextField3.setEditable(false);
      this.JTButton1 = new JToggleButtonIDE();
      this.JTButton1.putClientProperty("JButton.buttonType", "roundRect");
      this.JTButton1.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (Ui.this.JTButton1.isSelected()) {
               Ui.this.JTButtonBoolean = true;
            } else {
               Ui.this.JTButtonBoolean = false;
            }

         }
      });
   }

   public boolean isAll(SourceModule data) {
      return !"".equals(data.getName()) && !"".equals(data.getFileType()) && !"".equals(data.getSourceDir()) && !"".equals(data.getVersion());
   }

   public void getData(SourceModule data) {
      data.setFileType(this.formattedTextField4.getText());
      data.setVersion(this.formattedTextField2.getText());
      data.setName(this.formattedTextField1.getText());
      data.setSourceLong("1700");
      data.setSourceDir(this.formattedTextField3.getText());
      data.setKey(this.formattedTextField6.getText());
      data.setRandom(this.JTButtonBoolean);
   }

   private int countFiles(String dirPath, List<String> fileTypes) {
      File dir = new File(dirPath);
      return dir.exists() && dir.isDirectory() ? this.countFilesRecursive(dir, fileTypes) : 0;
   }

   private int countFilesRecursive(File dir, List<String> fileTypes) {
      int count = 0;
      File[] files = dir.listFiles();
      if (files == null) {
         return 0;
      } else {
         for(File file : files) {
            if (file.isDirectory()) {
               count += this.countFilesRecursive(file, fileTypes);
            } else {
               String fileName = file.getName();

               for(String type : fileTypes) {
                  if (fileName.endsWith("." + type.trim())) {
                     ++count;
                     break;
                  }
               }
            }
         }

         return count;
      }
   }

   private void $$$setupUI$$$() {
      this.createUIComponents();
      this.jPanel = new JPanel();
      this.jPanel.setLayout(new GridLayoutManager(12, 6, new Insets(0, 10, 15, 10), -1, -1));
      this.jPanel.setBorder(BorderFactory.createTitledBorder((Border)null, "桃神自用", 0, 0, (Font)null, (Color)null));
      JLabel label1 = new JLabel();
      label1.setText("软件名称");
      this.jPanel.add(label1, new GridConstraints(2, 0, 1, 1, 0, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      JLabel label2 = new JLabel();
      label2.setText("版本号");
      this.jPanel.add(label2, new GridConstraints(3, 0, 1, 1, 0, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      JLabel label3 = new JLabel();
      label3.setText("项目路径");
      this.jPanel.add(label3, new GridConstraints(4, 0, 1, 1, 0, 2, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      this.formattedTextField2 = new JFormattedTextField();
      this.formattedTextField2.setText("V1.0");
      this.jPanel.add(this.formattedTextField2, new GridConstraints(3, 1, 1, 5, 8, 1, 2, 0, (Dimension)null, new Dimension(150, -1), (Dimension)null, 0, false));
      this.formattedTextField1 = new JFormattedTextField();
      this.formattedTextField1.setText("软件");
      this.jPanel.add(this.formattedTextField1, new GridConstraints(2, 1, 1, 5, 8, 1, 4, 0, (Dimension)null, new Dimension(150, -1), (Dimension)null, 0, false));
      JLabel label4 = new JLabel();
      label4.setText("密钥");
      this.jPanel.add(label4, new GridConstraints(7, 0, 1, 1, 0, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      this.formattedTextField6 = new JFormattedTextField();
      this.formattedTextField6.setText("937599");
      this.jPanel.add(this.formattedTextField6, new GridConstraints(7, 1, 1, 5, 8, 1, 4, 0, (Dimension)null, new Dimension(150, -1), (Dimension)null, 0, false));
      JLabel label5 = new JLabel();
      label5.setText("代码清理,可生成两个文件：");
      this.jPanel.add(label5, new GridConstraints(0, 1, 1, 5, 0, 1, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      JLabel label7 = new JLabel();
      label7.setText("1.全量备查源代码");
      this.jPanel.add(label7, new GridConstraints(1, 1, 1, 2, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      JLabel label8 = new JLabel();
      label8.setText("2.源代码前后30页");
      this.jPanel.add(label8, new GridConstraints(1, 3, 1, 2, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      JLabel label11 = new JLabel();
      label11.setText("指定文件后缀");
      this.jPanel.add(label11, new GridConstraints(5, 0, 1, 1, 0, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      this.formattedTextField4 = new JFormattedTextField();
      this.formattedTextField4.setText("py,sql,tsx,ts");
      this.jPanel.add(this.formattedTextField4, new GridConstraints(5, 1, 1, 5, 8, 1, 4, 0, (Dimension)null, new Dimension(150, -1), (Dimension)null, 0, false));
      this.选择Button = new JButton();
      this.选择Button.setText("选择");
      this.jPanel.add(this.选择Button, new GridConstraints(4, 5, 1, 1, 0, 1, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      JPanel panel4 = new JPanel();
      panel4.setLayout(new FlowLayout(1, 5, 5));
      this.jPanel.add(panel4, new GridConstraints(8, 1, 1, 5, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      JLabel label13 = new JLabel();
      label13.setForeground(new Color(-4512223));
      label13.setText("未填写密钥只可生成全量源代码，填写后可生成两个文件");
      panel4.add(label13);
      JPanel panel5 = new JPanel();
      panel5.setLayout(new FlowLayout(1, 5, 5));
      this.jPanel.add(panel5, new GridConstraints(6, 1, 1, 5, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      JLabel label14 = new JLabel();
      label14.setText("指定多个文件请使用英文逗号分隔，请参考默认填写格式");
      panel5.add(label14);
      JPanel panel9 = new JPanel();
      panel9.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
      this.jPanel.add(panel9, new GridConstraints(4, 1, 1, 4, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      panel9.add(this.formattedTextField3, new GridConstraints(0, 0, 1, 1, 0, 3, 4, 4, (Dimension)null, new Dimension(158, 10), (Dimension)null, 0, false));
      JLabel label18 = new JLabel();
      label18.setText("是否打乱顺序");
      this.jPanel.add(label18, new GridConstraints(10, 0, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      this.生成Button = new JButton();
      this.生成Button.setBackground(new Color(-12542209));
      this.生成Button.setForeground(new Color(-1));
      this.生成Button.setText("生成");
      this.jPanel.add(this.生成Button, new GridConstraints(11, 3, 1, 1, 6, 0, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 1, false));
      JLabel label19 = new JLabel();
      label19.setText("注意事项");
      this.jPanel.add(label19, new GridConstraints(8, 0, 1, 1, 0, 2, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      JPanel panel10 = new JPanel();
      panel10.setLayout(new FlowLayout(1, 5, 5));
      this.jPanel.add(panel10, new GridConstraints(9, 1, 1, 5, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
      JLabel label20 = new JLabel();
      label20.setText("该软件需要用户有自己的项目代码");
      panel10.add(label20);
      this.jPanel.add(this.JTButton1, new GridConstraints(10, 1, 1, 1, 8, 0, 3, 3, (Dimension)null, new Dimension(80, 35), (Dimension)null, 0, false));
   }

   public JComponent $$$getRootComponent$$$() {
      return this.jPanel;
   }

   public void initUI() throws Exception {
      FlatIntelliJLaf.install();
      UIManager.put("TextComponent.arc", 5);
      UIManager.put("Component.focusWidth", 1);
      UIManager.put("Component.innerFocusWidth", 1);
      UIManager.put("Button.innerFocusWidth", 1);
      UIManager.put("TitlePane.unifiedBackground", true);
      UIManager.put("TitlePane.menuBarEmbedded", false);
      System.setProperty("awt.useSystemAAFontSettings", "on");
      System.setProperty("swing.aatext", "true");
      setUIFont();
      JFrame frame = new JFrame("代码清理");
      frame.setPreferredSize(new Dimension(780, 500));
      File iconFile = new File("logo.png");
      if (iconFile.exists()) {
         frame.setIconImage((new ImageIcon(iconFile.getAbsolutePath())).getImage());
      }
      frame.setContentPane((new Ui()).jPanel);
      frame.setDefaultCloseOperation(3);
      frame.pack();
      frame.setResizable(false);
      frame.setVisible(true);
      int windowWidth = frame.getWidth();
      int windowHeight = frame.getHeight();
      Toolkit kit = Toolkit.getDefaultToolkit();
      Dimension screenSize = kit.getScreenSize();
      int screenWidth = screenSize.width;
      int screenHeight = screenSize.height;
      frame.setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);
   }
}
