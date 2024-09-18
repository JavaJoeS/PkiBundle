/*******************************************************************************
 * Copyright (c) 2023 Eclipse Platform, Security Group and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Platform - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.pki.pkiselection;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.pki.auth.ContextObservable;
import org.eclipse.core.pki.auth.PasswordObserver;
import org.eclipse.core.pki.util.LogUtil;

public enum PkiPasswordInputUI {
	DO;
	PkiPasswordDialog dialog=null;
	String passwordString = "NOPASSWD";

	public String get() {
		// prompt for password
		System.out.println("PkiPasswordInputUI get method running");
		LogUtil.logWarning("PkiPasswordInputUI -  get method running"); //$NON-NLS-1$
		/*
		 * Display.getDefault().asyncExec(new Runnable() {
		 * 
		 * @Override public void run() {
		 * System.out.println("PkiPasswordInputUI RUN RUN RUN RUN"); dialog = new
		 * PkiPasswordDialog(null); if (dialog.open() == 0) { passwordString =
		 * dialog.getPW(); } } });
		 */
		//Display.getDefault().asyncExec(runner);
		ContextObservable ob = new ContextObservable();
		PasswordObserver observer = new PasswordObserver();
		ob.addObserver(observer);
		dialog=new PkiPasswordDialog(null, ob);	
		passwordString=dialog.getPW();
		passwordString = "NOPASSWD";
		return passwordString;
	}
	public void set(String pw) {
		LogUtil.logWarning("PkiPasswordInputUI -  set method running"); //$NON-NLS-1$
	}
}
