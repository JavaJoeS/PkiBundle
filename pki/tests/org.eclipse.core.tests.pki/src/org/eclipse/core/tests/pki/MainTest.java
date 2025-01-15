package org.eclipse.core.tests.pki;

/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
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
//import org.eclipse.core.pki.PKISetup;
import java.util.Properties;
import java.util.concurrent.Flow.Subscriber;
import org.eclipse.core.pki.auth.PublishPasswordUpdate;
import org.eclipse.core.pki.auth.PublishPasswordUpdateIfc;
import org.eclipse.core.pki.auth.PublicKeySecurity;
import org.eclipse.core.pki.auth.SecurityFileSnapshot;
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

import org.eclipse.core.pki.pkiselection.SecurityOpRequest;
import org.eclipse.core.pki.pkiselection.PkiPasswordGrabberWidget;
import org.eclipse.core.pki.auth.PKISetup;


//@RunWith(PowerMockRunner.class)
public class MainTest {
	Properties properties = new Properties();
	Object o = new Object();
	String testName = "PKItestSubscriber";
	PKITestSubscriber subscriber = null;
	PublicKeySecurity publicKeySecurityMock = null;
	SecurityOpRequest securityOpRequestMock = null;
	PublishPasswordUpdate publishPasswordUpdateMock = null;
	PKISetup pkiSetupMock = null;
	SecurityFileSnapshot securityFileSnapshotMock = null;
	PkiPasswordGrabberWidget pkiPasswordGrabberWidgetMock = null;
	
	
	public MainTest() {
		//System.out.println("Constructor MainTest");
	}
	
	@Before
	public void Initialize() throws Exception {
		MockitoAnnotations.initMocks(this);
		subscriber = new PKITestSubscriber(testName);
		publicKeySecurityMock = mock(PublicKeySecurity.class);
		securityOpRequestMock = mock(SecurityOpRequest.class);
		
		publishPasswordUpdateMock = mock(PublishPasswordUpdate.class);
		pkiSetupMock = mock(PKISetup.class);
		securityFileSnapshotMock = mock(SecurityFileSnapshot.class);
		pkiPasswordGrabberWidgetMock = mock(PkiPasswordGrabberWidget.class);
	}

	@Test
	public void testRun() {
		Object o = new Object();
		assertEquals(this.testName, subscriber.getName(), "The name should be set correctly.");
		assertNotEquals("footest", subscriber.getName(), "The name should be set correctly.");
	}
	@Test
	public void testSecurityFileSnapshot() {
		when(securityFileSnapshotMock.image()).thenReturn(true);
		when(securityFileSnapshotMock.createPKI()).thenReturn(true);
		when(securityFileSnapshotMock.load(isA(String.class),  isA(String.class))).thenReturn(properties);
		doNothing().when(securityFileSnapshotMock).restoreProperties();
	}
	@Test
	public void testPkiPasswordGrabberWidget() {
		when(pkiPasswordGrabberWidgetMock.getInput()).thenReturn("testPassword");
	}
	@Test
	public void testSecurityOp() {
		when(securityOpRequestMock.getConnected()).thenReturn(true);
		doNothing().when(securityOpRequestMock).setConnected(false);
		boolean testResult = securityOpRequestMock.getConnected();
		assertTrue(testResult);
		securityOpRequestMock.setConnected(true);	
	}
	@Test
	public void testPublishPasswordUpdate() {
		doNothing().when(publishPasswordUpdateMock).subscribe(subscriber);
		when(publishPasswordUpdateMock.getSubscriberCount()).thenReturn(1);
		doNothing().when(publishPasswordUpdateMock).publishMessage(isA(String.class));
		doNothing().when(publishPasswordUpdateMock).close();	
	}
}
