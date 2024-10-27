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
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.pki.util.LogUtil;
import java.util.concurrent.Flow.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class PublishPasswordUpdate implements Publisher<String> {
	private final ExecutorService executor = Executors.newFixedThreadPool(10);
	private List<Subscriber<? super String>> subscribers = new ArrayList<>();
	private static PublishPasswordUpdate instance = null;
	public PublishPasswordUpdate() {}
	public static PublishPasswordUpdate getInstance() {
        if(instance == null) {
            instance = new PublishPasswordUpdate();
        }  
        return instance;
    }

	@Override
	public void subscribe(Subscriber subscriber) {
		//LogUtil.logWarning(" PublishPasswordUpdate-------subscribe"); //$NON-NLS-1$
		if ( instance==null ) {
			this.getInstance();
		}
		subscribers.add(subscriber);
		//System.out.println("PublishPasswordUpdate adding subscriber COUNT:"+subscribers.size());
	}

	public void publishMessage(String message) {
		subscribers.forEach(subscriber -> {
			executor.submit(() -> {
				subscriber.onNext(message);
			});
		});
	}
	 public void close() {
        subscribers.forEach(Subscriber::onComplete);
        executor.shutdown();
    }
}
