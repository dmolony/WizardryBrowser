package com.bytezone.wizardry;

import java.io.File;

public class SavedStateChecker extends Thread
{
  private File file;
  private long lastModified;
  private ScenarioBrowser owner;

  public SavedStateChecker(ScenarioBrowser owner, File f)
  {
    this.file = f;
    this.owner = owner;
    lastModified = file.lastModified ();
  }

  public void run ()
  {
    while (true)
    {
      long lm = file.lastModified ();
      if (lastModified != lm)
        owner.refreshSavedState ();
      lastModified = lm;
      try
      {
        Thread.sleep (1000);
      }
      catch (InterruptedException ie)
      {
        return;
      }
    }
  }
}