package com.bytezone.wizardry;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

public class BasicMazeRoom implements MazeRoom
{
  private List items;
  private Direction walls;
  private Direction visibleDoors;
  private Direction secretDoors;
  private boolean isDark;
  private int height;
  private int width;
  
  public BasicMazeRoom (Direction walls, Direction doors, Direction secretDoors)
  {
    this.visibleDoors = doors;
    this.secretDoors = secretDoors;
    this.walls = walls;
  }
  
  public void draw (Graphics g, Point location)
  {
    g.setColor (Color.GREEN);
    if (secretDoors.north)
      g.drawLine (location.x, location.y, location.x, location.y + width);
  }
}

class Direction
{
  public boolean north;
  public boolean south;
  public boolean east;
  public boolean west;
}
