package com.bytezone.wizardry;

public class StringBuilder2
{
  StringBuilder sb;

  public StringBuilder2 ()
  {
    sb = new StringBuilder ();
  }

  public StringBuilder2 (String text)
  {
    sb = new StringBuilder (text);
  }

  public void append (String text)
  {
    sb.append (text);
  }

  public void tabTo (int tabPos)
  {
    while (sb.length () < tabPos)
      sb.append (" ");
  }

  public void appendNumber (int number, int tabPos)
  {
    String num = "" + number;
    tabTo (tabPos - num.length ());
    sb.append (num);
  }

  public int length ()
  {
    return sb.length ();
  }

  @Override
  public String toString ()
  {
    return sb.toString ();
  }
}
