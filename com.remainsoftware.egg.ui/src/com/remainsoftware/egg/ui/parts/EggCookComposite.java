package com.remainsoftware.egg.ui.parts;

import java.util.Random;

import org.eclipse.ecf.raspberrypi.gpio.IGPIOPinOutput;
import org.eclipse.nebula.visualization.widgets.datadefinition.IManualValueChangeListener;
import org.eclipse.nebula.widgets.oscilloscope.multichannel.Oscilloscope;
import org.eclipse.nebula.widgets.oscilloscope.multichannel.OscilloscopeDispatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Label;

public class EggCookComposite extends Composite {
	private static final int PIN_COOK = 0;
	private static final int PIN_LIGHT = 1;
	private static final String COOK = "Cook!";
	private static final boolean SWITCH_OPEN = false;
	private static final boolean SWITCH_CLOSED = true;
	private boolean fAdmin = "true".equals(System.getProperty("admin"));
	private ThermometerComposite fThermo;

	IGPIOPinOutput fCookPin = null;
	IGPIOPinOutput fLightPin = null;
	private ThermometerComposite fFloatingThermo;
	private Shell fFloatShell;
	private KnobComposite fKnob;
	private Button fBtnCook;
	private Button fBtnLight;
	private Double fTemp;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new EggCookComposite(shell, SWT.NONE);
		shell.pack();
		shell.open();
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
	public EggCookComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(3, false));

		fThermo = new ThermometerComposite(this, SWT.NONE);
		fThermo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));

		Group grpSafetyTemperature = new Group(this, SWT.NONE);
		grpSafetyTemperature.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpSafetyTemperature.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 2));
		grpSafetyTemperature.setText("Safety Temperature");

		fKnob = new KnobComposite(grpSafetyTemperature, SWT.NONE);
		fKnob.getKnob().addManualValueChangeListener(getValueChangeListener());

		fBtnCook = new Button(this, SWT.NONE);
		fBtnCook.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.BOLD
				| SWT.ITALIC));
		fBtnCook.setText(COOK);
		setButtonCooking(isCooking());
		fBtnCook.addSelectionListener(getCookButtonSelectionListener());
		fBtnCook.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		fBtnCook.setEnabled(false);

		fBtnLight = new Button(this, SWT.NONE);
		fBtnLight.setFont(SWTResourceManager.getFont("Segoe UI", 15, SWT.BOLD
				| SWT.ITALIC));
		fBtnLight.addSelectionListener(getLightButtonSelectionListener());
		setButtonLight(isLightOn());
		fBtnLight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		fBtnLight.setText("Light");
		fBtnLight.setEnabled(false);

		Group grpCooker = new Group(this, SWT.NONE);
		grpCooker.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		grpCooker.setText("Cooker - Pin 0");
		grpCooker.setLayout(new FillLayout(SWT.HORIZONTAL));

		Oscilloscope oscilloscope = new Oscilloscope(grpCooker, SWT.BORDER);
		dispatch(oscilloscope, PIN_COOK);

		Group grpLightPin = new Group(this, SWT.NONE);
		grpLightPin.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		grpLightPin.setText("Light - Pin 1");
		grpLightPin.setLayout(new FillLayout(SWT.HORIZONTAL));

		Oscilloscope oscilloscope_1 = new Oscilloscope(grpLightPin, SWT.BORDER);
		dispatch(oscilloscope_1, PIN_LIGHT);
	}

	private IManualValueChangeListener getValueChangeListener() {
		return new IManualValueChangeListener() {
			@Override
			public void manualValueChanged(double newValue) {
				fThermo.getThermo().setHiLevel(newValue);
				fThermo.getThermo().setShowHi(true);
			}
		};
	}

	private SelectionAdapter getLightButtonSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!isLightOn()) {
					lightOn();
				} else {
					lightOff();
				}
			}
		};
	}

	protected void setButtonLight(boolean pLightOn) {
		if (pLightOn) {
			fBtnLight.setImage(ResourceManager.getPluginImage(
					"com.remainsoftware.egg.ui", "icons/light_on.png"));
		} else {
			fBtnLight.setImage(ResourceManager.getPluginImage(
					"com.remainsoftware.egg.ui", "icons/light.png"));
		}
	}

	protected void lightOn() {
		if (fLightPin != null && !isLightOn()) {
			fLightPin.toggle();
			setButtonLight(true);
		}
	}

	protected void lightOff() {
		if (isLightOn()) {
			fLightPin.toggle();
			setButtonLight(false);
		}
	}

	protected boolean isLightOn() {
		if (fLightPin != null) {
			return fLightPin.getState() == SWITCH_OPEN;
		}
		return false;
	}

	private SelectionAdapter getCookButtonSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openFloatShell();
				if (fAdmin)
					toggleCooking();
			}

		};
	}

	private void toggleCooking() {
		if (!isCooking()) {
			cook();
		} else {
			stopCooking();
			setButtonCooking(false);
		}
	}

	private void setButtonCooking(boolean pCooking) {
		if (pCooking) {
			fBtnCook.setImage(ResourceManager.getPluginImage(
					"com.remainsoftware.egg.ui", "icons/stop.png"));
			fBtnCook.setText("Stop!!");
		} else {
			fBtnCook.setImage(ResourceManager.getPluginImage(
					"com.remainsoftware.egg.ui", "icons/egg_PNG5.png"));
			fBtnCook.setText(COOK);
		}
	}

	protected void stopCooking() {
		if (fAdmin && fCookPin != null && isCooking()) {
			fCookPin.setState(SWITCH_CLOSED);
			setButtonCooking(false);
		}
	}

	protected boolean isCooking() {
		if (fCookPin != null) {
			return fCookPin.getState() == SWITCH_OPEN;
		}
		return false;
	}

	protected void cook() {
		if (fAdmin || fCookPin != null && !isCooking()) {
			fCookPin.setState(SWITCH_OPEN);
			setButtonCooking(true);
		}
	}

	private void openFloatShell() {
		if (fFloatShell == null || fFloatShell.isDisposed()) {
			fFloatShell = new Shell(getShell(), SWT.ON_TOP | SWT.DIALOG_TRIM);
			fFloatingThermo = new ThermometerComposite(fFloatShell, SWT.BORDER);
			fFloatingThermo.getThermo().setShowMarkers(false);
			fFloatingThermo.getThermo().setShowMinorTicks(false);
			fFloatShell.setLocation(0, 0);
			fFloatShell.setSize(120, 300);
			fFloatShell.setLayout(new FillLayout());
			fFloatShell.setAlpha(220);
			fFloatShell.layout();
			fFloatShell.open();
			fFloatingThermo.setTemperature(fTemp);
		}
	}

	private void dispatch(Oscilloscope oscilloscope, int pPin) {
		new OscilloscopeDispatcher(oscilloscope) {

			@Override
			public int getDelayLoop() {
				return 40;
			}

			@Override
			public boolean getFade() {
				return false;
			}

			public int getPulse() {
				return new Random().nextInt(40) + 60;
			};

			public int getTailSize() {
				return Oscilloscope.TAILSIZE_DEFAULT;
			};

			public boolean isServiceActive() {
				if (pPin == PIN_COOK) {
					return fCookPin != null;
				}
				if (pPin == PIN_LIGHT) {
					return fLightPin != null;
				}
				return false;
			};

			public Color getActiveForegoundColor() {
				return getOscilloscope().getDisplay().getSystemColor(
						SWT.COLOR_GREEN);
			};

			public Color getInactiveForegoundColor() {
				return getOscilloscope().getDisplay().getSystemColor(
						SWT.COLOR_RED);
			};

			@Override
			public void hookSetValues(int value) {
				if (isServiceActive())
					getOscilloscope().setValues(0, Oscilloscope.HEARTBEAT);
			}
		}.dispatch();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void setCookPin(IGPIOPinOutput pPin) {
		this.fCookPin = pPin;
		if (fCookPin == null) {
			fBtnCook.setEnabled(false);
		} else {
			fBtnCook.setEnabled(fAdmin ? true : false);
		}
	}

	public void setLightPin(IGPIOPinOutput pPin) {
		this.fLightPin = pPin;
		if (fLightPin == null) {
			fBtnLight.setEnabled(false);
		} else {
			fBtnLight.setEnabled(true);
		}
	}

	public void setTemperature(Double pTemp) {
		fTemp = pTemp;
		fThermo.setTemperature(pTemp);
		if (pTemp >= fKnob.getKnob().getValue() && isCooking()) {
			stopCooking();
		}
		if (fFloatingThermo != null && !fFloatingThermo.isDisposed()) {
			fFloatingThermo.setTemperature(pTemp);
		}
	}
}
