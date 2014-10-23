 
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