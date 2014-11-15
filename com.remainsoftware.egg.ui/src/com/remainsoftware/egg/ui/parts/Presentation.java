/*******************************************************************************
 * Copyright (c) 2014 Remain Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wim.Jongman@remainsofwtare.com
 *******************************************************************************/ 
package com.remainsoftware.egg.ui.parts;

import javax.inject.Inject;
import javax.annotation.PostConstruct;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.SWT;

public class Presentation {
	@Inject
	public Presentation() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
		Browser browser = new Browser(parent, SWT.NONE);
		browser.setUrl("file:///C:/Users/jongw/Dropbox/RemainShared/Manuals/IBM i modernization.pdf");
		
	}
	
	
	
	
}