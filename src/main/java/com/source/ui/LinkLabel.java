package com.source.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.JLabel;

public class LinkLabel extends JLabel {
   private static final long serialVersionUID = 1L;
   private String text;
   private URL link = null;
   private Color preColor = null;

   public LinkLabel(final JLabel jLabel, String vText, String vLink) {
      super("<html>" + vText + "</html>");
      this.text = vText;
      jLabel.setText("<html>" + this.text + "</html>");
      jLabel.setForeground(Color.BLUE);

      try {
         if (!vLink.startsWith("http://")) {
            vLink = "http://" + vLink;
         }

         this.link = new URL(vLink);
      } catch (MalformedURLException err) {
         err.printStackTrace();
      }

      jLabel.addMouseListener(new MouseAdapter() {
         public void mouseExited(MouseEvent e) {
            jLabel.setCursor(Cursor.getPredefinedCursor(0));
            if (LinkLabel.this.preColor != null) {
               jLabel.setForeground(LinkLabel.this.preColor);
            }

            jLabel.setText("<html>" + LinkLabel.this.text + "</html>");
         }

         public void mouseEntered(MouseEvent e) {
            jLabel.setCursor(Cursor.getPredefinedCursor(12));
            LinkLabel.this.preColor = jLabel.getForeground();
            jLabel.setForeground(Color.BLUE);
            jLabel.setText("<html><u>" + LinkLabel.this.text + "</u></html>");
         }

         public void mouseClicked(MouseEvent e) {
            try {
               Desktop.getDesktop().browse(LinkLabel.this.link.toURI());
            } catch (IOException err) {
               err.printStackTrace();
            } catch (URISyntaxException err) {
               err.printStackTrace();
            }

         }
      });
   }
}
