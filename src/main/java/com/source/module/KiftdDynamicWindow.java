package com.source.module;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.Enumeration;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class KiftdDynamicWindow extends JFrame {
   private static final int OriginResolution_W = 1440;
   private static final int OriginResolution_H = 900;
   private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
   private static double proportionW;
   private static double proportionH;
   protected static double proportion;
   protected static Dimension fileChooerSize;

   public KiftdDynamicWindow() {
      try {
         String udp = "1.5";
         if (udp != null) {
            double udpi = Double.parseDouble(udp);
            if (udpi > (double)10.0F) {
               udpi = (double)10.0F;
            }

            proportion = udpi;
         }
      } catch (Exception var4) {
      }

      if (proportion < (double)1.0F) {
         proportion = (double)1.0F;
      }

      fileChooerSize = new Dimension((int)((double)590.0F * proportion), (int)((double)400.0F * proportion));
   }

   protected static void modifyComponentSize(Container c) {
      c.setPreferredSize(fileChooerSize);
   }

   public int getOriginResolution_W() {
      return 1440;
   }

   public int getOriginResolution_H() {
      return 900;
   }

   protected static void setUIFont() {
      Font f = new Font("宋体", 0, (int)((double)12.0F * proportion));
      String[] names = new String[]{"Label", "CheckBox", "PopupMenu", "MenuItem", "CheckBoxMenuItem", "JRadioButtonMenuItem", "ComboBox", "Button", "Tree", "ScrollPane", "TabbedPane", "EditorPane", "TitledBorder", "Menu", "TextArea", "OptionPane", "MenuBar", "ToolBar", "ToggleButton", "ToolTip", "ProgressBar", "TableHeader", "Panel", "List", "ColorChooser", "PasswordField", "TextField", "Table", "Label", "Viewport", "RadioButtonMenuItem", "RadioButton", "DesktopPane", "InternalFrame"};
      Enumeration keys = UIManager.getDefaults().keys();

      while(keys.hasMoreElements()) {
         Object key = keys.nextElement();
         Object value = UIManager.get(key);
         if (value instanceof FontUIResource) {
            for(String item : names) {
               UIManager.put(item + ".font", f);
            }
         }
      }

      UIManager.put("defaultFont", f);
   }

   static {
      proportionW = screenSize.getWidth() / (double)1440.0F;
      proportionH = screenSize.getHeight() / (double)900.0F;
      proportion = proportionW > proportionH ? proportionH : proportionW;
   }
}
