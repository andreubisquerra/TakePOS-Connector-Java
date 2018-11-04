/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jposbox;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.System.out;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jove
 */
public class Tray {
    
    public static int TrayLoaded=0;
    
    public boolean tray(){
    if (!SystemTray.isSupported()) {
      System.out.println("SystemTray is not supported");
      return false;
    }
    System.out.println("SystemTray is supported");
    if (TrayLoaded==1) return true;

    SystemTray tray = SystemTray.getSystemTray();
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Image image = toolkit.getImage("takepos.png");

    PopupMenu menu = new PopupMenu();

    MenuItem closeItem = new MenuItem("Close");
    closeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        out.println("Closing");
        System.exit(0);
      }
    });
    menu.add(closeItem);
    
    MenuItem openItem = new MenuItem("Open");
    openItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          Frame[] frames=PosBoxFrame.getFrames();
          frames[0].setVisible(true);
          frames[0].setState(Frame.NORMAL);
      }
    });
    menu.add(openItem);
    
    TrayIcon icon = new TrayIcon(image, "jPosBox", menu);
    icon.setImageAutoSize(true);

        try {
            tray.add(icon);
        } catch (AWTException ex) {
            Logger.getLogger(Tray.class.getName()).log(Level.SEVERE, null, ex);
        }
    TrayLoaded=1;
    return true;    
    }
    
}