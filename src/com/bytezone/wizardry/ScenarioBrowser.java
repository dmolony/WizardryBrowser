package com.bytezone.wizardry;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import com.bytezone.diskbrowser.gui.ImagePanel;
import com.bytezone.diskbrowser.utilities.HexFormatter;
import com.bytezone.wizardry.Spell.SpellType;

public class ScenarioBrowser extends JFrame
{
  private JScrollPane monsterScrollPane;
  private JScrollPane itemScrollPane;
  private JTabbedPane mazeTabbedPane;
  private JTabbedPane priestSpellsTabbedPane;
  private JTabbedPane mageSpellsTabbedPane;
  private JTabbedPane characterTabbedPane;
  private JTabbedPane mainTabbedPane;
  private JPanel imagePane;
  private JFrame monsterFrame;

  ArrayList<Item> items;
  ArrayList<Character> characters;
  ArrayList<Spell> spells;
  ArrayList<Monster> monsters;
  ArrayList<Message> messages;
  ArrayList<BufferedImage> images;
  ArrayList<MazeDataModel> mazes;
  long[][] experienceLevels;

  private ComponentFactory factory;
  private File savedStateFile;
  private AppleDump saveState;
  private Preferences prefs = Preferences.userNodeForPackage (this.getClass ());
  private static String windowTitle = "Wizardry Scenario Browser";
  private static String VERSION = "0.984";
  private JMenu menuDisplay;
  private Font baseFont = new Font ("Lucida Console", Font.PLAIN, 13);
  //  private Font baseFont = new Font ("Menlo", Font.PLAIN, 13);
  private MonsterPanel monsterPanel;
  private JTable monsterTable;
  private JTable itemTable;
  private JTable characterTable;
  private JTextArea cheatText;

  public ScenarioBrowser ()
  {
    super (windowTitle);

    mainTabbedPane = new JTabbedPane ();
    this.add (mainTabbedPane, BorderLayout.CENTER);

    characterTabbedPane = new JTabbedPane (SwingConstants.BOTTOM);
    mainTabbedPane.addTab ("Gilgamesh's Tavern", characterTabbedPane);

    mazeTabbedPane = new JTabbedPane (SwingConstants.BOTTOM);
    mainTabbedPane.addTab ("Maze", mazeTabbedPane);

    monsterScrollPane = new JScrollPane ();
    mainTabbedPane.addTab ("Monsters", monsterScrollPane);

    itemScrollPane = new JScrollPane ();
    mainTabbedPane.addTab ("Boltac's Trading Post", itemScrollPane);

    priestSpellsTabbedPane = new JTabbedPane (SwingConstants.BOTTOM);
    mainTabbedPane.addTab ("Priest Spells", priestSpellsTabbedPane);

    mageSpellsTabbedPane = new JTabbedPane (SwingConstants.BOTTOM);
    mainTabbedPane.addTab ("Mage Spells", mageSpellsTabbedPane);

    imagePane = new JPanel ();
    imagePane.setLayout (new GridLayout (4, 5));
    mainTabbedPane.addTab ("Images", imagePane);

    cheatText = new JTextArea ();
    cheatText.setFont (baseFont);
    cheatText.setEditable (false);
    cheatText.setBorder (BorderFactory.createEmptyBorder (10, 10, 10, 10));
    mainTabbedPane.addTab ("Cheat", new JScrollPane (cheatText));

    String fileName = prefs.get ("Last disk used", "");
    int currentPane = prefs.getInt ("Current pane", 0);
    if (currentPane >= mainTabbedPane.getComponentCount ())
      currentPane = 0;
    mainTabbedPane.setSelectedIndex (currentPane);

    monsterFrame = new JFrame ("Monsters");
    monsterPanel = new MonsterPanel ();
    monsterFrame.add (monsterPanel, SwingConstants.CENTER);
    monsterFrame.setResizable (false);
    monsterFrame.pack ();

    setMenus ();
    mainTabbedPane.setPreferredSize (new Dimension (740, 559));

    File f = new File (fileName);
    if (f.exists ())
    {
      diskSelected (f);
      characterTabbedPane.setSelectedIndex (prefs.getInt ("Current character", 0));
      int currentMaze = prefs.getInt ("Current maze", 0);
      if (currentMaze >= mazeTabbedPane.getTabCount ())
        currentMaze = 0;
      mazeTabbedPane.setSelectedIndex (currentMaze);
      priestSpellsTabbedPane.setSelectedIndex (prefs.getInt ("Current priest spell", 0));
      mageSpellsTabbedPane.setSelectedIndex (prefs.getInt ("Current mage spell", 0));

      savedStateFile = new File (f.getParent () + "\\SaveState.aws");
      if (savedStateFile.exists ())
      {
        refreshSavedState ();
      }
      SavedStateChecker ssc = new SavedStateChecker (this, savedStateFile);
      ssc.start ();
    }
    if (savedStateFile == null || !savedStateFile.exists ())
      cheatText.setText (
          "Use the SaveState feature of AppleWin to " + "save the current state of the game.\n"
              + "Save the file using the default file name (SaveState.aws)"
              + " and in the same folder\nas your scenario file.");
    pack ();

    int x = prefs.getInt ("Window1x", 0);
    int y = prefs.getInt ("Window1y", 0);
    int w = prefs.getInt ("Window1w", 0);
    int h = prefs.getInt ("Window1h", 0);
    if (x == 0 || y == 0 || w == 0 || h == 0)
      centre ();
    else
    {
      this.setLocation (x, y);
      this.setSize (w, h);
    }

    x = prefs.getInt ("Window2x", 0);
    y = prefs.getInt ("Window2y", 0);
    monsterFrame.setLocation (x, y);
    setVisible (true);

    addWindowListener (new WindowAdapter ()
    {
      @Override
      public void windowClosing (WindowEvent e)
      {
        savePreferences ();
        System.exit (0);
      }
    });
  }

