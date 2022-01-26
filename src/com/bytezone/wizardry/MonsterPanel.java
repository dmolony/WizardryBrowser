package com.bytezone.wizardry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JPanel;

public class MonsterPanel extends JPanel
{
  private ArrayList<Monster> monsters;
  private ArrayList<BufferedImage> images = null;
  private int currentMonster = 0;

  private Font headerFont;
  private Font textFont;
  private FontMetrics itemFontMetrics;
  private FontMetrics headerFontMetrics;

  private NumberFormat nf = NumberFormat.getNumberInstance ();

  public MonsterPanel ()
  {
    setPreferredSize (new Dimension (500, 380));
    headerFont = new Font ("Verdana", Font.BOLD, 24);
    textFont = new Font ("Verdana", Font.PLAIN, 13);
    headerFontMetrics = getFontMetrics (headerFont);
    setBackground (Color.WHITE);

    addKeyListener (new KeyAdapter ()
    {
      @Override
      public void keyPressed (KeyEvent e)
      {
        int c = e.getKeyCode ();
        switch (c)
        {
          case KeyEvent.VK_LEFT:
            if (--currentMonster < 0)
              currentMonster = monsters.size () - 1;
            repaint ();
            break;
          case KeyEvent.VK_RIGHT:
            if (++currentMonster >= monsters.size ())
              currentMonster = 0;
            repaint ();
            break;
        }
      }
    });
  }

  public void setMonsters (ArrayList<Monster> monsters)
  {
    this.monsters = monsters;
  }

  public void setImages (ArrayList<BufferedImage> images)
  {
    this.images = images;
  }

  public void setMonster (int monsterNo)
  {
    if (monsters == null || monsterNo < 0 || monsterNo >= monsters.size ())
      return;
    currentMonster = monsterNo;
    repaint ();
  }

  public int getCurrentMonster ()
  {
    return currentMonster;
  }

  @Override
  public void paintComponent (Graphics g)
  {
    super.paintComponent (g);

    if (monsters == null)
      return;

    Monster m = monsters.get (currentMonster);

    g.setFont (headerFont);
    g.drawString (m.getRealName (), 180, 40);
    g.setFont (textFont);
    g.drawString (m.getName (), 180, 60);
    g.drawString (m.getExperience () + "", 180, 80);

    int x = 20;
    int x1 = 150;
    int y = 160;
    int lineHeight = 20;

    g.drawString ("Class", x, y);
    g.drawString (m.getMonsterClass (), x1, y);
    y += lineHeight;

    g.drawString ("Armour class", x, y);
    g.drawString (nf.format (m.getArmourClass ()), x1, y);
    y += lineHeight;

    g.drawString ("Speed", x, y);
    g.drawString (nf.format (m.getSpeed ()), x1, y);
    y += lineHeight;

    g.drawString ("Mage spell level", x, y);
    g.drawString (nf.format (m.getMageSpellLevel ()), x1, y);
    y += lineHeight;

    g.drawString ("Priest spell level", x, y);
    g.drawString (nf.format (m.getPriestSpellLevel ()), x1, y);
    y += lineHeight;

    g.drawString ("Monster ID", x, y);
    g.drawString (nf.format (currentMonster), x1, y);
    y += lineHeight;

    g.drawString ("Maze level", x, y);
    g.drawString (nf.format (m.data[136] + 1), x1, y);
    y += lineHeight;

    g.drawString ("Group size", x, y);
    g.drawString (m.getGroupSize (), x1, y);
    y += lineHeight;

    g.drawString ("Hit points", x, y);
    g.drawString (m.getHitPoints (), x1, y);
    y += lineHeight;

    g.drawString ("Damage", x, y);
    g.drawString (m.getDamage (), x1, y);
    y += lineHeight;

    g.drawString ("Appears with", x, y);
    int odds = m.getPartnerOdds ();
    if (odds > 0)
      g.drawString (m.getPartnerID () + ":" + monsters.get (m.getPartnerID ()).getRealName () + " ("
          + odds + "%)", x1, y);
    else
      g.drawString ("None", x1, y);
    y += lineHeight;

    x = 240;
    x1 = 340;
    y = 160;

    g.drawString ("Unknown 1", x, y);
    g.drawString (String.format ("%02X", m.data[132]), x1, y);
    g.drawString ("(level drain?)", x1 + 40, y);
    y += lineHeight;

    g.drawString ("Unknown 2", x, y);
    g.drawString (String.format ("%02X", m.data[134]), x1, y);
    y += lineHeight;

    g.drawString ("Ability 1", x, y);
    g.drawString (String.format ("%02X", m.data[150]), x1, y);
    g.drawString ("(breathing?)", x1 + 40, y);
    y += lineHeight;

    g.drawString ("Ability 2", x, y);
    g.drawString (String.format ("%02X", m.data[152]), x1, y);
    y += lineHeight;

    g.drawString ("Ability 3", x, y);
    g.drawString (String.format ("%02X", m.data[154]), x1, y);
    y += lineHeight;

    g.drawString ("Ability 4", x, y);
    g.drawString (String.format ("%02X", m.data[156]), x1, y);
    y += lineHeight;

    if (images != null)
    {
      Graphics2D g2 = (Graphics2D) g;
      BufferedImage image = images.get (m.getImageID ());
      int scale = 2;
      g2.transform (AffineTransform.getScaleInstance (scale, scale));

      // set 2 pixel border around image
      g2.setColor (Color.BLACK);
      g2.fillRect (10, 10, 74, 54);
      g2.drawImage (image, 12, 12, this);
      // g2.transform (AffineTransform.getScaleInstance (1, 1));
    }
  }
}
