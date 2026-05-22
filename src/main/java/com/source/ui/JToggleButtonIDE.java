package com.source.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JToggleButton;
import javax.swing.Timer;

public class JToggleButtonIDE extends JToggleButton implements MouseListener {
   private static final int DELAY_TIME = 1;
   private static final int START_X = 10;
   private static final int HEIGHT = 35;
   private static final int WIDTH = 80;
   private static final Color DEFAULT_COLOR = new Color(75, 237, 144);
   private static final Color BACKGROUND_COLOR = Color.decode("#E0E0E0");
   private static final int ROUND_RECT_ARC = 40;
   private int x;
   private int y;
   private Timer timer;
   private Color co;

   public JToggleButtonIDE() {
      this(DEFAULT_COLOR);
   }

   public JToggleButtonIDE(Color color) {
      this.co = color;
      this.addMouseListener(this);
      this.setPreferredSize(new Dimension(80, 35));
   }

   protected void paintComponent(Graphics g) {
      int w = this.getWidth();
      int h = this.getHeight();
      Graphics2D g2d = (Graphics2D)g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setColor(BACKGROUND_COLOR);
      RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0.0F, 0.0F, (float)(w - 1), (float)(h - 1), 40.0F, 40.0F);
      g2d.fill(roundedRectangle);
      if (this.isSelected()) {
         g2d.setColor(this.co);
         g2d.fillRoundRect(1, 1, w - 2, h - 2, 40, 40);
      }

      g2d.setColor(Color.WHITE);
      int circleSize = Math.min(31, this.getWidth() - 4);
      int circleX = this.x;
      int circleY = (this.getHeight() - circleSize) / 2;
      g2d.fillOval(circleX, circleY, circleSize, circleSize);
   }

   public void startAnimation() {
      this.y = 10;
      this.timer = new Timer(1, (e) -> {
         if (this.isSelected()) {
            this.x += 5;
            if (this.x >= 45) {
               this.timer.stop();
            }
         } else {
            this.x -= 5;
            if (this.x <= 0) {
               this.timer.stop();
            }
         }

         this.repaint();
      });
      this.timer.start();
   }

   public void mouseClicked(MouseEvent e) {
      if (this.timer != null && this.timer.isRunning()) {
         this.timer.stop();
      }

      this.startAnimation();
   }

   public void mousePressed(MouseEvent e) {
   }

   public void mouseReleased(MouseEvent e) {
   }

   public void mouseEntered(MouseEvent e) {
   }

   public void mouseExited(MouseEvent e) {
   }
}
