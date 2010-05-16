package org.mazur.stplan

import java.awt.Dimension;

import javax.swing.UIManager;

import org.mazur.stplan.gui.GUIBuilder;

import com.jgoodies.looks.FontSizeHints;
import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;

/**
 * Application starter.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */

UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE)
Options.setGlobalFontSizeHints(FontSizeHints.MIXED)
Options.setDefaultIconSize(new Dimension(18, 18))

try {
  UIManager.setLookAndFeel(LookUtils.IS_OS_WINDOWS_XP 
      ? Options.crossPlatformLookAndFeelClassName
      : Options.systemLookAndFeelClassName)
} catch (Exception e) {
  println ("Can't set look & feel:" + e)
}

GUIBuilder.run()