  public void refreshSavedState ()
  {
    saveState = new AppleDump (savedStateFile);
    saveState.setItems (items);
    saveState.setCharacters (characters);
    cheatText.setText ("Location : " + saveState.getLocation () + "\n\nParty:\n\n"
        + saveState.getCharacters () + "\n\nMonsters:\n\n" + saveState.getBiffo ());
  }

  private void setMenus ()
  {
    JMenuBar menuBar = new JMenuBar ();
    this.setJMenuBar (menuBar);

    JMenu menuFile = new JMenu ("File");
    menuDisplay = new JMenu ("Display");
    //    JMenu menuGUI = new JMenu ("GUI");
    JMenu menuHelp = new JMenu ("Help");

    JMenuItem menuItemOpen = new JMenuItem ("Open...");
    JMenuItem menuItemPrint = new JMenuItem ("Print...");
    JMenuItem menuItemRefresh = new JMenuItem ("Refresh");
    JMenuItem menuItemAbout = new JMenuItem ("About");

    //    ButtonGroup guiGroup = new ButtonGroup ();

    //    for (int i = 0; i < installedLAF.length; i++)
    //    {
    //      String guiName = installedLAF[i].getName ();
    //      String currentGUI = UIManager.getLookAndFeel ().getName ();
    //      JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem (guiName);
    //      guiGroup.add (menuItem);
    //      menuGUI.add (menuItem);
    //
    //      if (guiName.equals (currentGUI))
    //        menuItem.setSelected (true);
    //    }

    menuBar.add (menuFile);
    menuBar.add (menuHelp);

    menuFile.add (menuItemOpen);
    menuFile.add (menuItemPrint);

    menuHelp.add (menuItemAbout);

    menuItemOpen.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        JFileChooser chooser = new JFileChooser ();
        chooser.setDialogTitle ("Select disk image");
        //        chooser.addChoosableFileFilter (new DiskImageFilter ());

        if (factory != null)
          chooser.setCurrentDirectory (factory.getFile ());

        int result = chooser.showOpenDialog (ScenarioBrowser.this);
        File f = chooser.getSelectedFile ();
        if (result == JFileChooser.APPROVE_OPTION && f != null)
        {
          monsterTable = null; // force a complete rebuild
          diskSelected (f);
        }
      }
    });

    menuItemPrint.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        Printable printable = null;
        if (mainTabbedPane.getSelectedComponent () == monsterScrollPane)
          printable = monsterTable.getPrintable (JTable.PrintMode.FIT_WIDTH,
              new MessageFormat ("Monsters"), new MessageFormat ("Page - {0}"));
        if (mainTabbedPane.getSelectedComponent () == itemScrollPane)
          printable = itemTable.getPrintable (JTable.PrintMode.FIT_WIDTH,
              new MessageFormat ("Items"), new MessageFormat ("Page - {0}"));
        if (mainTabbedPane.getSelectedComponent () == characterTabbedPane)
          printable = characterTable.getPrintable (JTable.PrintMode.FIT_WIDTH,
              new MessageFormat ("Characters"), new MessageFormat ("Page - {0}"));

        if (printable == null)
          return;

        // fetch a PrinterJob
        PrinterJob job = PrinterJob.getPrinterJob ();

        // set the Printable on the PrinterJob
        job.setPrintable (printable);

        // create an attribute set to store attributes from the print dialog
        PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet ();

        // display a print dialog and record whether or not the user cancels it
        boolean printAccepted = job.printDialog (attr);

        // if the user didn't cancel the dialog
        if (printAccepted)
        {
          // do the printing (may need to handle PrinterException)
          try
          {
            job.print (attr);
          }
          catch (PrinterException pe)
          {
            System.out.println (pe);
          }
        }

      }
    });

    menuItemRefresh.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        if (factory != null)
          diskSelected (factory.getFile ());
      }
    });

    menuItemAbout.addActionListener (new ActionListener ()
    {
      @Override
      public void actionPerformed (ActionEvent e)
      {
        JOptionPane.showMessageDialog (ScenarioBrowser.this,
            "Wizardry Scenario Browser - version " + VERSION + "\nCopyright Denis Molony 2004"
                + "\nContact : apple@bytezone.com" + "\nJava level : "
                + System.getProperty ("java.version"),
            "About ScenarioBrowser", JOptionPane.INFORMATION_MESSAGE);
      }
    });
  }

  private void diskSelected (File file)
  {
    try
    {
      factory = new ComponentFactory (file);
    }
    catch (NotAnAppleDisk e)
    {
      JOptionPane.showMessageDialog (this, "Not an apple disk");
      return;
    }
    catch (NotAWizardryDisk e)
    {
      JOptionPane.showMessageDialog (this, "Not a Wizardry scenario disk");
      return;
    }

    menuDisplay.setEnabled (true);
    setTitle (windowTitle + " - " + file.getName ());

    items = factory.getItems ();
    characters = factory.getCharacters ();
    spells = factory.getSpells ();
    monsters = factory.getMonsters ();
    messages = factory.getMessages ();
    images = factory.getImages ();
    mazes = factory.getLevels ();

    monsterPanel.setImages (images);
    monsterPanel.setMonsters (monsters);

    if (false)
    {
      for (Monster m : monsters)
      {
        StringBuilder line = new StringBuilder (m.getRealName ());
        while (line.length () < 20)
          line.append (" ");
        for (int i = 132; i < 140; i += 2)
          line.append (HexFormatter.format2 (m.data[i]) + " ");
        for (int i = 150; i < 158; i += 2)
          line.append (HexFormatter.format2 (m.data[i]) + " ");
        line.append (": " + m.getExperience ());
        System.out.println (line.toString ());
      }
    }

    setCharacterPane ();
    setItemPane ();

    if (monsterTable == null)
    {
      setMonsterPane ();
      setSpellPane (priestSpellsTabbedPane, SpellType.PRIEST);
      setSpellPane (mageSpellsTabbedPane, SpellType.MAGE);
      setImagePane ();
      setMazePane ();
    }
  }

  private void setCharacterPane ()
  {
    int currentTab = characterTabbedPane.getSelectedIndex ();
    characterTabbedPane.removeAll ();

    AbstractTableModel atm = new CharacterTableModel (characters);
    final SortFilterModel sortFilter = new SortFilterModel (atm);

    characterTable = new JTable (sortFilter);
    JTableHeader th = characterTable.getTableHeader ();

    th.addMouseListener (new MouseAdapter ()
    {
      @Override
      public void mouseClicked (MouseEvent event)
      {
        if (event.getClickCount () < 2)
          return;
        int tableColumn = characterTable.columnAtPoint (event.getPoint ());
        int modelColumn = characterTable.convertColumnIndexToModel (tableColumn);
        if (modelColumn <= 3 || modelColumn == 5)
          sortFilter.sort (modelColumn, SortFilterModel.ASCENDING);
        else
          sortFilter.sort (modelColumn, SortFilterModel.DESCENDING);
      }
    });

    characterTable.setFont (new Font ("Verdana", Font.PLAIN, 12));
    characterTable.setShowGrid (true);
    characterTable.setShowVerticalLines (false);
    TableColumnModel model = characterTable.getColumnModel ();

    // set desired column widths
    model.getColumn (0).setMinWidth (100); // name
    model.getColumn (4).setMaxWidth (35); // level
    model.getColumn (5).setMaxWidth (35); // a/c
    model.getColumn (6).setMaxWidth (35); // hits
    model.getColumn (8).setMinWidth (80); // exp

    model.getColumn (10).setPreferredWidth (30);

    // right justify the numeric columns
    DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer ();
    renderRight.setHorizontalAlignment (SwingConstants.RIGHT);
    int[] columns = { 4, 5, 6, 10 };
    for (int col : columns)
      model.getColumn (col).setCellRenderer (renderRight);

    // renderer for right-justified, formatted numbers
    NumberRenderer nr = new NumberRenderer ();
    nr.setHorizontalAlignment (SwingConstants.RIGHT);
    for (int col = 7; col < 10; col++)
      model.getColumn (col).setCellRenderer (nr);

    JPanel base = new JPanel ();
    LayoutManager lm = new BorderLayout ();
    base.setLayout (lm);
    JScrollPane sp = new JScrollPane (characterTable);
    base.add (sp, BorderLayout.NORTH);
    characterTabbedPane.addTab ("roster", base);
    characterTabbedPane.setSelectedIndex (0);

    SummaryPane summary = new SummaryPane ();
    summary.setCharacters (characters);
    summary.setItems (items);
    base.add (summary, BorderLayout.CENTER);

    // add the character's tabs
    if (characters != null)
    {
      Iterator<Character> i = characters.iterator ();
      while (i.hasNext ())
      {
        Character character = i.next ();
        characterTabbedPane.add (character.getName ().toLowerCase (),
            new JScrollPane (new CharacterPanel (character)));
      }
      if (currentTab >= characterTabbedPane.getTabCount ())
        currentTab = -1;
      characterTabbedPane.setSelectedIndex (currentTab < 0 ? 0 : currentTab);
    }
  }

  private void setImagePane ()
  {
    imagePane.removeAll ();
    for (BufferedImage image : images)
    {
      ImagePanel imagePanel = new ImagePanel ();
      imagePanel.setImage (image);
      imagePanel.setBackground (Color.BLACK);
      imagePanel.setScale (2);
      imagePane.add (imagePanel);

      //      HiResPanel p = new HiResPanel (image);
      //      p.setBackground (Color.BLACK);
      //      p.setScale (2);
      //      imagePane.add (p);
    }
  }

  private void setMazePane ()
  {
    int saveTab = mazeTabbedPane.getSelectedIndex ();
    mazeTabbedPane.removeAll ();

    if (mazes == null)
      return;

    if (mazes.size () == 0)
    {
      System.out.println ("No mazes found");
      return;
    }

    for (MazeDataModel model : mazes)
    {
      MazePane p = new MazePane (model);
      mazeTabbedPane.addTab (" Level " + (model.getLevel ()) + " ", new JScrollPane (p));
      //      p.setMessages (messages);
      //      p.setMonsters (monsters);
      //      p.setItems (items);
    }

    mazeTabbedPane.setSelectedIndex (saveTab < 0 ? 0 : saveTab);
  }

  private void setMonsterPane ()
  {
    AbstractTableModel mtm = new MonsterTableModel (monsters);
    final SortFilterModel sortFilter = new SortFilterModel (mtm);

    monsterTable = new JTable (sortFilter);
    JTableHeader th = monsterTable.getTableHeader ();
    monsterTable.setFont (new Font ("Verdana", Font.PLAIN, 12));
    monsterTable.setShowGrid (true);
    monsterTable.setShowVerticalLines (false);

    monsterTable.getSelectionModel ().addListSelectionListener (new ListSelectionListener ()
    {
      @Override
      public void valueChanged (ListSelectionEvent e)
      {
        DefaultListSelectionModel lsl = (DefaultListSelectionModel) e.getSource ();
        if (e.getValueIsAdjusting () || lsl.isSelectionEmpty ())
          return;
        int row = monsterTable.getSelectedRow ();
        monsterPanel.setMonster (sortFilter.translateRow (row));
      }
    });

    monsterTable.addMouseListener (new MouseAdapter ()
    {
      @Override
      public void mouseClicked (MouseEvent e)
      {
        if (e.getClickCount () == 2)
          monsterFrame.setVisible (true);
      }
    });

    TableColumnModel model = monsterTable.getColumnModel ();

    model.getColumn (0).setMinWidth (90); // name
    model.getColumn (1).setMinWidth (90); // real name
    model.getColumn (7).setMinWidth (120); // damage

    th.addMouseListener (new MouseAdapter ()
    {
      @Override
      public void mouseClicked (MouseEvent event)
      {
        if (event.getClickCount () < 2)
          return;
        int tableColumn = monsterTable.columnAtPoint (event.getPoint ());
        int modelColumn = monsterTable.convertColumnIndexToModel (tableColumn);
        if (modelColumn <= 3)
          sortFilter.sort (modelColumn, SortFilterModel.ASCENDING);
        else
          sortFilter.sort (modelColumn, SortFilterModel.DESCENDING);
      }
    });

    // renderer for right-justified, formatted numbers
    NumberRenderer nr = new NumberRenderer ();
    nr.setHorizontalAlignment (SwingConstants.RIGHT);

    monsterTable.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);

    int[] columns = { 2, 4 };
    for (int col : columns)
    {
      model.getColumn (col).setCellRenderer (nr);
      model.getColumn (col).setMaxWidth (30);
    }

    int[] columns2 = { 5, 6 };
    for (int col : columns2)
    {
      // model.getColumn (col).setCellRenderer (nr);
      model.getColumn (col).setMaxWidth (70);
    }

    int[] columns3 = { 3 };
    for (int col : columns3)
    {
      model.getColumn (col).setCellRenderer (nr);
      model.getColumn (col).setMaxWidth (50);
      model.getColumn (col).setMinWidth (50);
    }

    ListSelectionModel lsm = monsterTable.getSelectionModel ();
    int x = prefs.getInt ("Monster selected", 0);
    if (x < monsters.size ())
      lsm.setSelectionInterval (x, x);

    monsterScrollPane.setViewportView (monsterTable);
  }

  private void setItemPane ()
  {
    AbstractTableModel itm = new ItemTableModel (items);
    final SortFilterModel sortFilter = new SortFilterModel (itm);

    itemTable = new JTable (sortFilter);
    JTableHeader th = itemTable.getTableHeader ();
    itemTable.setFont (new Font ("Verdana", Font.PLAIN, 12));
    itemTable.setShowGrid (true);
    itemTable.setShowVerticalLines (false);
    TableColumnModel model = itemTable.getColumnModel ();

    model.getColumn (0).setMinWidth (120);      // name
    model.getColumn (1).setMinWidth (70);       // name
    model.getColumn (8).setMaxWidth (60);       // damage
    model.getColumn (9).setMaxWidth (80);       // used by
    model.getColumn (7).setMaxWidth (40);       // speed
    model.getColumn (1).setMaxWidth (150);      // type
    model.getColumn (2).setMaxWidth (30);       // cursed

    th.addMouseListener (new MouseAdapter ()
    {
      @Override
      public void mouseClicked (MouseEvent event)
      {
        if (event.getClickCount () < 2)
          return;
        int tableColumn = itemTable.columnAtPoint (event.getPoint ());
        int modelColumn = itemTable.convertColumnIndexToModel (tableColumn);
        if (modelColumn <= 2)
          sortFilter.sort (modelColumn, SortFilterModel.ASCENDING);
        else
          sortFilter.sort (modelColumn, SortFilterModel.DESCENDING);
      }
    });

    // renderer for right-justified, formatted numbers
    NumberRenderer nr = new NumberRenderer ();
    nr.setHorizontalAlignment (SwingConstants.RIGHT);
    DefaultTableCellRenderer cr = new DefaultTableCellRenderer ();
    cr.setHorizontalAlignment (SwingConstants.CENTER);

    int[] columns = { 4, 5, 6 };
    for (int col : columns)
    {
      model.getColumn (col).setCellRenderer (nr);
      model.getColumn (col).setMaxWidth (30);
    }
    int[] columns2 = { 3 };
    for (int col : columns2)
      model.getColumn (col).setCellRenderer (nr);
    int[] columns3 = { 2, 7 };
    for (int col : columns3)
      model.getColumn (col).setCellRenderer (cr);

    itemScrollPane.setViewportView (itemTable);
  }

  private void setSpellPane (JTabbedPane spellPane, SpellType spellType)
  {
    int currentTab = spellPane.getSelectedIndex ();
    spellPane.removeAll ();

    for (Spell spell : spells)
    {
      if (spell.getType () != spellType)
        continue;
      StringBuilder text = new StringBuilder ();

      setHTMLHeader (text);
      text.append (spell.toHTMLTable () + "\n");
      setHTMLTrailer (text);

      JEditorPane ep = new JEditorPane ();
      ep.setContentType ("text/html");
      ep.setEditable (false);
      ep.setText (text.toString ());
      spellPane.add (spell.getName ().toLowerCase (), ep);
    }
    spellPane.setSelectedIndex (currentTab < 0 ? 0 : currentTab);
  }

  private void setHTMLHeader (StringBuilder text)
  {
    text.append ("<html>\n<head>\n");
    // text.append ("<title>Spelletjes</title>\n");

    // text.append ("<style type=\"text/css\">\n");
    // text.append ("body {font: Verdana}\n");
    // text.append ("</style>\n");

    // text.append ("</head>\n");
    // text.append ("<body>\n");
  }

  private void setHTMLTrailer (StringBuilder text)
  {
    // text.append ("</body></html>");
    text.append ("</html>");
  }

  private void savePreferences ()
  {
    if (factory != null)
    {
      prefs.put ("Last disk used", factory.getFile ().getAbsolutePath ());
      //      prefs.put ("GUI", UIManager.getLookAndFeel ().getName ());
      prefs.putInt ("Current pane", mainTabbedPane.getSelectedIndex ());
      prefs.putInt ("Current character", characterTabbedPane.getSelectedIndex ());
      prefs.putInt ("Current maze", mazeTabbedPane.getSelectedIndex ());
      prefs.putInt ("Current priest spell", priestSpellsTabbedPane.getSelectedIndex ());
      prefs.putInt ("Current mage spell", mageSpellsTabbedPane.getSelectedIndex ());
      prefs.putInt ("Window1x", this.getLocation ().x);
      prefs.putInt ("Window1y", this.getLocation ().y);
      prefs.putInt ("Window1h", this.getHeight ());
      prefs.putInt ("Window1w", this.getWidth ());
      prefs.putInt ("Window2x", monsterFrame.getLocation ().x);
      prefs.putInt ("Window2y", monsterFrame.getLocation ().y);
      prefs.putInt ("Monster selected", monsterPanel.getCurrentMonster ());
    }
  }

  private void centre ()
  {
    Dimension screen = Toolkit.getDefaultToolkit ().getScreenSize ();
    Dimension window = this.getSize ();
    setLocation ((screen.width - window.width) / 2, (screen.height - window.height) / 3);
  }

  public static void main (String[] args)
  {
    try
    {
      //      UIManager.setLookAndFeel (new SyntheticaStandardLookAndFeel ());
      UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName ());
    }
    catch (Exception e)
    {
      e.printStackTrace ();
    }
    new ScenarioBrowser ();
  }
}