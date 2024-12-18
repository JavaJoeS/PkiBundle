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
package org.eclipse.ui.pki.preferences;

import java.io.PrintStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Optional;

import org.eclipse.core.pki.AuthenticationBase;
import org.eclipse.core.pki.auth.PKIState;
import org.eclipse.core.pki.pkiselection.PKI;
import org.eclipse.core.pki.pkiselection.PKIProperties;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.pki.pkcs.VendorImplementation;
//import org.eclipse.ui.pki.pkiselection.PKCSpick;
import org.eclipse.ui.pki.preferences.ChangedPressedFieldEditorStatus;
import org.eclipse.ui.pki.util.PKISecureStorage;
import org.eclipse.ui.pki.wizard.TrustStoreSecureStorage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;
import org.eclipse.ui.pki.AuthenticationPlugin;


/**
 * @since 1.3
 */
public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private StringFieldEditor pkcs11Certificate;
	private StringFieldEditor pkiCertificate;
	private StringFieldEditor trustStoreJKS;
	private StringFieldEditor securityProvider;
	private FileFieldEditor configurationLocationFile;
	private PKI previousPKI;
	private Composite yourSibling=null;
	private String pkiType="NONE";
	private boolean isGoodConfig=false;
	public boolean exitView=false;
	private Group groups = null;
	private String pkcs11Label="Smartcard location Configuration";
	private String pkcs12Label="PKCS12 Certificate Installation location";
	PKISecureStorage pkiSecureStorage = null;
