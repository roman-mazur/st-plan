package org.mazur.stplan.gui

import java.awt.Window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import groovy.swing.SwingBuilder;

import org.mazur.stplan.QueueAlg
import org.mazur.stplan.SelectProcessorAlg 
import org.mazur.stplan.State
import org.mazur.stplan.generation.GenerationParameteres;
import org.mazur.stplan.generation.GraphGenerator;

/**
 * Main GUI builder.
 * @version: $Id$
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
final class GUIBuilder {

  /**  Swing builder. */
  private static SwingBuilder swing = new SwingBuilder()
  
  /** Title part. */
  private static String TITLE_PART = "stplan - Планування"

  /** Document tabs. */
  private static JTabbedPane mainTabs
  
  /** Editors list. */
  private static def editors = [] 
  
  /* =========== Actions ============ */
  
  /** New task graph. */
  private static def newTaskGraphAction = swing.action(
    name : "Новий граф задачі",
    shortDescription : "Новий граф задачі",
    closure : { newEditor new TaskGraphEditor(name : "noname") }
  )
  /** New system graph. */
  private static def newSystemGraphAction = swing.action(
    name : "Новий граф системи",
    shortDescription : "Новий граф системи",
    closure : { newEditor new SystemGraphEditor(name : "noname") }
  )
  /** Load task graph. */
  private static def openGraphAction = swing.action(
    name : "Завантажити",
    shortDescription : "Завантажити граф",
    closure : {
      def fc  = swing.fileChooser(dialogTitle : "Виберіть файл із графом",
          dialogType : JFileChooser.OPEN_DIALOG,
          fileSelectionMode : JFileChooser.FILES_ONLY)
      if (fc.showOpenDialog() != JFileChooser.APPROVE_OPTION) { return } 
      File f = fc.selectedFile
      def e = GraphEditor.load(new FileInputStream(f))
      e.name = f.name
      newEditor e
    }
  )
  /** Save task graph. */
  private static def saveGraphAction = swing.action(
    name : "Зберегти",
    shortDescription : "Зберегти граф",
    closure : {
      def fc  = swing.fileChooser(dialogTitle : "Виберіть файл із графом",
          fileSelectionMode : JFileChooser.FILES_ONLY)
      if (fc.showSaveDialog() != JFileChooser.APPROVE_OPTION) { return } 
      File f = fc.selectedFile
      currentEditor().serialize(new FileOutputStream(f))
    }
  )

  /** About action. */
  private static def aboutAction = swing.action(
    name : "Про програму",
    closure : {
      def d = swing.dialog(title : "Про програму", modal : true, resizable : false) {
        borderLayout()
        label(text : "Текст")
      }
      d.visible = true
      locate(d)
    }
  )

  /** Select the queue action. */
  private static def selectQueueAction = swing.action(
    name : "Вибір черги",
    shortDescription : "Вибір черги задач",
    closure : {
      def dialog
      dialog = swing.dialog(title : "Вибір черги", visible : true, pack : true, location : [200, 200]) {
        gridLayout(rows : QueueAlg.values().length, cols : 1)
        QueueAlg.values().each { alg ->
          radioButton(selected : State.INSTANCE.queueAlg == alg, action : action(
            name : alg.caption,
            closure : { State.INSTANCE.queueAlg = alg; dialog.visible = false }
          ))
        }
      }
    }
  )
  
  /** Select the algorithm action. */
  private static def selectAlgorithmAction = swing.action(
    name : "Вибір алгоритму",
    shortDescription : "Вибір алгоритму призначення",
    closure : {
      def dialog
      dialog = swing.dialog(title : "Вибір алгоритму", visible : true, pack : true) {
        gridLayout(rows : SelectProcessorAlg.values().length, cols : 1)
        SelectProcessorAlg.values().each { alg ->
          radioButton(selected : State.INSTANCE.selectProcessorAlg == alg, action : action(
            name : alg.caption,
            closure : { State.INSTANCE.selectProcessorAlg = alg; dialog.visible = false }
          ))
        }
      }
      locate dialog
    }
  )

  /** Select the queue action. */
  private static def generateGraphAction = swing.action(
    name : "Згенерувати граф задачі",
    shortDescription : "Генерація нового графа задачі",
    closure : {
      def e = new TaskGraphEditor(name : "auto")
      new ParametersDialog(
          model : new GenerationParameteres(
            minVertexWeight : 5,
            maxVertexWeight : 15,
            n : 20,
            k : 0.7
          )
      ).show {
        def waitD = swing.dialog(title : "Генерація", pack : true, visible : true) {
          borderLayout()
          label(constraints : BorderLayout.NORTH, text : "Зачекайте будь ласка...")
          progressBar(indeterminate : true)
        }
        locate(waitD)
        def genThread = new Thread(new GraphGenerator(editor : e, parameters : it)) 
        genThread.start()
        new Thread(new CRunnable({
          genThread.join()
          invokeSwing {  
            newEditor e
            waitD.visible = false
          }
        })).start()
      }
    }
  )

  private static def startModellingAction = swing.action(
    name : "Старт",
    shortDescription : "Старт моделювання",
    closure : {
      def sw = new SimWindow()
      def e = currentEditor()
      def pe = editors.find { it instanceof SystemGraphEditor }
      def sysParams = pe.systemParameters
      sw.init([e.parent, e.model], [pe.parent, pe.model], sysParams)
      sw.show()
    }
  )
  
  /** Exit action. */
  private static def exitAction = swing.action(
    name : "Вихід",
    shortDescription : "Завершення роботи",
    closure : {
      System.exit(0);
    }
  )
  
  public static void locate(final Window w) {
    Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize()
    int scrWidth = scrSize.width
    int scrHeight = scrSize.height
    int ww = w.width, hh = w.height
    w.setLocation((scrWidth - ww) >> 1,(scrHeight - hh) >> 1)
  }
  
  public static void invokeSwing(def c) {
    SwingUtilities.invokeLater new CRunnable(c)
  }
  
  /**
   * Add a new tab.
   * @param descriptor descriptor
   */
  private static void addTab(final TabDescriptor descriptor) {
    mainTabs.add descriptor.title, descriptor.panel
  }
  
  private static void newEditor(final GraphEditor e) {
    editors += e
    addTab(new TabDescriptor(title : e.name, panel : e.mainPanel))
  }
  private static GraphEditor currentEditor() { return editors[mainTabs.selectedIndex] }
  
  /**
   * Build the GUI.
   */
  static void run() {
    def f = swing.frame(title : TITLE_PART, pack : true, visible : true, defaultCloseOperation : JFrame.EXIT_ON_CLOSE) {
      borderLayout()
      
      menuBar {
        menu(text : "Файл") {
          menuItem(newTaskGraphAction)
          menuItem(newSystemGraphAction)
          menuItem(openGraphAction)
          menuItem(saveGraphAction)
        }
        menu(text : "Моделювання") {
          menuItem(startModellingAction)
          menuItem(selectQueueAction)
          menuItem(selectAlgorithmAction)
          menuItem(generateGraphAction)
        }
        menu(text : "Статистика") {
          
        }
        menu(text : "Допомога") {
          menuItem(aboutAction)
        }
        menu(action : exitAction)
      }
      
      panel() {
        borderLayout()
        mainTabs = tabbedPane(preferredSize : [800, 600])
      }
    }
    locate(f)
  }
  
  /** Hide the constructor. */
  private GUIBuilder() { }
  
}

class CRunnable implements Runnable {
  private def c
  public CRunnable(def c) { this.c = c }
  @Override public void run() { c() }
}
