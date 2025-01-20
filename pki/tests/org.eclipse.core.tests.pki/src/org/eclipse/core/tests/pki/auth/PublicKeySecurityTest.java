/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.pki.auth;


import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import junit.framework.TestCase;

//import static org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
//import static org.assertj.swing.fixture.JPanelFixture;
//import static org.assertj.swing.testing.AssertJSwingTestCaseTemplate;

import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.MockedStatic;

//import org.mockito.MockedStatic.staticMethod;
//import org.powermock.modules.junit4.PowerMockRunner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.eclipse.core.pki.auth.PublicKeySecurity;

public class PublicKeySecurityTest {
	PublicKeySecurity publicKeySecurityMock = null;
	Properties properties = new Properties();
	String PiN = "12345679";
	public PublicKeySecurityTest() {}
	
	
	@Before
	public void Initialize() throws Exception {
		MockitoAnnotations.initMocks(this);
		publicKeySecurityMock = mock(PublicKeySecurity.class);
	}
	
	@Test
	public void testGetpkiPropertyFile() {
		when(publicKeySecurityMock.getPkiPropertyFile(PiN)).thenReturn(properties);
	}

}
