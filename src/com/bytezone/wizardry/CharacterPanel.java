package com.bytezone.wizardry;

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.text.NumberFormat;
import java.util.Iterator;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

import javax.swing.JPanel;
import static com.bytezone.wizardry.Spell.SpellType;

public class CharacterPanel extends JPanel
{
  private Character character;
  private Font headerFont;
  private Font textFont;
  private Font itemFont;
  private FontMetrics itemFontMetrics;
  private FontMetrics headerFontMetrics;
  private NumberFormat nf = NumberFormat.getNumberInstance ();

  private Rectangle spellArea = new Rectangle (30, 334, 665, 155);
  private Rectangle graphArea = new Rectangle (317, 60, 380, 260);
  int spellOffsetX = 28;
  int spellLineHeight = 14;
  
//private static String[] columnToolTips = {
//    "STRENGTH - affects combat ability",
//    "IQ - determines the ability to cast mage spells",
//    "PIETY - determines the ability to cast priest spells",
//    "VITALITY - modifies amount of damage that can be sustained " + 
//                    "before death",
//    "AGILITY - determines the order in which attacks occur",
//    "LUCK - helps in many mysterious ways" };

  public CharacterPanel(Character character)
  {
    this.character = character;
    headerFont = new Font ("Verdana", Font.BOLD, 24);
    textFont = new Font ("Verdana", Font.PLAIN, 13);
    itemFont = new Font ("Tahoma", Font.PLAIN, 12);
    setBackground (Color.WHITE);
    itemFontMetrics = getFontMetrics (itemFont);
    headerFontMetrics = getFontMetrics (headerFont);
    setToolTipText (""); // dummy call to turn on tooltip mechanism
  }

  public String getToolTipText (MouseEvent e)
  {
    Point p = new Point (e.getX (), e.getY ());
    if (spellArea.contains (p))
      return getSpellToolTipText (p);
    if (graphArea.contains (p))
      return "character attributes";
    return null;
  }

  private String getSpellToolTipText (Point p)
  {
    int spellLevel = (p.x - spellOffsetX) / 95 + 1;
    SpellType type;
    int spellNo;
    if (p.y > 424) // 334 + 90
    {
      type = SpellType.MAGE;
      spellNo = (p.y - 430) / spellLineHeight;
    }
    else
    {
      type = SpellType.PRIEST;
      spellNo = (p.y - 340) / spellLineHeight;
    }

    int lastLevel = -1;
    int spellSeq = -1;
    Iterator<Spell> i = character.getSpells ();
    while (i.hasNext ())
    {
      Spell s = i.next ();
      SpellType st = s.getType ();
      int level = s.getLevel ();
      if (level != lastLevel)
      {
        lastLevel = level;
        spellSeq = -1;
      }
      ++spellSeq;
      if (st == type && level == spellLevel && spellNo == spellSeq)
      {
        StringBuilder text = new StringBuilder ("<html>" + s.getDescription ());
        for (int j = 46, pos = 0; j < text.length (); j = pos + 44)
        {
          pos = text.indexOf (" ", j); // first space after 40 characters
          if (pos < 0)
            break;
          text.insert (pos, "<br>".toCharArray ());
        }
        text.append ("</html>");
        return text.toString ();
      }
    }
    return null;
  }

  public void paintComponent (Graphics g)
  {
    super.paintComponent (g);

    Statistics stats = character.getStatistics ();
    g.setFont (headerFont);
    String name = character.getName ();
    if (character.isWinner ())
      name += ">";
    if (character.isOut ())
      name += " * out *";
    if (stats.statusValue != 0)
      name += " - " + stats.status;
    g.drawString (name, 20, 35);
    String type = character.getType ();
    g.drawString (type,
            getWidth () - headerFontMetrics.stringWidth (type) - 20, 35);

    int lineHeight = 17;
    int x1 = 40;
    int x2 = 140;
    int y = 70;

    g.setFont (textFont);

    g.drawString ("Race", x1, y);
    g.drawString (stats.race, x2, y);
    y += lineHeight;

    g.drawString ("Alignment", x1, y);
    g.drawString (stats.alignment, x2, y);
    y += lineHeight;

    if (stats.experience >= stats.nextLevel)
      g.setColor (Color.RED);
    g.drawString ("Experience", x1, y);
    g.drawString (nf.format (stats.experience) + " / "
            + nf.format (stats.nextLevel), x2, y);
    g.setColor (Color.BLACK);
    y += lineHeight;

    g.drawString ("Gold", x1, y);
    g.drawString (nf.format (stats.gold), x2, y);
    y += lineHeight;

    g.drawString ("Level", x1, y);
    g.drawString (nf.format (stats.level), x2, y);
    y += lineHeight;

    g.drawString ("Age", x1, y);
    g.drawString (nf.format (stats.ageInWeeks / 52), x2, y);
    y += lineHeight;

    g.drawString ("Hit points", x1, y);
    g.drawString (stats.hitsLeft + " / " + stats.hitsMax, x2, y);
    y += lineHeight;

    g.drawString ("Armour class", x1, y);
    g.drawString (nf.format (stats.armourClass), x2, y);
    y += lineHeight;

    paintBaggage ((Graphics2D) g);
    paintSpells ((Graphics2D) g);
    paintGraph ((Graphics2D) g);
  }

