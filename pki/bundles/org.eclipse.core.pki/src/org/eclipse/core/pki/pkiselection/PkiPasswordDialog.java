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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.internal.Callback;
import org.eclipse.core.pki.auth.ContextObservable;
import org.eclipse.core.pki.util.LogUtil;

public class PkiPasswordDialog extends Dialog implements Runnable {

	protected Object result;
	protected Shell shell;
	protected Text passwdField;
	protected String pw = null;
	boolean uninitialzed = true;
	boolean isReady = true;
	ContextObservable observable=null;

	public PkiPasswordDialog(Shell parent, ContextObservable ob) {
		super(parent);
		observable=ob;
		LogUtil.logWarning("PkiPasswordDialog CONSTRUCTOR");
		// Display.getDefault().asyncExec(this);
		Display.getDefault().asyncExec(this); // main thread waits
	}
	@Override
	protected void okPressed() {
		
		LogUtil.logWarning("PkiPasswordDialog OK pressed");
	    String _passphrase=passwdField.getText();
	    LogUtil.logWarning("PkiPasswordDialog OK pressed TEXT:"+_passphrase);
	    if(_passphrase==null||_passphrase.length()==0){
	      return;
	    }
	    pw=_passphrase;
	    observable.onchange(pw);
	    setReturnCode(OK);
	    super.okPressed();
	   
	    close();
	}
	
	@Override
	protected void cancelPressed() {
		LogUtil.logWarning("PkiPasswordDialog CANCEL pressed");
		pw=null;
		super.cancelPressed();
		close();
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		// mkae shell mods here
		shell.setText("PKCS12 PKI Password Input Field");
		shell.setSize(275, 200);
		LogUtil.logWarning("PkiPasswordDialog configureShell");
	}

	public void create() {
		super.create();
		passwdField.setFocus();
		LogUtil.logWarning("PkiPasswordDialog create");
	}

	public String getPW() {
		LogUtil.logWarning("PkiPasswordDialog getPW");
		return pw;
	}

	protected Control createDialogArea(Composite parent) {
		LogUtil.logWarning("PkiPasswordDialog createDialogArea");
		initializeDialogUnits(parent);
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		// main.setSize(800, 500);
		main.setLayout(gridLayout);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		setup(main);
		Dialog.applyDialogFont(main);

		return main;

	}

	public void run() {
		LogUtil.logWarning("PkiPasswordDialog run methid top");
		if (uninitialzed) {
			uninitialzed = false;
			this.create();
			shell = createShell();
			shell.layout();
			// configureShell(shell);
			Control control = createDialogArea(getContents().getParent());

			control.pack();

			LogUtil.logWarning("PkiPasswordDialog run methid NOW OPEN");

			open();
		}
	}

	public void setup(Composite parent) {
		if ( isReady ) {
			isReady=false;
			LogUtil.logWarning("PkiPasswordDialog setup");
	
			passwdField = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
			passwdField.setEchoChar('*');
	
			Button button = new Button(parent, SWT.PUSH);
			button.setText("Show Password");
	
			Label labelInfo = new Label(parent, SWT.NONE);
			labelInfo.setText("*");
	
			button.addSelectionListener(new SelectionAdapter() {
	
				@Override
				public void widgetSelected(SelectionEvent e) {
					LogUtil.logWarning("PkiPasswordDialog WIDGET pressed");
					labelInfo.setText(passwdField.getText());
					labelInfo.pack();
				}
			});
			  
		
			
		}
	}
}
