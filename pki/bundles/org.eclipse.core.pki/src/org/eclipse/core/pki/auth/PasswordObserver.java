/**
 * Copyright (c) 2014 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.core.pki.auth;

import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.spi.RegistryStrategy;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.pki.util.LogUtil;
import org.eclipse.core.pki.util.ConfigureTrust;
import org.eclipse.core.pki.util.KeyStoreManager;
import org.eclipse.core.pki.util.KeyStoreFormat;


import org.eclipse.core.pki.pkiselection.PKIProperties;

@SuppressWarnings("restriction")
public class PasswordObserver implements Observer {
	static boolean isPkcs11Installed = false;
	static boolean isKeyStoreLoaded = false;
	PKIProperties pkiInstance = null;
	Properties pkiProperties = null;
	SSLContext sslContext = null;
	protected static KeyStore keyStore = null;
	private static final int DIGITAL_SIGNATURE = 0;
	private static final int KEY_CERT_SIGN = 5;
	private static final int CRL_SIGN = 6;
	public PasswordObserver() {

	}

	public void update(Observable obj, Object arg) {
		Optional<KeyStore> keystoreContainer = null;
		String pw = (String) arg;
		LogUtil.logWarning("PasswordObserver- BREAK for INPUT:"+pw);
		System.setProperty("javax.net.ssl.keyStorePassword", pw); //$NON-NLS-1$
		try {

			keystoreContainer = Optional.ofNullable(
					KeyStoreManager.INSTANCE.getKeyStore(System.getProperty("javax.net.ssl.keyStore"), //$NON-NLS-1$
							System.getProperty("javax.net.ssl.keyStorePassword"), //$NON-NLS-1$
							KeyStoreFormat.valueOf(System.getProperty("javax.net.ssl.keyStoreType")))); //$NON-NLS-1$

			if ((keystoreContainer.isEmpty()) || (!(KeyStoreManager.INSTANCE.isKeyStoreInitialized()))) {
				LogUtil.logError("PKISetup - Failed to Load a Keystore.", null); //$NON-NLS-1$
				PKIState.CONTROL.setPKCS12on(false);
				System.clearProperty("javax.net.ssl.keyStoreType"); //$NON-NLS-1$
				System.clearProperty("javax.net.ssl.keyStore"); //$NON-NLS-1$
				System.clearProperty("javax.net.ssl.keyStoreProvider"); //$NON-NLS-1$
				System.clearProperty("javax.net.ssl.keyStorePassword"); //$NON-NLS-1$
				SecurityFileSnapshot.INSTANCE.restoreProperties();
			} else {
				LogUtil.logError("A Keystore and Password are detected.", null); //$NON-NLS-1$
				keyStore = keystoreContainer.get();
				setKeyStoreLoaded(true);
				setPkiContext();
			}
		} catch (Exception e) {
			LogUtil.logError("Failed to Load Keystore.", e); //$NON-NLS-1$
		}
	}
	public void setPkiContext() {
		LogUtil.logError("setPkiContext", null); //$NON-NLS-1$
		if ((IncomingSystemProperty.SETTINGS.checkTrustStoreType()) && (isKeyStoreLoaded())) {
			if ((IncomingSystemProperty.SETTINGS.checkTrustStore())
					&& (KeyStoreManager.INSTANCE.isKeyStoreInitialized())) {
				LogUtil.logInfo("A KeyStore and Truststore are detected."); //$NON-NLS-1$
				Optional<X509TrustManager> PKIXtrust = ConfigureTrust.MANAGER.setUp();

				try {
					KeyManager[] km = new KeyManager[] { KeyStoreManager.INSTANCE };
					TrustManager[] tm = new TrustManager[] { ConfigureTrust.MANAGER };
					if (PKIXtrust.isEmpty()) {
						LogUtil.logError("Invalid TrustManager Initialization.", null); //$NON-NLS-1$
					} else {
						SSLContext ctx = SSLContext.getInstance("TLS");//$NON-NLS-1$
						ctx.init(km, tm, new SecureRandom());
						SSLContext.setDefault(ctx);
						HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
						setSSLContext(ctx);
						pkiInstance = PKIProperties.getInstance();
						pkiInstance.load();
						setUserEmail();
						// Grab a handle to registry
						File[] storageDirs = null;
						boolean[] cacheReadOnly = null;
						String token = "core.pki";
						/*
						 * try { IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); //
						 * IResource resource = //
						 * ResourcesPlugin.getWorkspace().getAdapter(IResource.class); IMarker marker =
						 * root.createMarker("virtual.core.pki.context"); marker.setAttribute(token,
						 * ctx); } catch (Exception imarkerErr) { imarkerErr.printStackTrace(); }
						 */
						/*
						 * IExtensionRegistry registry = Platform.getExtensionRegistry();
						 * IConfigurationElement[] extensions =
						 * registry.getConfigurationElementsFor(EXTENSION_POINT); for
						 * (IConfigurationElement element : extensions) { [..] }
						 */

						// InjectorFactory.getDefault().addBinding(MyPart.class).implementedBy(MyFactory.class)
						// RegistryStrategy strategy = RegistryFactory.createOSGiStrategy(File[]
						// storageDirs, boolean[] cacheReadOnly, Object token)
						RegistryStrategy strategy = RegistryFactory.createOSGiStrategy(storageDirs,
								cacheReadOnly, token);
						// IExtensionRegistry registry = RegistryFactory.getRegistry();
						IExtensionRegistry registry = RegistryFactory.createRegistry(strategy, token, ctx);

						setupAdapter();
						LogUtil.logInfo("PKISetup default SSLContext has been configured."); //$NON-NLS-1$
					}
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					LogUtil.logError("Initialization Error", e); //$NON-NLS-1$
				} catch (KeyManagementException e) {
					// TODO Auto-generated catch block
					LogUtil.logError("Initialization Error", e); //$NON-NLS-1$
				}
			} else {
				LogUtil.logError("Valid KeyStore and Truststore not found.", null); //$NON-NLS-1$
			}
		} else {
			LogUtil.logError("Valid Truststore not found.", null); //$NON-NLS-1$
		}
	}
	public SSLContext getSSLContext() {
		return sslContext;
	}

	public void setSSLContext(SSLContext context) {
		this.sslContext = context;
	}
	private boolean isKeyStoreLoaded() {
		return PKISetup.isKeyStoreLoaded;
	}

	private void setKeyStoreLoaded(boolean isKeyStoreLoaded) {
		PKISetup.isKeyStoreLoaded = isKeyStoreLoaded;
	}
	private void setUserEmail() {
		try {
			Enumeration<String> en = keyStore.aliases();
			while (en.hasMoreElements()) {
				String alias = en.nextElement();
				// System.out.println(" " + alias);
				Certificate cert = keyStore.getCertificate(alias);
				if (cert.getType().equalsIgnoreCase("X.509")) {
					X509Certificate X509 = (X509Certificate) cert;

					//
					// we need to make sure this is a digital certificate instead of a server
					// cert or something
					//
					if (isDigitalSignature(X509.getKeyUsage())) {
						Collection<List<?>> altnames = X509.getSubjectAlternativeNames();
						if (altnames != null) {
							for (List<?> item : altnames) {
								Integer type = (Integer) item.get(0);
								if (type == 1)
									try {
										String userEmail = item.toArray()[1].toString();
										System.setProperty("mail.smtp.user", userEmail);
									} catch (Exception e) {
										e.printStackTrace();
									}
							}
						}

					}

				}
			}
		} catch (Exception err) {

		}
	}

	private static boolean isDigitalSignature(boolean[] ba) {
		if (ba != null) {
			return ba[DIGITAL_SIGNATURE] && !ba[KEY_CERT_SIGN] && !ba[CRL_SIGN];
		} else {
			return false;
		}
	}

	private void setupAdapter() {
		
		IAdapterFactory pr = new IAdapterFactory() {
	        @Override
	        public Class[] getAdapterList() {
	                return new Class[] { SSLContext.class };
	        }
	        
			@Override
			public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
					IResource res = (IResource) adaptableObject;
					SSLContext v = null;
					QualifiedName key = new QualifiedName("org.eclipse.core.pki", "context");
					try {
						v = (SSLContext) res.getSessionProperty(key);
						if (v == null) {
							v = getSSLContext();
							res.setSessionProperty(key, v);
						}
					} catch (CoreException e) {
						// unable to access session property - ignore
					}
					return (T)v;
			}
		};
		Platform.getAdapterManager().registerAdapters(pr,IResource.class);
	}

}
