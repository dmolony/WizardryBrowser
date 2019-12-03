package com.bytezone.wizardry;

import java.awt.*;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
  MazeDataModel model;
  BufferedImage image;
  int mazeOffsetX = 0;
  int mazeOffsetY = 0;
  int imageOffsetX = 28;
  int imageOffsetY = 28;

  Dimension cellSize = new Dimension (22, 22); // size in pixels
  Rectangle mazeArea = new Rectangle (0, 0, 20 * cellSize.width,
          20 * cellSize.height);

  public MazePane(MazeDataModel model)
  {
    this.model = model;
    createImage ();
    setBackground (Color.BLACK);
    setToolTipText (""); // turn on tooltips
    ToolTipManager ttm = ToolTipManager.sharedInstance ();
    ttm.setDismissDelay (8000); // 8 seconds

    addKeyListener (new KeyAdapter ()
    {
      public void keyPressed (KeyEvent e)
      {
        int c = e.getKeyCode ();
        switch (c)
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

  public String getToolTipText (MouseEvent e)
  {
    Point p = new Point (e.getX () - imageOffsetX, e.getY () - imageOffsetY);
    if (!mazeArea.contains (p))
      return null;

    int row = 19 - p.y / cellSize.height;
    int column = p.x / cellSize.width;

    MazeCell cell = model.getLocation (
            (row + mazeOffsetY) % model.getRows (), (column + mazeOffsetX)
                    % model.getColumns ());
    return cell.getTooltipText ();
  }

  private void createImage ()
  {
    int rows = model.getRows ();
    int columns = model.getColumns ();

    image = new BufferedImage (columns * cellSize.width + 1, rows
            * cellSize.height + 1, BufferedImage.TYPE_USHORT_555_RGB);
    Graphics2D g = image.createGraphics ();
    g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

    for (int row = 0; row < rows; row++)
      for (int column = 0; column < columns; column++)
      {
        MazeCell cell = model.getLocation ((row + mazeOffsetY)
                % model.getRows (), (column + mazeOffsetX)
                % model.getColumns ());
        int x = column * cellSize.width;
        int y = image.getHeight () - (row + 1) * cellSize.height - 1;
        cell.draw (g, x, y);
      }
  }

  public Dimension getPreferredSize ()
  {
    return new Dimension (20 * cellSize.width + 1, 20 * cellSize.height + 1);
  }

  public void paintComponent (Graphics g1D)
  {
    super.paintComponent (g1D);

    if (image == null)
      createImage ();

    Graphics2D g = (Graphics2D) g1D;
    g.drawImage (image, imageOffsetX, imageOffsetY, this);

    g.setColor (Color.WHITE);
    g.drawRect (500, 29, 195, 438);
    /*
    int x = 520;
    int y = 50;
    int offsetX = x + 25;
    int offsetY = 16;
    int lineHeight = 22;

    drawTeleport (g, x, y);
    g.drawString ("Teleport", offsetX, y + offsetY);
    y += lineHeight;

    drawSpellsBlocked (g, x, y);
    g.drawString ("Spells fizzle out here", offsetX, y + offsetY);
    y += lineHeight;

    drawMonster (g, x, y);
    g.drawString ("An encounter", offsetX, y + offsetY);
    y += lineHeight;

    drawPit (g, x, y);
    g.drawString ("Pit", offsetX, y + offsetY);
    y += lineHeight;

    drawStairsUp (g, x, y);
    g.drawString ("Stairs going up", offsetX, y + offsetY);
    y += lineHeight;

    drawStairsDown (g, x, y);
    g.drawString ("Stairs going down", offsetX, y + offsetY);
    y += lineHeight;

    drawRock (g, x, y);
    g.drawString ("Rock", offsetX, y + offsetY);
    y += lineHeight;

    drawDarkness (g, x, y);
    g.drawString ("Darkness", offsetX, y + offsetY);
    y += lineHeight;

    drawChar (g, x, y, "M", Color.RED);
    g.drawString ("Message", offsetX, y + offsetY);
    y += lineHeight;

    drawElevator (g, x, y, 3);
    g.drawString ("Elevator", offsetX, y + offsetY);
    y += lineHeight;

    drawChute (g, x, y);
    g.drawString ("Chute", offsetX, y + offsetY);
    y += lineHeight;
    
    drawHotDogStand (g, x, y);
    g.drawString ("Hotdog Stand", offsetX, y + offsetY);
    y += lineHeight;

    g.drawString ("S", x + 8, y + 16);
    g.drawString ("Spinner", offsetX, y + offsetY);
    y += lineHeight;

    drawMonsterLair (g, x, y);
    g.drawString ("Monsters' Lair", offsetX, y + offsetY);
    y += lineHeight;

    drawWest (g, x, y);
    g.drawString ("Wall", offsetX, y + offsetY);
    y += lineHeight;

    g.setColor (Color.RED);
    drawWest (g, x, y);
    g.setColor (Color.WHITE);
    g.drawString ("Door", offsetX, y + offsetY);
    y += lineHeight;

    g.setColor (Color.GREEN);
    drawWest (g, x, y);
    g.setColor (Color.WHITE);
    g.drawString ("Secret door", offsetX, y + offsetY);
    y += lineHeight;
    */
  }
}
