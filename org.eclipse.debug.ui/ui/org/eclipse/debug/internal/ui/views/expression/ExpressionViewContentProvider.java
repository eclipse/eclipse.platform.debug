package org.eclipse.debug.internal.ui.views.expression;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.variables.VariablesViewContentProvider;
 
/**
 * Provides contents for the expression view
 */
public class ExpressionViewContentProvider extends VariablesViewContentProvider {

	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		Object[] children= null;
		try {
			if (parent instanceof IExpressionManager) {
				// do not cache parents
				return ((IExpressionManager)parent).getExpressions();
			} else if (parent instanceof IExpression) {
				IValue value= ((IExpression)parent).getValue();
				if (value != null) {
					children= value.getVariables();
				}
			} else if (parent instanceof IVariable) {
				children = ((IVariable)parent).getValue().getVariables();
			}
			if (children != null) {
				cache(parent, children);
				return children;
			}
		} catch (DebugException e) {
			DebugUIPlugin.log(e);
		}
		return new Object[0];
	}
	
	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object item) {
		if (item instanceof IExpression) {
			return DebugPlugin.getDefault().getExpressionManager();
		}
		return super.getParent(item);
	}
	
	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof IExpressionManager) {
			return ((IExpressionManager)element).hasExpressions();
		} else if (element instanceof IExpression) {
			IValue v = ((IExpression)element).getValue();
			if (v == null) {
				return false;
			}
			try {
				return v.hasVariables();
			} catch (DebugException e) {
				DebugUIPlugin.log(e);
				return false;
			}
		}
		return super.hasChildren(element);
	}	
}
