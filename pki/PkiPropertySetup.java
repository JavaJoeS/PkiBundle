
public enum PkiPropertySetup {
	PREPARATION;
	public void validation() {
		Optional keystoreTypeContainer = Optional.ofNullable(
			System.getProperty("javax.net.ssl.keyStoreType")); //$NON-NLS-1$
		
		Optional keystoreContainer = Optional.ofNullable(
			System.getProperty("javax.net.ssl.keyStore"): //$NON-NLS-1$
				
		Optional keystorePasswordContainer = Optional.ofNullable(
			System.getProperty("javax.net.ssl.keyStorePassword"): //$NON-NLS-1$
		
	}

}