//	Display display = Display.getCurrent();
//	Color blue = display.getSystemColor(SWT.COLOR_BLUE);
//	Color red = display.getSystemColor(SWT.COLOR_RED);
//	Color green = display.getSystemColor(SWT.COLOR_GREEN);
//	Color yellow = display.getSystemColor(SWT.COLOR_YELLOW);
//	Color black = display.getSystemColor(SWT.COLOR_BLACK);

	public PreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		noDefaultButton();
		setPreferenceStore(AuthenticationPlugin.getDefault().getPreferenceStore());
		setDescription("PKI Preferences:");
		//printoutStore();
		//listProviders();
		previousPKI = this.previousPKI();
		pkiSecureStorage = new PKISecureStorage();
	}
	@Override
	public void createControl(Composite parent) {
		super.createControl( parent );
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getShell(), 
	            "org.eclipse.pki.preferences");
	}
	@Override
    protected Control createContents(Composite parent) {
		String propertyAddedProvider=null;
		// help to debug
		//yourParent.getChildren()[0].setBackground(yellow);
		
		yourSibling = new Composite(parent, SWT.NONE);
		
		//  HELP with debugging...
		//yourSibling.setBackground(red);
		
        yourSibling.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        GridLayout pkiGrid = new GridLayout();
        pkiGrid.marginTop=10;
        yourSibling.setLayout( pkiGrid );
        groups =  addGroup( yourSibling );
        addFields( groups );
        initialize();
        setEditors();
	    checkState();
        
        /*
         * NOTE: 
         * When there has been NO pki selection made, then select PKCS11, but ONLY if its been configured by SA's.
         * Otherwise, just make the pkcs12 selection available.
         */
       
        if (PKIState.CONTROL.isPKCS11on()) {
        	//(VendorImplementation.getInstance().isInstalled() )) {
        
        	//System.out.println("PreferencePage -------- AVAL:"+VendorImplementation.getInstance().isInstalled() );
        	//System.out.println("PreferencePage --------- TURNING ON DEFAULT pkcs11 is on");
        	setPkcs12InVisible();
        	setPkcs11Visible();
        	
        	setVisible(false);
        	
        	Optional incomingProvider = Optional.ofNullable(AuthenticationBase.INSTANCE.getPkiProvider());
        	if ( incomingProvider.isEmpty() ) {
        		incomingProvider = Optional.ofNullable(System.getProperty("javax.net.ssl.keyStoreProvider"));
        				
        		if (incomingProvider.isEmpty() ) {
        			propertyAddedProvider="PKCS11";
        		} else {
        			propertyAddedProvider=(String) incomingProvider.get();
        		}
        		securityProvider.setStringValue(propertyAddedProvider);
        	} else {
        		securityProvider.setStringValue((String) incomingProvider.get());
        	}       	
    		System.out.println("PreferencePage --------- preference:"+securityProvider.getStringValue() );
    		
    		Optional cfgDirectoryHolder = Optional.ofNullable(AuthenticationBase.INSTANCE.getCfgDirectory());
    		
    		if ( cfgDirectoryHolder.isEmpty() ) {
    			configurationLocationFile.setStringValue("Set to your PKCS11 cfg file");
    		} else {
    			configurationLocationFile.setStringValue((String) cfgDirectoryHolder.get());
    		}
    		
    		System.out.println("PreferencePage --------- set cfgvalue"+configurationLocationFile.getStringValue());
    		AuthenticationPlugin.getDefault()
    			.getPreferenceStore()
    			.setValue(AuthenticationPreferences.SECURITY_PROVIDER, 
    				securityProvider.getStringValue());
    	
    		AuthenticationPlugin.getDefault()
    			.getPreferenceStore()
    			.setValue(AuthenticationPreferences.PKCS11_CFG_FILE_LOCATION,
    				 configurationLocationFile.getStringValue() );
    		
        } else if (PKIState.CONTROL.isPKCS12on()) {
        	//System.out.println("PreferencePage --------- pkcs12 is on ----------------");
        	setPkcs11InVisible();
        	setPkcs12Visible();
        	pkiSecureStorage.storePKI(AuthenticationPlugin.getDefault());
        	pkiSecureStorage.loadUpPKI();
        	
        	
        } else if ((!( PKIState.CONTROL.isPKCS11on() )) && 
            		(!(PKIState.CONTROL.isPKCS12on())) ) {
            		//(!(VendorImplementation.getInstance().isInstalled() ))) {
        	//System.out.println("PreferencePage --------- THERE WAS NO DEFAULT  pkcs12 is on");
        	PKIState.CONTROL.setPKCS12on(true);
        	setPkcs11InVisible();
        	setPkcs12Visible();
        } 
        
        //initialize();
        //setEditors();
	    //checkState();
		
		yourSibling.layout();
		
		parent.redraw();  
	    return yourSibling;
	}
	

	private Group addGroup(Composite top) {
		Group group = new Group(top, SWT.TOP);
		GridData data = new GridData(SWT.TOP, GridData.FILL_HORIZONTAL);
		data.horizontalSpan=550;
		data.verticalSpan=5;
		data.widthHint=650;
		data.heightHint = 225;
		group.setLayoutData(data); 
		return group;
	}
	private void addFields(Group group) {
		
		configurationLocationFile = new FileFieldEditor(IPreferenceConstants.DEFAULT_EDITORS,
				"&PKCS11 config Selection:", true, group );
		
		securityProvider = new PKICertLocationFieldEditor(AuthenticationPreferences.SECURITY_PROVIDER,
                "Java PKI Security Provider", group, "pkcs11", this);
				
		pkcs11Certificate = new PKICertLocationFieldEditor("NONE",
                "Smartcard repository location", group, "pkcs11", this);
		
		pkiCertificate = new PKICertLocationFieldEditor(AuthenticationPreferences.PKI_CERTIFICATE_LOCATION,
                "Certificate path:", group, "pkcs12", this);
		
		trustStoreJKS = new TrustStoreLocationFieldEditor(AuthenticationPreferences.TRUST_STORE_LOCATION,
                "Trust Store Location:", group);
		
		configurationLocationFile.setEnabled(true, group);
		securityProvider.getTextControl(group).setEnabled(true);
		pkcs11Certificate.getTextControl(group).setEnabled(false);
		pkiCertificate.getTextControl(group).setEnabled(false);
	    trustStoreJKS.getTextControl(group).setEnabled(false);
	   
	    configurationLocationFile.loadDefault();
	    securityProvider.loadDefault();
	    pkcs11Certificate.loadDefault();
	    pkiCertificate.loadDefault();
	  
	    addField(configurationLocationFile);
	    addField(securityProvider);
	    addField(pkcs11Certificate);
	    addField(pkiCertificate);
	    
	}
	

    /**
     * Initializes all field editors.
     */
    protected void initialize() {
        super.initialize();
        
    }
    protected void setEditors() {
    	
        if (pkiCertificate != null) {
        	pkiCertificate.setPage(this);
        	pkiCertificate.setPreferenceStore(AuthenticationPlugin.getDefault().getPreferenceStore());
        	pkiCertificate.load(); 
        }
        if (pkcs11Certificate != null) {
        	pkcs11Certificate.setPage(this);
        	pkcs11Certificate.setPreferenceStore(AuthenticationPlugin.getDefault().getPreferenceStore());
        	pkcs11Certificate.load();
        }
        if (trustStoreJKS != null) {
        	trustStoreJKS.setPage(this);
        	trustStoreJKS.setPreferenceStore(AuthenticationPlugin.getDefault().getPreferenceStore());
        	trustStoreJKS.load();	
        }
        if (configurationLocationFile != null ) {
        	configurationLocationFile.setPage(this);
        	configurationLocationFile.setPreferenceStore(AuthenticationPlugin.getDefault().getPreferenceStore());
        	configurationLocationFile.load();
        }
        if (securityProvider != null ) {
        	securityProvider.setPage(this);
        	securityProvider.setPreferenceStore(AuthenticationPlugin.getDefault().getPreferenceStore());
        	securityProvider.load();
        } 
    }
    protected void setPkcs11InVisible() {
    	
    	try {
    		//System.out.println("PreferencePage ---------  pkcs12 is on setting PKCS11 INVISIBLE");
			groups.getChildren()[0].setVisible(false);
			groups.getChildren()[1].setVisible(false);
			groups.getChildren()[2].setVisible(false);
			groups.getChildren()[3].setVisible(false);
			groups.getChildren()[4].setVisible(false);
			groups.getChildren()[5].setVisible(false);
			groups.getChildren()[6].setVisible(false);
			groups.getChildren()[7].setVisible(false);
			groups.getChildren()[8].setVisible(false);
			groups.setText(pkcs12Label);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    protected void setPkcs12InVisible() {
    	
    	try {
    		//System.out.println("PreferencePage ---------  pkcs11 is on");
        	groups.getChildren()[9].setVisible(false);
    		groups.getChildren()[10].setVisible(false);
        	groups.getChildren()[11].setVisible(false);
        	groups.getChildren()[12].setVisible(false);
        	
        	groups.setText(pkcs11Label);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    protected void setPkcs11Visible() {
    	
    	try {
    		pkiType="pkcs11";
    		groups.setText(pkcs11Label);
    	
    		
			groups.getChildren()[0].setVisible(true);
        	groups.getChildren()[1].setVisible(true);
        	groups.getChildren()[2].setVisible(true);
        	groups.getChildren()[3].setVisible(true);
        	groups.getChildren()[4].setVisible(true);
        	groups.getChildren()[5].setVisible(true);
        	groups.getChildren()[6].setVisible(true);
        	groups.getChildren()[7].setVisible(true);
        	groups.getChildren()[8].setVisible(true);
    		
        	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    protected void setPkcs12Visible() {
    	
    	try {
    		pkiType="pkcs12";
    		groups.setText(pkcs12Label);
    		
        	//groups.getChildren()[7].setVisible(true);
        	//groups.getChildren()[8].setVisible(true);
        	groups.getChildren()[9].setVisible(true);
        	groups.getChildren()[10].setVisible(true);
        	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void findGroups() {
    	Control[] co = groups.getChildren();
    	for(Control c : groups.getChildren()) {
    		
    	}
    	for(int i=0; i < co.length; i++)  {
    		if ( co[i] instanceof Label ) {
    			System.out.println("PreferencePage GROUP:"+ i+" "+co[i].toString());
    		}
    	}
    	
    }

	@Override
	protected void performApply() {
		//CheckUpdatedKeystoreValue.isValid( pkiCertificate.getStringValue() );
		//super.performApply();
		
		//System.out.println("PreferencePage -- APPLY PRESSED  FIX the stored values. TYPE:"+pkiType);
		try {
			if (!(exitView)) {
				if ( pkiType.equals("pkcs11") && ChangedPressedFieldEditorStatus.isPkiChangedPressed()) {
					//System.out.println("PreferencePage - APPLY PRESSED REQUEST  PKCS11 needs to be set");
					if ( pkcs11Certificate.isValid() ) {
						PKIState.CONTROL.setPKCS11on(true);
						ChangedPressedFieldEditorStatus.setPkiChangedPressed(true);
						if ((ChangedPressedFieldEditorStatus.isPkiChangedPressed() ) ) {
							if ( CheckUpdatedKeystoreValue.isValid( pkcs11Certificate.getStringValue() )) {
								
								pkcs11Certificate.setStringValue( "pkcs11" );
								
						
								//System.out.println("PreferencePage SETTING certificate path for PKCS11 FIX THIS");
								AuthenticationPlugin.getDefault().setCertificatePath("pkcs11");
								
								//System.out.println("PreferencePage SECURITYPROVIDER:"+ securityProvider.getStringValue());
								//System.out.println("PreferencePage CFG:"+ configurationLocationFile.getStringValue());
								
								
								//AuthenticationPlugin.getDefault().getPreferenceStore().setValue(AuthenticationPreferences.SECURITY_PROVIDER, defaultTrustStorePath);
								//AuthenticationPlugin.getDefault().getPreferenceStore().setValue(AuthenticationPreferences.PKCS11_CONFIGURE_FILE_LOCATION, defaultTrustStorePath);
								
								AuthenticationPlugin.getDefault().setUserKeyStore(VendorImplementation.getInstance().getKeyStore());
								AuthenticationPlugin.getDefault().setUserKeyStoreSystemProperties( VendorImplementation.getInstance().getKeyStore() );
								System.setProperty("javax.net.ssl.keyStoreProvider", "SunPKCS11");
								pkcs11Certificate.getTextControl(groups).setEnabled(false);
							}
						}
					} 
				}
				if ( (pkiType.equalsIgnoreCase("pkcs12") && (ChangedPressedFieldEditorStatus.isPkiChangedPressed()))) {
					if ( pkiCertificate.isValid() ) {
						PKIState.CONTROL.setPKCS12on(true);
						if ((ChangedPressedFieldEditorStatus.isPkiChangedPressed() ) ) {
							if ( CheckUpdatedKeystoreValue.isValid( pkiCertificate.getStringValue() )) {
								AuthenticationPlugin.getDefault().setCertificatePath(pkiCertificate.getStringValue());
								pkiCertificate.getTextControl(groups).setEnabled(false);
							}
						}
					}
				}
				previousPKI = this.previousPKI();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void performDefaults() {
		//System.out.println("PreferencePage --------- perform defaults...----------------------------RE");
		
		//getPreferenceStore().removePropertyChangeListener( propertyChangeListener );
//		if ( pkcs11Composite == null) {
//			AuthenticationPlugin.getDefault().setUserKeyStore(null);
//			super.performDefaults();
//		}
	}
    
    public void init(IWorkbench workbench) {
        // TODO Auto-generated method stub  
    }

    @Override
    protected void createFieldEditors() {
        // NOT called b/c we override createContents()
    }    

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		boolean isOK=true;
		//System.out.println("PreferencePage --------- OK PRESSED REQUEST   TBD:   FIX the stored values. TYPE:"+pkiType);
		
		if(ChangedPressedFieldEditorStatus.isJksChangedPressed()){
			//System.out.println("PreferencePage --------- OK PRESSED REQUEST changepresed trust");
			changeJKSTrustStoreSecureStorage();
			//The trust store path, trust store password and key store already set in AuthenticationPlugin when 
			//the user entered and clicked Finish in the trust store login wizard.
			AuthenticationPlugin.getDefault().setTrustStoreSystemProperties(AuthenticationPlugin.getDefault().getExistingTrustStore());
		}
		if(ChangedPressedFieldEditorStatus.isSaveConfigurationLocationFileChecked()){
			//System.out.println("PreferencePage --------- OK PRESSED REQUEST changepresed  CFG FILE");
		}
		
		if ((ChangedPressedFieldEditorStatus.isPkiChangedPressed() ) ) {
			AuthenticationPlugin.getDefault().setUserKeyStore(ChangedPressedFieldEditorStatus.getPkiUserKeyStore());
			//System.out.println("PreferencePage --------- PROCESSING A PKI CHANGE OK REQUEST");
			if (( PKIState.CONTROL.isPKCS11on()) || ( pkiType.equals("pkcs11") )) {
				
				//System.out.println("PreferencePage SECURITYPROVIDER:"+ securityProvider.getStringValue());
				//System.out.println("PreferencePage CFG:"+ configurationLocationFile.getStringValue());
				
				AuthenticationPlugin.getDefault()
					.getPreferenceStore()
					.setValue(AuthenticationPreferences.SECURITY_PROVIDER, 
							securityProvider.getStringValue());
				
				AuthenticationPlugin.getDefault()
					.getPreferenceStore()
					.setValue(AuthenticationPreferences.PKCS11_CFG_FILE_LOCATION,
							 configurationLocationFile.getStringValue() );
				
				/*
				 * NOTE:
				 * The USER needs to be able to type in a location. and If its NOT valid, then FAIL this TEST.
				 */
				isOK = CheckUpdatedKeystoreValue.isValid( pkcs11Certificate.getStringValue() );
				
				pkcs11Certificate.setStringValue( "pkcs11" );
				
				PKIProperties.getInstance().setKeyStorePassword(AuthenticationPlugin.getDefault().getCertPassPhrase());
				PKIProperties.getInstance().restore();
			} else 	if (( PKIState.CONTROL.isPKCS12on()) || ( pkiType.equals("pkcs12") )) {
				//System.out.println("PreferencePage --------- PROCESSING A CHANGE OK REQUEST   FOR PKCS12");
				if (  (pkiCertificate.getStringValue() != null ) || (!pkiCertificate.getStringValue().isEmpty() )){
					isOK  = CheckUpdatedKeystoreValue.isValid( pkiCertificate.getStringValue() );
				} else {
					//  The value was alaready set in authentication plugin so get it from there.
					isOK  = CheckUpdatedKeystoreValue.isValid( AuthenticationPlugin.getDefault().getCertificatePath() );
				}
			} else {
				//System.out.println("PreferencePage --------- PROCESSING A CHANGE OK REQUESt NO SELECTION");
			}
		
			changePKISecureStorage();
			//System.out.println("PreferencePage -----------performOK  SETING TRUSTSTORE VALUE BACK to initial value:"+ AuthenticationPlugin.getDefault().obtainSystemPropertyJKSPath());
			//AuthenticationPlugin.getDefault().setTrustStoreSystemProperties(AuthenticationPlugin.getDefault().getExistingTrustStore());
			
			//trustStoreJKS.setStringValue(AuthenticationPlugin.getDefault().obtainSystemPropertyJKSPath());
			
			//createContents(yourParent);
			
			//The pki path, pki password and key store already set in AuthenticationPlugin when 
			//the user entered and clicked Finish in the pki login wizard.
			//AuthenticationPlugin.getDefault().setUserKeyStoreSystemProperties(AuthenticationPlugin.getDefault().getExistingUserKeyStore());
			
		} else {
			//System.out.println("PreferencePage --------- OK PRESSED REQUEST  AND DING A RESTORE OF OLD VALUES");
			previousPKI.reSetSystem();
		}
		
		ChangedPressedFieldEditorStatus.setPKISaveCertificateChecked(false);
		ChangedPressedFieldEditorStatus.setJKSSaveTrustStoreChecked(false);
		ChangedPressedFieldEditorStatus.setSaveConfigurationLocationFileChecked(false);
		ChangedPressedFieldEditorStatus.setPkiChangedPressed(false);
		ChangedPressedFieldEditorStatus.setJksChangedPressed(false);
		
		//System.out.println("PreferencePage -----------performOK DONE   VALUE:"+isOK);
		if ( isOK ) {
			super.performOk();
		}
		
		//return super.performOk();
		return isOK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performCancel()
	 */
	@Override
	public boolean performCancel() {
		
		//String jksTrustStorePathInSystemProperties = AuthenticationPlugin.getDefault().obtainSystemPropertyJKSPath();
		String jksTrustStorePathInSystemProperties ="";
		
		if(ChangedPressedFieldEditorStatus.isPkiChangedPressed()){
			AuthenticationPlugin.getDefault().setUserKeyStore(ChangedPressedFieldEditorStatus.getPreviousUserKeyStore());
    		previousPKI.reSetSystem();
    		if ( previousPKI.getKeyStoreType().equalsIgnoreCase("PKCS11") ) {
    			PKIState.CONTROL.setPKCS11on(true);
    			PKIState.CONTROL.setPKCS12on(false);
    			AuthenticationPlugin.getDefault().setCertificatePath("pkcs11" );
    			pkcs11Certificate.setStringValue("pkcs11");
    		}
    		if ( previousPKI.getKeyStoreType().equalsIgnoreCase("PKCS12") ) {
    			PKIState.CONTROL.setPKCS12on(true);
    			PKIState.CONTROL.setPKCS11on(false);
    			String pkiCertificatePathInSystemProperties = AuthenticationPlugin.getDefault().obtainSystemPropertyPKICertificatePath();
    			AuthenticationPlugin.getDefault().setCertificatePath(pkiCertificatePathInSystemProperties);
    			pkiCertificate.setStringValue(pkiCertificatePathInSystemProperties);
    		}
    		PKIProperties.getInstance().load();
    		AuthenticationPlugin.getDefault().setCertPassPhrase(AuthenticationPlugin.getDefault().obtainSystemPropertyPKICertificatePass());
    		AuthenticationPlugin.getDefault().setTrustStoreSystemProperties(AuthenticationPlugin.getDefault().getTrustStore());
    		
    		if ( previousPKI.isSecureStorage()) {
    			ChangedPressedFieldEditorStatus.setPKISaveCertificateChecked( true );
    			changePKISecureStorage();
    		}
    		
		} else {
			//System.out.println("PreferencePage -----------performCANCEL THERE WAS NO CHANGE,,,..");
		}
		
		if(ChangedPressedFieldEditorStatus.isJksChangedPressed()){
			AuthenticationPlugin.getDefault().setTrustStore(ChangedPressedFieldEditorStatus.getJksTrustStore());
    		AuthenticationPlugin.getDefault().getPreferenceStore().setValue(AuthenticationPreferences.TRUST_STORE_LOCATION, jksTrustStorePathInSystemProperties);
    		AuthenticationPlugin.getDefault().setTrustStorePassPhrase(AuthenticationPlugin.getDefault().obtainSystemPropertyJKSPass());
		}
		
		ChangedPressedFieldEditorStatus.setPkiChangedPressed(false);
		ChangedPressedFieldEditorStatus.setJksChangedPressed(false);
		
		
		ChangedPressedFieldEditorStatus.setPKISaveCertificateChecked(false);
		ChangedPressedFieldEditorStatus.setJKSSaveTrustStoreChecked(false);
		
		ChangedPressedFieldEditorStatus.setPkiUserKeyStore(null);
		ChangedPressedFieldEditorStatus.setJksTrustStore(null);
		
		return super.performCancel();
		
	}
	@Override
	public boolean okToLeave() {
		//System.out.println("PreferencePage ----------------------------------------ok to leave");
		
		//initialize();
		//this.performApply();
		//this.performDefaults();
		
		/*
		*** dont do this because it causes problems
   		* this.getShell().setVisible(false);
   		*/
//		return false;	
		return true;
	}
//	@Override
//	public void setValid( boolean b ) {
//		isGoodConfig = b;
//	}
	
	@Override
	public boolean isValid() {
		//System.out.println("PreferencePage ------ isValid");
		boolean isGood=false;
		isGood=isGoodConfig;
		if ( PKIState.CONTROL.isPKCS12on()) {
			isGood=true;
		}
		if ( PKIState.CONTROL.isPKCS11on()) {
			if ( pkcs11Certificate.isValid() ) {
				isGood=true;
			}
		}
		 if ((!(PKIState.CONTROL.isPKCS11on())) && 
		     (!(PKIState.CONTROL.isPKCS12on())) ) {
		      isGood=true;
		 }
		 //System.out.println("PreferencePage --------------------------isValid-----------------VALID:"+isGoodConfig);
		 
		/*
		 * TODO
		 * Figure out how to see if focus is lost for this preference page.
		 * Till then the apply and OK buttons need to be ALEWAYS enabled.
		 */
		//isGood=true;
		return isGood;
	}
	
	/**
	 * Store changed PKI information to secure storage.
	 */
	private void changePKISecureStorage(){
		
		pkiSecureStorage = new PKISecureStorage();
		if(ChangedPressedFieldEditorStatus.isPKISaveCertificateChecked()){			
			pkiSecureStorage.storePKI(AuthenticationPlugin.getDefault());
		} else {
			pkiSecureStorage.getNode().removeNode();
		}				
	}
	
	/**
	 * Store changed jks trust store to secure storage.
	 */
	private void changeJKSTrustStoreSecureStorage(){
		AuthenticationPlugin.getDefault().getPreferenceStore().setValue(AuthenticationPreferences.TRUST_STORE_LOCATION, trustStoreJKS.getStringValue() );
		TrustStoreSecureStorage jksTrustStore = new TrustStoreSecureStorage();
		if(ChangedPressedFieldEditorStatus.isJKSSaveTrustStoreChecked()){			
			jksTrustStore.storeJKS(AuthenticationPlugin.getDefault());
		} else {
			jksTrustStore.getNode().removeNode();
		}
	}
	public boolean pageChangeListener(Object source, String property, String oldValue, String newValue ) {
		boolean pageUpdate=true;
		
		try {
			if ( "FOCUS".equals(property)) {
				if ( newValue.equals(PKICertLocationFieldEditor.FOCUS_LOST)  ) {
					isGoodConfig=true;
					exitView=true;
					setValid(true);
				}
				if ( newValue.equals(PKICertLocationFieldEditor.FOCUS_GAINED)  ) {
					isGoodConfig=false;
					exitView=false;
					setValid(false);
				}
			}
			if ( "VALIDATE".equals(property)) {
				if ( newValue.equals("TURN_ON_APPLY")  ) {
					//System.out.println("PreferencePage ------  APPLY EVENT TURN ON GOOD CONFIG  GOODCONIF:"+isGoodConfig);
					if (!( isGoodConfig)  ) {
						//System.out.println("PreferencePage ------  APPLY EVENT TURN ON GOOD CONFIG");
						isGoodConfig=true;
						setValid(true);
						setVisible(true);
					}
					updateApplyButton();
				}
				if ( newValue.equals("TURN_OFF_APPLY")  ) {
					//System.out.println("PreferencePage ------  APPLY EVENT TURN OFF APPLY --- GOOD CONFIG");
					isGoodConfig=false;
					setValid(false);
					setVisible(true);
				}
			}
			if ( AuthenticationPreferences.PKI_CERTIFICATE_LOCATION.equals(property)) {
				if ( newValue != null) {
					setPkcs11InVisible();
					setPkcs12Visible(); 	
		        	pkcs11Certificate.setStringValue((String) "");
		        	pkiCertificate.setStringValue((String) newValue);
		        	isGoodConfig=true;
		        	setValid(true);
				}
			}
			if ( AuthenticationPreferences.PKCS11_CONFIGURE_FILE_LOCATION.equals(property)) {
				if ( newValue != null) {
					setPkcs12InVisible();
					setPkcs11Visible();
		        	pkiCertificate.setStringValue((String) "");
		        	pkcs11Certificate.setStringValue((String) newValue);
		        	isGoodConfig=true;
		        	setValid(true);
				}
			}
			
		} catch( Exception pageProcessorError) {
			pageProcessorError.printStackTrace();
		}
		
		return pageUpdate;
	}
	
	protected PKI previousPKI() {
		PKI pki = new PKI();
		PKIProperties current =  PKIProperties.getInstance();
		pkiSecureStorage = new PKISecureStorage();
		if (pkiSecureStorage.isPKISaved()) {
			pki.setSecureStorage(true);
		}
		current.load();
		pki.setKeyStore(current.getKeyStore());
		pki.setKeyStorePassword(current.getKeyStorePassword());
		pki.setKeyStoreProvider(current.getKeyStoreProvider());
		pki.setKeyStoreType(current.getKeyStoreType());
		return pki;	
	}
	public static void listProviders() {
		for ( Provider provider : Security.getProviders() ) {
	    	System.out.println("PreferencePage -BEFORE ADDING ANY Provider NAME:"+ provider.getName() );
	    }
	}
	
	public void printoutStore() {
		PrintStream ps = new PrintStream( System.out);
		ScopedPreferenceStore store = (ScopedPreferenceStore) AuthenticationPlugin.getDefault().getPreferenceStore();
		IEclipsePreferences[] prefs = store.getPreferenceNodes(true);
		for ( IEclipsePreferences node : prefs) {
			try {
				String[] keys=node.keys();
				for ( String name : keys) {
					System.out.println("PreferencePage PREF:"+ name+" VALUE:"+ 
								node.get(name, null));
				}
			} catch (BackingStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//String[] names = store.preferenceNames();
		//for ( String name : names) {
		//	System.out.println("PreferencePage PREF:"+ name+" VALUE:"+ store.getDefaultString(name));
		//}
//		PreferenceStore store = (PreferenceStore) this.getPreferenceStore();
//		store.list ( ps );
		
		
	}
}
