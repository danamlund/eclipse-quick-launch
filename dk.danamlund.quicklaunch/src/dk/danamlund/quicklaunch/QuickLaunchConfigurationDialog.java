package dk.danamlund.quicklaunch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.FilteredList.FilterMatcher;

public class QuickLaunchConfigurationDialog extends ElementListSelectionDialog {
	private final String runMode;
	private Object previousSelection = null;
	private List<ILaunchConfiguration> launchConfigurations;

	public QuickLaunchConfigurationDialog(String runMode) {
		super(null, new QuickLaunchConfigurationLabelProvider());
		this.runMode = runMode;

		setBlockOnOpen(false);

		setTitle(capitalize(runMode) + " Launch Configuration");
		setMessage("Enter fuzzy pattern:");
	}

	private static String capitalize(String s) {
		if (s.isEmpty()) {
			return "";
		}
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public void setLaunchConfigurations(List<ILaunchConfiguration> launchConfigurations) {
		this.launchConfigurations = launchConfigurations;
		setElements(this.launchConfigurations.toArray());
	}

	@Override
	public int open() {
		int output = super.open();

		// Running initially selected element caused ok button to be disabled
		// on next run until you search for something.
		// This fixes that, without me having to understand why it happens.
		Button okButton = getOkButton();
		if (okButton != null) {
			okButton.setEnabled(true);
		}

		if (previousSelection != null) {
			setSelection(new Object[] { previousSelection });
		}

		return output;
	}

	@Override
	protected FilteredList createFilteredList(Composite parent) {
		FilteredList list = super.createFilteredList(parent);
		// Set dummy filters and comparators, do everything in doFilter
		list.setFilterMatcher(new TrueFilterMatcher());
		list.setComparator(new AllEqualComparator());
		return list;
	}

	@Override
	protected Text createFilterText(Composite parent) {
		Text text = super.createFilterText(parent);

		// Dont trigger FilterList filter
		for (Listener listener : text.getListeners(SWT.Modify)) {
			text.removeListener(SWT.Modify, listener);
		}

		text.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				doFilter(text.getText());
			}
		});

		return text;
	}

	private void doFilter(String pattern) {
		pattern = pattern.toLowerCase();

		List<ILaunchConfiguration> filtered = new ArrayList<>();
		for (ILaunchConfiguration launchConfig : launchConfigurations) {
			if (fuzzyMatch(launchConfig.toString(), pattern)) {
				filtered.add(launchConfig);
			}
		}
		filtered.sort(new FuzzyScoreComparator(pattern));

		// Naively update list with filtered elements.
		fFilteredList.setElements(filtered.toArray());
	}

	@Override
	protected void okPressed() {
		super.okPressed();
		Object[] results = getResult();
		if (results.length == 1) {
			ILaunchConfiguration launchConfiguration = (ILaunchConfiguration) results[0];
			try {
				launchConfiguration.launch(runMode, new NullProgressMonitor(), true, true);
			} catch (CoreException e) {
				throw new IllegalStateException(e);
			}
			previousSelection = launchConfiguration;
		}
	}

	private static class QuickLaunchConfigurationLabelProvider extends LabelProvider {
		final IDebugModelPresentation debugModelPresentation = DebugUITools.newDebugModelPresentation();

		@Override
		public Image getImage(Object element) {
			return debugModelPresentation.getImage(element);
		}
	}

	/**
	 * fuzzy 'foo' is same as regular '*f*o*o*'.
	 */
	private static boolean fuzzyMatch(String element, String pattern) {
		if (pattern.isEmpty()) {
			return true;
		}
		String s = element.toLowerCase().trim();
		int sIndex = 0;
		for (int i = 0; i < pattern.length(); i++) {
			if (sIndex >= s.length()) {
				return false;
			}
			sIndex = s.indexOf(pattern.charAt(i), sIndex);
			if (sIndex < 0) {
				return false;
			}
			sIndex++;
		}
		return true;
	}

	private static int getFuzzyScore(String name, String filter) {
		int score = 0;
		int nameI = 0;
		for (int filterI = 0; filterI < filter.length(); filterI++) {
			int newNameI = name.indexOf(filter.charAt(filterI), nameI);
			if (newNameI == nameI) {
				score++;
			}
			nameI = newNameI + 1;
		}
		return score;
	}

	private static class TrueFilterMatcher implements FilterMatcher {
		@Override
		public boolean match(Object element) {
			return true;
		}

		@Override
		public void setFilter(String pattern, boolean ignoreCase, boolean ignoreWildCards) {
		}
	}

	private static class AllEqualComparator implements Comparator<Object> {
		@Override
		public int compare(Object o1, Object o2) {
			return 0;
		}
	}

	private static class FuzzyScoreComparator implements Comparator<Object> {
		private final String pattern;

		public FuzzyScoreComparator(String pattern) {
			this.pattern = pattern;
		}

		@Override
		public int compare(Object aObject, Object bObject) {
			String a = aObject.toString().toLowerCase();
			String b = bObject.toString().toLowerCase();
			int comparison = Integer.compare(getFuzzyScore(a, pattern), getFuzzyScore(b, pattern));
			if (comparison != 0) {
				return comparison;
			}
			return a.compareTo(b);
		}
	}
}
