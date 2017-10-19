package dk.danamlund.quicklaunch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.FilteredList.FilterMatcher;

public class QuickLaunchConfigurationDialog extends ElementListSelectionDialog {
	private final String runMode;

	public QuickLaunchConfigurationDialog(String runMode) {
		super(null, new QuickLaunchConfigurationLabelProvider());
		this.runMode = runMode;

		setBlockOnOpen(false);
	}

	@Override
	protected FilteredList createFilteredList(Composite parent) {
		FilteredList list = super.createFilteredList(parent);
		list.setFilterMatcher(new FuzzyFilterMatcher());
		return list;
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
	private static class FuzzyFilterMatcher implements FilterMatcher {
		private String pattern = "";

		@Override
		public void setFilter(String pattern, boolean ignoreCase, boolean ignoreWildCards) {
			this.pattern = pattern.toLowerCase().trim();
		}

		@Override
		public boolean match(Object element) {
			if (pattern.isEmpty()) {
				return true;
			}
			String s = element.toString().toLowerCase().trim();
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
	}
}
