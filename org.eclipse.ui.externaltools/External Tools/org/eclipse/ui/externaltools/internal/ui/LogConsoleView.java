package org.eclipse.ui.externaltools.internal.ui;/**********************************************************************Copyright (c) 2002 IBM Corp. and others.All rights reserved.   This program and the accompanying materialsare made available under the terms of the Common Public License v0.5which accompanies this distribution, and is available athttp://www.eclipse.org/legal/cpl-v05.html Contributors:**********************************************************************/import java.net.*;import java.util.*;import java.util.Vector;import org.apache.tools.ant.Project;import org.eclipse.jface.action.*;import org.eclipse.jface.preference.PreferenceConverter;import org.eclipse.jface.resource.ImageDescriptor;import org.eclipse.jface.text.*;import org.eclipse.jface.viewers.*;import org.eclipse.swt.SWT;import org.eclipse.swt.custom.*;import org.eclipse.swt.events.*;import org.eclipse.swt.graphics.*;import org.eclipse.swt.layout.*;import org.eclipse.swt.widgets.*;import org.eclipse.ui.*;import org.eclipse.ui.externaltools.internal.core.ExternalToolsPlugin;import org.eclipse.ui.externaltools.internal.core.ToolMessages;import org.eclipse.ui.part.ViewPart;import org.eclipse.ui.texteditor.*;public class LogConsoleView extends ViewPart {	public final static String PROPERTY_PREFIX_FIND = "find_action."; // $NON-NLS-1$	private final static int SASH_WIDTH = 3; // regular width for a sash	// strings for the memento	private final static String TREE_WIDTH_PROPERTY = "tree_width";	private final static String SHOW_ONLY_SELECTED_ITEM_PROPERTY = "wasShowOnlySelectedTreeItemsTurnedOn"; // $NON-NLS-1$	private final static String SHOW_TREE_PROPERTY = "hideOrShowTreeAction"; // $NON-NLS-1$	// UI objects	private SashForm sash;	private TreeViewer treeViewer;	private TextViewer textViewer;	private Action copyAction;	private Action selectAllAction;	private Action clearOutputAction;	private Action findAction;	private Action expandTreeItemAction;	private Action showTreeAction;	private Action showSelectedItemAction;	private LogTreeLabelProvider labelprovider;	private LogTreeContentProvider contentProvider;	// lastWidth is used to store the width of the tree that the user set	private int lastTreeWidth = 30;	private boolean showOnlySelectedItems = false;	private boolean showTree = false;	/**	 * Constructor for AntConsole	 */	public LogConsoleView() {		super();		LogConsoleDocument.getInstance().registerView(this);		labelprovider = new LogTreeLabelProvider(this);		contentProvider = new LogTreeContentProvider(this);	}	/**	 * @see IViewPart	 */	public void init(IViewSite site, IMemento memento) throws PartInitException {		super.init(site, memento);		if (memento != null) {			// retrieve the values of the previous session			lastTreeWidth = memento.getInteger(TREE_WIDTH_PROPERTY).intValue();			showOnlySelectedItems = memento.getInteger(SHOW_ONLY_SELECTED_ITEM_PROPERTY).intValue() != 0;			showTree = memento.getInteger(SHOW_TREE_PROPERTY).intValue() != 0;		}	}	protected void addContributions() {		// In order for the clipboard actions to be accessible via their shortcuts		// (e.g., Ctrl-C, Ctrl-V), we *must* set a global action handler for		// each action		IActionBars actionBars = getViewSite().getActionBars();		actionBars.setGlobalActionHandler(ITextEditorActionConstants.COPY, copyAction);		actionBars.setGlobalActionHandler(ITextEditorActionConstants.FIND, findAction);		actionBars.setGlobalActionHandler(ITextEditorActionConstants.SELECT_ALL, selectAllAction);		MenuManager textViewerMgr = new MenuManager();		textViewerMgr.setRemoveAllWhenShown(true);		textViewerMgr.addMenuListener(new IMenuListener() {			public void menuAboutToShow(IMenuManager textViewerMgr) {				fillTextViewerContextMenu(textViewerMgr);			}		});		Menu textViewerMenu = textViewerMgr.createContextMenu(textViewer.getControl());		textViewer.getControl().setMenu(textViewerMenu);		MenuManager treeViewerMgr = new MenuManager();		treeViewerMgr.setRemoveAllWhenShown(true);		treeViewerMgr.addMenuListener(new IMenuListener() {			public void menuAboutToShow(IMenuManager treeViewerMgr) {				fillTreeViewerContextMenu(treeViewerMgr);			}		});		Menu treeViewerMenu = treeViewerMgr.createContextMenu(treeViewer.getControl());		treeViewer.getControl().setMenu(treeViewerMenu);		//add toolbar actions		IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();		tbm.add(showTreeAction);		tbm.add(showSelectedItemAction);		tbm.add(clearOutputAction);		getViewSite().getActionBars().updateActionBars();	}	private void createHideOrShowTreeAction() {		showTreeAction = new Action() {			public void run() {				showTree = isChecked();				if (showTree) {					// the tree is hidden, let's show it					sash.SASH_WIDTH = SASH_WIDTH;					sash.setWeights(new int[] { lastTreeWidth, 100 - lastTreeWidth });					setToolTipText(ToolMessages.getString("LogConsoleView.hideOutputStructureTree")); // $NON-NLS-1$					// the "ShowOnlySelectedElement" functionality can be turned on					showSelectedItemAction.setEnabled(true);					showSelectedItemAction.setChecked(showOnlySelectedItems);					showSelectedItemAction.run();				} else {					// let's hide the tree					sash.SASH_WIDTH = 0;					sash.setWeights(new int[] { 0, 100 });					setToolTipText(ToolMessages.getString("LogConsoleView.showOutputStructureTree")); // $NON-NLS-1$					// show the whole document					showCompleteOutput();					// disable the show selected item action					showSelectedItemAction.setEnabled(false);				}			}		};		showTreeAction.setImageDescriptor(getImageDescriptor("icons/full/clcl16/hide_show_tree.gif")); // $NON-NLS-1$		showTreeAction.setChecked(showTree);		showTreeAction.setText(ToolMessages.getString("LogConsoleView.showTree")); // $NON-NLS-1$		String tooltip = showTree ? "LogConsoleView.hideOutputStructureTree" : "LogConsoleView.showOutputStructureTree"; // $NON-NLS-1$		showTreeAction.setToolTipText(ToolMessages.getString(tooltip));	}	private boolean isTreeHidden() {		return sash.getWeights()[0] == 0;	}	public void append(String value) {		append(value, Project.MSG_INFO);	}	public void append(final String value, final int ouputLevel) {		getViewSite().getShell().getDisplay().syncExec(new Runnable() {			public void run() {				int start = getDocument().get().length();				getDocument().set(getDocument().get() + value);				setOutputLevelColor(ouputLevel, start, value.length());				if (value.length() > 0 && textViewer != null)					textViewer.revealRange(getDocument().get().length() - 1, 1);			}		});	}	private void setOutputLevelColor(int level, int start, int end) {		LogConsoleDocument.getInstance().setOutputLevelColor(level, start, end);	}	protected void copySelectionToClipboard() {		textViewer.doOperation(textViewer.COPY);	}	/**	 * Creates the actions that will appear in this view's toolbar and popup menus.	 */	protected void createActions() {		// Create the actions for the text viewer.			copyAction = new Action(ToolMessages.getString("LogConsoleView.copy")) {// $NON-NLS-1$	public void run() {				copySelectionToClipboard();			}		};			selectAllAction = new Action(ToolMessages.getString("LogConsoleView.selectAll")) {// $NON-NLS-1$	public void run() {				selectAllText();			}		};			clearOutputAction = new Action(ToolMessages.getString("LogConsoleView.clearOutput")) {// $NON-NLS-1$	public void run() {				LogConsoleDocument.getInstance().clearOutput();			}		};		clearOutputAction.setImageDescriptor(getImageDescriptor("icons/full/clcl16/clear.gif")); // $NON-NLS-1$		clearOutputAction.setToolTipText(ToolMessages.getString("LogConsoleView.clearOutput")); // $NON-NLS-1$		findAction = new FindReplaceAction(ToolMessages.getResourceBundle(), PROPERTY_PREFIX_FIND, this);		findAction.setEnabled(true);		// Create the actions for the tree viewer.		createHideOrShowTreeAction();			expandTreeItemAction = new Action(ToolMessages.getString("LogConsoleView.expandAll")) {// $NON-NLS-1$	public void run() {				OutputStructureElement selectedElement = (OutputStructureElement) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();				treeViewer.expandToLevel(selectedElement, treeViewer.ALL_LEVELS);			}		};		//create the toolbar actions		showSelectedItemAction = new Action() {			public void run() {				showOnlySelectedItems = isChecked();				if (showOnlySelectedItems) {					// we want to show only the selected tree items					showSelectedElementOnly();					// changes the labels					setToolTipText(ToolMessages.getString("LogConsoleView.showCompleteOutput")); // $NON-NLS-1$				} else {					// we want to show the whole document now					showCompleteOutput();					// changes the labels					setToolTipText(ToolMessages.getString("LogConsoleView.showSelectedElementOnly")); // $NON-NLS-1$				}			}		};		showSelectedItemAction.setImageDescriptor(getImageDescriptor("icons/full/clcl16/show_selected_text.gif")); // $NON-NLS-1$		showSelectedItemAction.setChecked(showOnlySelectedItems);		showSelectedItemAction.setText(ToolMessages.getString("LogConsoleView.showSelectedElementOnly")); // $NON-NLS-1$		String tooltip = showOnlySelectedItems ? "LogConsoleView.showCompleteOutput" : "LogConsoleView.showSelectedElementOnly"; // $NON-NLS-1$		showSelectedItemAction.setToolTipText(ToolMessages.getString(tooltip));	}	/*	 * Shows the output of the selected item only	 */	protected void showSelectedElementOnly() {		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();		if (selection.isEmpty())			textViewer.setVisibleRegion(0, 0);		else {			OutputStructureElement selectedElement = (OutputStructureElement) selection.getFirstElement();			// XXX NOTE: #setVisibleRegion doesn't keep the color information... See "1GHQC7Q: ITPUI:WIN2000 - TextViewer#setVisibleRegion doesn't take into account the colors"			textViewer.setVisibleRegion(selectedElement.getStartIndex(), selectedElement.getLength());		}	}	/*	 * Shows the output of the whole docuent, and reveals the range of the selected item	 */	protected void showCompleteOutput() {		// show all the document		textViewer.setVisibleRegion(0, getDocument().get().length());		// XXX should I have to do that? If this is not done, then the colors don't appear --> bug of #setVisibleRegion ? --> See "1GHQC7Q: ITPUI:WIN2000 - TextViewer#setVisibleRegion doesn't take into account the colors"		textViewer.getTextWidget().setStyleRanges((StyleRange[]) getStyleRanges().toArray(new StyleRange[getStyleRanges().size()]));		// and then reveal the range of the selected item		revealRangeOfSelectedItem();	}	private void revealRangeOfSelectedItem() {		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();		if (!selection.isEmpty()) {			// then show the reveal the range of the output accordingly			OutputStructureElement selectedElement = (OutputStructureElement) selection.getFirstElement();			textViewer.revealRange(selectedElement.getStartIndex(), selectedElement.getLength());			textViewer.setSelectedRange(selectedElement.getStartIndex(), selectedElement.getLength());		}	}	public void initializeTreeInput() {		getSite().getShell().getDisplay().syncExec(new Runnable() {			public void run() {				if (treeViewer != null)					treeViewer.setInput(LogConsoleDocument.getInstance().getRoot());			}		});	}	public void refreshTree() {		// if the tree is null, it means that the view hasn't been shown yet, so we don't need to refresh it.		if (treeViewer != null) {			getSite().getShell().getDisplay().syncExec(new Runnable() {				public void run() {					treeViewer.refresh();					treeViewer.expandAll();				}			});		}	}	public void updateFont() {		if (textViewer != null)			textViewer.getTextWidget().setFont(LogConsoleDocument.ANT_FONT);	}	protected void fillTextViewerContextMenu(IMenuManager manager) {		copyAction.setEnabled(textViewer.canDoOperation(textViewer.COPY));		selectAllAction.setEnabled(textViewer.canDoOperation(textViewer.SELECT_ALL));		manager.add(copyAction);		manager.add(findAction);		manager.add(selectAllAction);		manager.add(new Separator());		manager.add(showTreeAction);		manager.add(clearOutputAction);	}	protected void fillTreeViewerContextMenu(IMenuManager manager) {		manager.add(showSelectedItemAction);		manager.add(expandTreeItemAction);		manager.add(new Separator());		manager.add(showTreeAction);		manager.add(clearOutputAction);	}	public Object getAdapter(Class required) {		if (IFindReplaceTarget.class.equals(required))			return textViewer.getFindReplaceTarget();		return super.getAdapter(required);	}	private Document getDocument() {		return LogConsoleDocument.getInstance().getDocument();	}	private ArrayList getStyleRanges() {		return LogConsoleDocument.getInstance().getStyleRanges();	}	public TextViewer getTextViewer() {		return textViewer;	}	public TreeViewer getTreeViewer() {		return treeViewer;	}	protected ImageDescriptor getImageDescriptor(String relativePath) {		try {			URL installURL = ExternalToolsPlugin.getDefault().getDescriptor().getInstallURL();			URL url = new URL(installURL, relativePath);			return ImageDescriptor.createFromURL(url);		} catch (MalformedURLException e) {			return null;		}	}	protected void selectAllText() {		textViewer.doOperation(textViewer.SELECT_ALL);	}	/**	 * @see WorkbenchPart#setFocus()	 */	public void setFocus() {		sash.setFocus();	}	/**	 * @see WorkbenchPart#createPartControl(Composite)	 */	public void createPartControl(Composite parent) {		sash = new SashForm(parent, SWT.HORIZONTAL);		GridLayout sashLayout = new GridLayout();		sashLayout.marginHeight = 0;		sashLayout.marginWidth = 0;		sash.setLayout(sashLayout);		sash.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));		treeViewer = new TreeViewer(sash, SWT.V_SCROLL | SWT.H_SCROLL);		GridData treeData = new GridData(GridData.FILL_BOTH);		treeViewer.getControl().setLayoutData(treeData);		treeViewer.setContentProvider(contentProvider);		treeViewer.setLabelProvider(labelprovider);		treeViewer.setInput(LogConsoleDocument.getInstance().getRoot());		treeViewer.expandAll();		addTreeViewerListeners();		textViewer = new TextViewer(sash, SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);		GridData viewerData = new GridData(GridData.FILL_BOTH);		textViewer.getControl().setLayoutData(viewerData);		textViewer.setEditable(false);		textViewer.setDocument(getDocument());		textViewer.getTextWidget().setFont(LogConsoleDocument.ANT_FONT);		textViewer.getTextWidget().setStyleRanges((StyleRange[]) getStyleRanges().toArray(new StyleRange[getStyleRanges().size()]));		addTextViewerListeners();		// sets the ratio tree/textViewer for the sashForm		if (showTree)			sash.setWeights(new int[] { lastTreeWidth, 100 - lastTreeWidth });		else			// the "hideOrShowTree" action wasn't checked: this means that the user didn't want to have the tree			sash.setWeights(new int[] { 0, 100 });		createActions();		addContributions();	}	protected void addTreeViewerListeners() {		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {			public void selectionChanged(SelectionChangedEvent e) {				if (textViewer != null)					if (showSelectedItemAction.isChecked())						showSelectedElementOnly();					else						revealRangeOfSelectedItem();			}		});		// to remember the place of the sash when we hide the tree		treeViewer.getControl().addControlListener(new ControlAdapter() {			public void controlResized(ControlEvent e) {				if (treeViewer.getControl().getSize().x != 0)					// we don't want the width to be stored when the tree is getting hidden 					// (because it equals zero and we want to have the previous value)					lastTreeWidth = new Float((float) treeViewer.getControl().getSize().x / sash.getSize().x * 100).intValue();			}		});	}	protected void addTextViewerListeners() {		textViewer.getTextWidget().addMouseListener(new MouseAdapter() {			public void mouseDown(MouseEvent e) {				if (!showSelectedItemAction.isChecked())					selectTreeItem(textViewer.getTextWidget().getCaretOffset());			}		});		textViewer.getTextWidget().addKeyListener(new KeyAdapter() {			public void keyReleased(KeyEvent e) {				if (!showSelectedItemAction.isChecked())					selectTreeItem(textViewer.getTextWidget().getCaretOffset());			}		});	}	protected void selectTreeItem(int caretPosition) {		// tree.getTree().getItems()[1] returns the root of the tree that contains the project		// it may not exist if there is no output (in this case, there is only one item: the "Ant Script" one)		if (treeViewer.getTree().getItems().length != 1) {			TreeItem itemToSelect = null;			if (findItem(treeViewer.getTree().getItems()[0], caretPosition) != null)				// the first item is the good one				itemToSelect = treeViewer.getTree().getItems()[0];			else				// the first item is not the good one, let's check the second one and its children				itemToSelect = findItem(treeViewer.getTree().getItems()[1], caretPosition);			treeViewer.getTree().setSelection(new TreeItem[] { itemToSelect });		}	}	private TreeItem findItem(TreeItem item, int position) {		if (!(((OutputStructureElement) item.getData()).getStartIndex() <= position && ((OutputStructureElement) item.getData()).getEndIndex() > position))			return null;		for (int i = 0; i < item.getItems().length; i++) {			TreeItem child = findItem(item.getItems()[i], position);			if (child != null)				return child;		}		return item;	}	/**	 * @see IViewPart	 */	public void saveState(IMemento memento) {		memento.putInteger(TREE_WIDTH_PROPERTY, lastTreeWidth);		// it is not possible to put a boolean in a memento, so we use integers		memento.putInteger(SHOW_ONLY_SELECTED_ITEM_PROPERTY, showOnlySelectedItems ? 1 : 0);		memento.putInteger(SHOW_TREE_PROPERTY, showTree ? 1 : 0);	}	public void dispose() {		LogConsoleDocument.getInstance().unregisterView(this);		super.dispose();	}}