package org.eclipse.core.tests.pki.auth;

import static org.junit.jupiter.api.Assertions.*;

//import org.junit.jupiter.api.Test;

import org.eclipse.core.pki.auth.KeystoreSetup;
import org.mockito.junit.MockitoJUnitRunner;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class KeystoreSetupTest {
	

	@Mock
	KeystoreSetup keyStoreSetupMock = null;
	public KeystoreSetupTest() {}
	
	@Before
	public void Initialize() throws Exception {
		MockitoAnnotations.initMocks(this);	
		keyStoreSetupMock = mock(KeystoreSetup.class);
	}
	@Test
	public void testgetInstance() {
		KeystoreSetup keystoreMock = Mockito.spy(KeystoreSetup.getInstance());
		
		try (MockedStatic <KeystoreSetup> keyStoreSetupMock = Mockito.mockStatic(KeystoreSetup.class)) {
			keyStoreSetupMock.when(() -> { KeystoreSetup.getInstance(); })
	      .thenReturn(keystoreMock);
		}
	}
}
