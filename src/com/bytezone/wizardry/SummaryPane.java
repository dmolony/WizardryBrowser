package com.bytezone.wizardry;

import java.awt.Font;
import java.awt.Graphics;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JPanel;

public class SummaryPane extends JPanel
{
  private ArrayList<Character> characters;
  private ArrayList<Item> items;
  private int totalGold = 0;
  private int totalAssets = 0;
  private int totalExperience = 0;
  private NumberFormat nf = NumberFormat.getNumberInstance ();
  private int itemsInStock = 0;
  private int totalItems = 0;
  private int totalOwnedItems = 0;
  private long stockValue = 0;

  public SummaryPane()
  {
    super ();
    setFont (new Font ("Verdana", Font.PLAIN, 12));
  }

  public void setCharacters (ArrayList<Character> characters)
  {
    if (characters == null)
      return;
    this.characters = characters;

    for (Character character : characters)
    {
      Statistics stats = character.getStatistics ();
      totalGold += stats.gold;
      totalAssets += stats.assetValue;
      totalExperience += stats.experience;
    }
  }

  public void setItems (ArrayList<Item> items)
  {
    if (items == null)
      return;
    this.items = items;

    for (Item item : items)
    {
      long cost = item.getCost ();
      totalItems++;
      int stock = item.getStockOnHand ();
      if (stock == 0)
      {
        if (item.partyOwns > 0)
          totalOwnedItems++;
        continue;
      }
      if (stock < 0)
        stock = 10;

      itemsInStock++;
      stockValue += stock * cost;
    }
    totalItems--; // ignore the broken item
  }

  public void paintComponent (Graphics g)
  {
    super.paintComponent (g);
    g.drawString ("Total gold : " + nf.format (totalGold), 20, 30); // x, y
    g.drawString ("Total assets : " + nf.format (totalAssets), 20, 50);
    g.drawString ("Total experience : " + nf.format (totalExperience), 20, 70);

    g.drawString ("Known items : " + nf.format (itemsInStock + totalOwnedItems)
            + " / " + nf.format (totalItems), 260, 30);
    g.drawString ("Stock value : " + nf.format (stockValue), 260, 50);
  }
}