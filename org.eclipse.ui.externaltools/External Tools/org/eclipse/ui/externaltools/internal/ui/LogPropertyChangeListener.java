package org.eclipse.ui.externaltools.internal.ui;/**********************************************************************Copyright (c) 2002 IBM Corp. and others.All rights reserved.   This program and the accompanying materialsare made available under the terms of the Common Public License v0.5which accompanies this distribution, and is available athttp://www.eclipse.org/legal/cpl-v05.html Contributors:**********************************************************************/import java.util.Iterator;import org.eclipse.jface.util.IPropertyChangeListener;import org.eclipse.jface.util.PropertyChangeEvent;import org.eclipse.swt.graphics.Color;import org.eclipse.swt.graphics.Font;import org.eclipse.swt.graphics.FontData;import org.eclipse.swt.widgets.Display;import org.eclipse.ui.externaltools.internal.core.IPreferenceConstants;/** * Property change listener */public class LogPropertyChangeListener implements IPropertyChangeListener {	// unique instance	private static LogPropertyChangeListener instance = new LogPropertyChangeListener();	// private constructor to ensure the singleton	private LogPropertyChangeListener() {	}		// access to the singleton	public static LogPropertyChangeListener getInstance() {		return instance;	}		/**	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)	 */	public void propertyChange(PropertyChangeEvent event) {			String propertyName= event.getProperty();					if (propertyName.equals(IPreferenceConstants.CONSOLE_ERROR_RGB)) {				Color temp = LogConsoleDocument.ERROR_COLOR;				LogConsoleDocument.ERROR_COLOR = ToolsPreferencePage.getPreferenceColor(IPreferenceConstants.CONSOLE_ERROR_RGB);				temp.dispose();				clearOutput();			} else if (propertyName.equals(IPreferenceConstants.CONSOLE_WARNING_RGB)) {				Color temp = LogConsoleDocument.WARN_COLOR;				LogConsoleDocument.WARN_COLOR = ToolsPreferencePage.getPreferenceColor(IPreferenceConstants.CONSOLE_WARNING_RGB);				temp.dispose();				clearOutput();			} else if (propertyName.equals(IPreferenceConstants.CONSOLE_INFO_RGB)) {				Color temp = LogConsoleDocument.INFO_COLOR;				LogConsoleDocument.INFO_COLOR = ToolsPreferencePage.getPreferenceColor(IPreferenceConstants.CONSOLE_INFO_RGB);				temp.dispose();				clearOutput();			} else if (propertyName.equals(IPreferenceConstants.CONSOLE_VERBOSE_RGB)) {				Color temp = LogConsoleDocument.VERBOSE_COLOR;				LogConsoleDocument.VERBOSE_COLOR = ToolsPreferencePage.getPreferenceColor(IPreferenceConstants.CONSOLE_VERBOSE_RGB);				temp.dispose();				clearOutput();			} else if (propertyName.equals(IPreferenceConstants.CONSOLE_DEBUG_RGB)) {				Color temp = LogConsoleDocument.DEBUG_COLOR;				LogConsoleDocument.DEBUG_COLOR = ToolsPreferencePage.getPreferenceColor(IPreferenceConstants.CONSOLE_DEBUG_RGB);				temp.dispose();				clearOutput();			} else if (propertyName.equals(IPreferenceConstants.CONSOLE_FONT)) {				FontData data= ToolsPreferencePage.getConsoleFontData();				Font temp= LogConsoleDocument.ANT_FONT;				LogConsoleDocument.ANT_FONT = new Font(Display.getCurrent(), data);				temp.dispose();				updateFont();				} else				return;	}	/**	 * Clears the output of all the consoles	 */	private void clearOutput() {		LogConsoleDocument.getInstance().clearOutput();	}		/**	 * Updates teh font in all the consoles	 */	private void updateFont() {		for (Iterator iterator = LogConsoleDocument.getInstance().getViews().iterator(); iterator.hasNext();)			 ((LogConsoleView) iterator.next()).updateFont();	}}