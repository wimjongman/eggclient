package com.remainsoftware.egg.ui.parts;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.widgets.datadefinition.IManualValueChangeListener;
import org.eclipse.nebula.visualization.widgets.figures.KnobFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class KnobComposite extends Composite {

	private KnobFigure fKnobFigure;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.ON_TOP | SWT.DIALOG_TRIM);
		shell.setLayout(new FillLayout());
		new KnobComposite(shell, SWT.NONE);
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
	public KnobComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));

		Canvas canvas = new Canvas(this, SWT.NONE);

		// use LightweightSystem to create the bridge between SWT and draw2D
		final LightweightSystem lws = new LightweightSystem(canvas);

		fKnobFigure = new KnobFigure();

		// Init Knob
		fKnobFigure.setRange(20, 130);
		fKnobFigure.setMajorTickMarkStepHint(50);
		fKnobFigure.setThumbColor(ColorConstants.gray);
		fKnobFigure
				.addManualValueChangeListener(new IManualValueChangeListener() {
					@Override
					public void manualValueChanged(double newValue) {
						System.out.println("You set value to: " + newValue);
					}
				});

		lws.setContents(fKnobFigure);

	}

	public KnobFigure getKnob() {
		return fKnobFigure;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
