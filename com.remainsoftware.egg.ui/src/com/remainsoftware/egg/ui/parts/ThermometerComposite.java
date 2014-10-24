package com.remainsoftware.egg.ui.parts;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.widgets.figures.ThermometerFigure;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ThermometerComposite extends Composite {

	private double fTemp;
	private ThermometerFigure fThermo;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.ON_TOP | SWT.DIALOG_TRIM);
		shell.setLayout(new FillLayout());
		new ThermometerComposite(shell, SWT.NONE);
		shell.pack();
		shell.open();
		shell.setSize(100, 500);
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public ThermometerComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 10;
		setLayout(gridLayout);

		Canvas canvas = new Canvas(this, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// use LightweightSystem to create the bridge between SWT and draw2D
		final LightweightSystem lws = new LightweightSystem(canvas);

		fThermo = new ThermometerFigure();

		// Init widget
		fThermo.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(
				255, 255, 255));

		// fThermo.setBorder(new SchemeBorder(SchemeBorder.SCHEMES.ETCHED));

		fThermo.setRange(0, 120);
		fThermo.setHiLevel(60);
		fThermo.setHihiLevel(78);
		fThermo.setShowHi(false);
		fThermo.setEffect3D(true);
		fThermo.setShowLo(false);
		fThermo.setShowLolo(false);
		fThermo.setShowHihi(false);
		fThermo.setValue(24.4);
		fThermo.setFillColor(getDisplay().getSystemColor(SWT.COLOR_GREEN));
		lws.setContents(fThermo);

	}

	public ThermometerFigure getThermo() {
		return fThermo;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void setTemperature(Double pTemp) {
		fTemp = pTemp;
		fThermo.setValue(fTemp);
		if (fTemp > 60) {
			fThermo.setFillColor(getDisplay().getSystemColor(SWT.COLOR_RED));
		} else if (fTemp > 24) {
			fThermo.setFillColor(getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		} else {
			fThermo.setFillColor(getDisplay().getSystemColor(SWT.COLOR_GREEN));
		}
	}
}
