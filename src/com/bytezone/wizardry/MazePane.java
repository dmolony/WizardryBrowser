package com.bytezone.wizardry;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
//import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

public class MazePane extends JPanel
{
  private static String[][] text = {
      { "Teleport", "Spells fizzle out here", "An encounter", "Pit", "Stairs going up",
          "Stairs going down", "Rock", "Darkness", "Message", "Elevator", "Chute", "Hotdog Stand",
          "Spinner", "Monsters' Lair", "Wall", "Door", "Secret door" },
      { "Téléportation", "Anti-sorts", "Rencontre", "Fosse", "Escalier vers le haut",
          "Escalier vers le bas", "Roc", "Noirceur", "Message", "Ascenseur", "Chute",
          "Stand de hot-dog", "Plaque tournante", "Antre des monstres", "Mur", "Porte",
          "Porte secrète" } };

  MazeDataModel model;
  BufferedImage image;
  int mazeOffsetX = 0;
  int mazeOffsetY = 0;
  int imageOffsetX = 28;
  int imageOffsetY = 28;

  Dimension cellSize = new Dimension (22, 22);          // size in pixels
  Rectangle mazeArea = new Rectangle (0, 0, 20 * cellSize.width, 20 * cellSize.height);

  public MazePane (MazeDataModel model)
  {
    this.model = model;
    createImage ();
    setBackground (Color.BLACK);
    setToolTipText ("");                        // turn on tooltips
    ToolTipManager ttm = ToolTipManager.sharedInstance ();
    ttm.setDismissDelay (8000);                 // 8 seconds

    addKeyListener (new KeyAdapter ()
    {
      @Override
      public void keyPressed (KeyEvent e)
      {
        switch (e.getKeyCode ())
        {
          case KeyEvent.VK_LEFT:
            if (--mazeOffsetX < 0)
              mazeOffsetX = 19;
            image = null;
            repaint ();
            break;

          case KeyEvent.VK_RIGHT:
            if (++mazeOffsetX >= 20)
              mazeOffsetX = 0;
            image = null;
            repaint ();
            break;

          case KeyEvent.VK_DOWN:
            if (--mazeOffsetY < 0)
              mazeOffsetY = 19;
            image = null;
            repaint ();
            break;

          case KeyEvent.VK_UP:
            if (++mazeOffsetY >= 20)
              mazeOffsetY = 0;
            image = null;
            repaint ();
            break;
        }
      }
    });
  }

  @Override
  public String getToolTipText (MouseEvent e)
  {
    Point p = new Point (e.getX () - imageOffsetX, e.getY () - imageOffsetY);
    if (!mazeArea.contains (p))
      return null;

    int row = 19 - p.y / cellSize.height;
    int column = p.x / cellSize.width;

    MazeCell cell = model.getLocation ((row + mazeOffsetY) % model.getRows (),
        (column + mazeOffsetX) % model.getColumns ());
    return cell.getTooltipText ();
  }

  private void createImage ()
  {
    int rows = model.getRows ();
    int columns = model.getColumns ();

    image = new BufferedImage (columns * cellSize.width + 1, rows * cellSize.height + 1,
        BufferedImage.TYPE_USHORT_555_RGB);
    Graphics2D g = image.createGraphics ();
    g.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    for (int row = 0; row < rows; row++)
      for (int column = 0; column < columns; column++)
      {
        MazeCell cell = model.getLocation ((row + mazeOffsetY) % model.getRows (),
            (column + mazeOffsetX) % model.getColumns ());
        int x = column * cellSize.width;
        int y = image.getHeight () - (row + 1) * cellSize.height - 1;
        cell.draw (g, x, y);
      }
  }

  @Override
  public Dimension getPreferredSize ()
  {
    return new Dimension (20 * cellSize.width + 1, 20 * cellSize.height + 1);
  }

  @Override
  public void paintComponent (Graphics g1D)
  {
    super.paintComponent (g1D);

    if (image == null)
      createImage ();

    Graphics2D g = (Graphics2D) g1D;
    g.drawImage (image, imageOffsetX, imageOffsetY, this);

    g.setColor (Color.WHITE);
    g.drawRect (500, 29, 195, 438);

    int x = 520;
    int y = 50;
    int offsetX = x + 25;
    int offsetY = 16;
    int lineHeight = 22;

    int lang = 0;
    int n = 0;

    MazeCell.drawTeleport (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawSpellsBlocked (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawMonster (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawPit (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawStairsUp (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawStairsDown (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawRock (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawDarkness (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawChar (g, x, y, "M", Color.RED);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawElevator (g, x, y, 3);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawChute (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawHotDogStand (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    g.drawString ("S", x + 8, y + 16);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawMonsterLair (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    MazeCell.drawWest (g, x, y);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    g.setColor (Color.RED);
    MazeCell.drawWest (g, x, y);
    g.setColor (Color.WHITE);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;

    g.setColor (Color.GREEN);
    MazeCell.drawWest (g, x, y);
    g.setColor (Color.WHITE);
    g.drawString (text[lang][n++], offsetX, y + offsetY);
    y += lineHeight;
  }
}
