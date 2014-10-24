package com.remainsoftware.egg.ui.parts;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.ecf.raspberrypi.gpio.IGPIOPin;
import org.eclipse.ecf.raspberrypi.gpio.IGPIOPinOutput;
import org.eclipse.ecf.raspberrypi.gpio.ILM35;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import com.remainsoftware.egg.core.OSGiUtil;

public class ServiceControlComposite extends Composite implements
		SelectionListener {
	private TableViewer fTableViewer;
	private HashMap<Integer, Button> fBtnPins = new HashMap<>();
	private HashMap<Integer, ServiceReference<IGPIOPinOutput>> fServiceList = new HashMap<>();
	private Label fLblTemp;
	private double fTemp;
	private ServiceRegistration<?> fTempService;
	private boolean fAdmin = "true".equals(System.getProperty("admin"));

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public ServiceControlComposite(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(2, false));

		Label lblNewLabel_1 = new Label(this, SWT.NONE);
		lblNewLabel_1.setText("Temperature:");
		fLblTemp = new Label(this, SWT.NONE);
		fLblTemp.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		fLblTemp.setFont(SWTResourceManager.getFont("Segoe UI", 12, SWT.BOLD));
		fLblTemp.setBackground(SWTResourceManager
				.getColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		fLblTemp.setText(" 00.0C ");

		initPins(parent);

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2,
				1));
		composite.setBounds(0, 0, 64, 64);
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);

		fTableViewer = new TableViewer(composite, SWT.BORDER
				| SWT.FULL_SELECTION);
		Table table = fTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(
				fTableViewer, SWT.NONE);
		TableColumn tblclmnProperty = tableViewerColumn.getColumn();
		tcl_composite.setColumnData(tblclmnProperty, new ColumnWeightData(1,
				ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmnProperty.setText("property");

		TableViewerColumn tableViewerColumn_1 = new TableViewerColumn(
				fTableViewer, SWT.NONE);
		TableColumn tblclmnValue = tableViewerColumn_1.getColumn();
		tcl_composite.setColumnData(tblclmnValue, new ColumnWeightData(1,
				ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmnValue.setText("value");
		fTableViewer.setContentProvider(new ArrayContentProvider());
		fTableViewer.setLabelProvider(new PropertyLabelProvider());
	}

	private void initPins(Composite parent) {
		addPin(parent, 0);
		addPin(parent, 1);
		activatePins();
	}

	private void activatePins() {
		fBtnPins.values().forEach(
				btnPin -> btnPin.setEnabled(isPinServiceAvailable(btnPin)));
	}

	private boolean isPinServiceAvailable(Button pBtnPin) {
		Integer pin = (Integer) pBtnPin.getData("pin");
		boolean serviceAvailable = fServiceList.get(pin) != null;
		return serviceAvailable & authorizedToPin(pin);
	}

	private void addPin(Composite parent, int i) {
		new Label(this, SWT.NONE).setText(getPinText(i));
		Button btnPin = new Button(this, SWT.CHECK);
		btnPin.setData("pin", i);
		btnPin.addSelectionListener(this);
		fBtnPins.put(i, btnPin);
	}

	private String getPinText(int i) {
		switch (i) {
		case 0:
			return "Egg Cooker (" + i + ")";
		case 1:
			return "Light (" + i + ")";
		default:
			break;
		}
		return "??";
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	private class PropertyLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public PropertyLabelProvider() {
			super();
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return element.toString();
			case 1:
				ServiceReference<IGPIOPinOutput> serviceRef = fServiceList
						.values().iterator().next();
				return propertyToString(serviceRef.getProperty(element
						.toString()));
			}
			return "??";
		}

		private String propertyToString(Object pValue) {
			if (pValue instanceof Object[]) {
				return Arrays.deepToString((Object[]) pValue);
			}
			return pValue.toString();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		Integer pin = (Integer) e.widget.getData("pin");
		if (authorizedToPin(pin))
			OSGiUtil.getService(fServiceList.get(pin), this).toggle();
	}

	private boolean authorizedToPin(Integer pin) {
		return pin != 0 || fAdmin;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/**
	 * Sets the service and activates the UI. Must therefore be called in the UI
	 * thread.
	 * 
	 * @param pServiceRef
	 */
	public void addPinService(ServiceReference<IGPIOPinOutput> pServiceRef) {
		Integer pin = getPinFromServiceRef(pServiceRef);
		if (pin == null) {
			System.out.println("Invalid pin null");
			return;
		}
		fServiceList.put(pin, pServiceRef);
		activateUI(pServiceRef);
		registerTemperatureService();
	}

	private Integer getPinFromServiceRef(
			ServiceReference<IGPIOPinOutput> pServiceRef) {
		Integer pin = Integer.valueOf((String) pServiceRef
				.getProperty(IGPIOPin.PIN_ID_PROP));
		return pin;
	}

	private void registerTemperatureService() {

		if (fTempService != null) {
			return;
		}

		System.out.println("Registering temperature service");

		fTempService = OSGiUtil.RegisterService(ILM35.class, new ILM35() {
			@Override
			public void setTemperature(String pHost, double pTemperature) {
				System.out.println("Temperature " + pTemperature);
				fTemp = pTemperature;
				getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						fLblTemp.setText(prettyTemp(fTemp));
					}
				});

			}
		}, this);

	}

	private String prettyTemp(double pTemperature) {
		String ds = String.valueOf(pTemperature);
		return " " + ds.substring(0, ds.indexOf(".") + 2) + "C ";
	}

	protected void activateUI(ServiceReference<IGPIOPinOutput> pServiceRef) {
		activatePins();
		fTableViewer.setInput(pServiceRef.getPropertyKeys());
	}

	/**
	 * Factory method to get a Composite with a tab.
	 * 
	 * @param pTabFolder
	 * @return
	 */
	public static CTabItem createTabItem(CTabFolder pTabFolder) {
		CTabItem tabItem = new CTabItem(pTabFolder, SWT.NONE);
		tabItem.setControl(new ServiceControlComposite(pTabFolder, SWT.NONE));
		return tabItem;
	}

	@Override
	public void dispose() {
		System.out.println("Disposing tab.");
		unRegisterServiceAsync();
		super.dispose();
	}

	private void unRegisterServiceAsync() {
//		Thread thread = new Thread(new Runnable() {
//			@Override
//			public void run() {
				fTempService.unregister();
				fTempService = null;
//			}
//		});
//		thread.setName("Unregistering service (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=448466)");
//		thread.setDaemon(true);
//		thread.start();
	}

	public void removeService(ServiceReference<IGPIOPinOutput> pReference) {
		fServiceList.remove(getPinFromServiceRef(pReference));
		activatePins();
	}

	public int getServiceCount() {
		return fServiceList.size();
	}
}