  private void paintBaggage (Graphics2D g)
  {
    g.setFont (itemFont);
    int y = 210;
    int x1 = 40;
    int x3 = 240;
    Iterator<Baggage> i = character.getBaggage ();
    while (i.hasNext ())
    {
      Baggage b = i.next ();
      if (b.equipped)
        g.drawString ("*", x1, y);
      else if (!b.identified)
        g.drawString ("?", x1, y);
      else if (!b.item.canUse (this.character.getStatistics ().typeInt))
        g.drawString ("#", x1, y);
      g.drawString (b.item.getName (), x1 + 10, y);
      long value = b.item.getCost ();
      if (value > 0)
      {
        String valueText = nf.format (value);
        g.drawString (valueText, x3 - itemFontMetrics.stringWidth (valueText),
                y);
      }
      int ac = b.item.getArmourClass ();
      if (ac > 0)
        g.drawString ("(" + ac + ")", x3 + 15, y);
      y += 16;
    }
  }

  private void paintSpells (Graphics2D g)
  {
    int[] mageSpells = character.getMageSpellPoints ();
    int[] priestSpells = character.getPriestSpellPoints ();
    int lastLevel = 0;
    int seq = 0;

    int colWidth = spellArea.width / 7;

    RoundRectangle2D.Float rect = new RoundRectangle2D.Float (spellArea.x,
            spellArea.y, spellArea.width, spellArea.height, 25.0f, 25.0f);
    g.setStroke (new BasicStroke (4));
    g.setPaint (Color.gray);
    g.draw (rect);
    g.setStroke (new BasicStroke (2));
    g.drawLine (spellArea.x, 424, spellArea.x + spellArea.width, 424);
    for (int i = 0; i < 6; i++)
    {
      int x = (i + 1) * colWidth + spellArea.x;
      g.drawLine (x, spellArea.y, x, spellArea.y + spellArea.height);
    }
    g.setPaint (Color.black);

    Iterator<Spell> i = character.getSpells ();
    while (i.hasNext ())
    {
      Spell spell = i.next ();
      int level = spell.getLevel ();
      if (level != lastLevel)
      {
        lastLevel = level;
        seq = 0;
      }
      int x = (level - 1) * colWidth + spellArea.x + spellOffsetX;
      int yy = spell.getType () == SpellType.MAGE ? 1 : 0;
      int y = 350 + yy * 90 + seq * spellLineHeight;

      if (seq == 0)
      {
        int spellPoints = 0;
        if (spell.getType () == SpellType.PRIEST)
          spellPoints = priestSpells[level - 1];
        else
          spellPoints = mageSpells[level - 1];
        g.drawString (spellPoints + " :", x - 20, y);
      }

      g.drawString (spell.getName ().toLowerCase (), x, y);
      seq++;
    }
  }

  private void paintGraph (Graphics2D g)
  {
    int barWidth = 30;
    int inset = 40;
    int gap = 20;
    int unitSize = 10;
    char[] categories = { 'S', 'I', 'P', 'V', 'A', 'L' };

    Attributes attr = character.getAttributes ();

    RoundRectangle2D.Float rect = new RoundRectangle2D.Float (graphArea.x,
            graphArea.y, graphArea.width, graphArea.height, 25.0f, 25.0f);
    g.setPaint (Color.black);
    g.fill (rect);
    g.setStroke (new BasicStroke (1));

    g.setColor (Color.CYAN);
    for (int i = 0; i < attr.array.length; i++)
    {
      int h = attr.array[i] * unitSize;
      g.fillRect (graphArea.x + inset + i * (gap + barWidth) + gap, graphArea.y
              + graphArea.height - inset - h, barWidth, h);
    }

    g.setColor (Color.GREEN);

    g.drawLine (graphArea.x + inset, graphArea.y + graphArea.height - inset,
            graphArea.x + inset + 6 * (barWidth + gap) + gap, graphArea.y
                    + graphArea.height - inset);

    g.drawLine (graphArea.x + inset, graphArea.y + graphArea.height - inset,
            graphArea.x + inset, graphArea.y + graphArea.height - inset - 18
                    * unitSize - gap);

    for (int i = 0; i < categories.length; i++)
    {
      int x = graphArea.x + i * (barWidth + gap) + inset + gap + barWidth / 2;
      int y = graphArea.y + graphArea.height - inset;
      g.drawChars (categories, i, 1, x - 3, y + 20);
      int val = attr.array[i];
      g.drawString (nf.format (val), x - 8, y - val * unitSize - 8);
    }
  }
}
