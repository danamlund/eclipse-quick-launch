package dk.danamlund.quicklaunch;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

public class QuickLaunchConfigurationDialog extends FilteredItemsSelectionDialog {
	private final String runMode;
	private final FuzzyComparator fuzzyComparator;
	private final StringFuzzyMatcher stringFuzzyMatcher;
	private List<ILaunchConfiguration> launchConfigurations;

	public QuickLaunchConfigurationDialog(String runMode) {
		this(runMode, new StringFuzzyMatcher());
	}

	public QuickLaunchConfigurationDialog(String runMode, StringFuzzyMatcher stringFuzzyMatcher) {
		super(null, false);
		this.runMode = runMode;
		this.fuzzyComparator = new FuzzyComparator(stringFuzzyMatcher);
		this.stringFuzzyMatcher = stringFuzzyMatcher;

		setListLabelProvider(new IconLabelProvider());

		setTitle(capitalize(runMode) + " Launch Configuration");
		setMessage("Enter fuzzy pattern:");

		setSelectionHistory(new ResourceSelectionHistory());
	}

	private static String capitalize(String s) {
		if (s.isEmpty()) {
			return "";
		}
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		return new DialogSettings("QuickLaunchConfigurationDialog");
	}

	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new FuzzyMatchItemsFilter();
	}

	@Override
	protected Comparator<?> getItemsComparator() {
		return fuzzyComparator;
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
			IProgressMonitor progressMonitor) throws CoreException {
		FuzzyMatchItemsFilter fuzzyFilter = (FuzzyMatchItemsFilter) itemsFilter;
		fuzzyComparator.setPattern(fuzzyFilter.getRealPattern());
		progressMonitor.beginTask("Searching", launchConfigurations.size());
		for (ILaunchConfiguration launchConfiguration : launchConfigurations) {
			contentProvider.add(launchConfiguration, itemsFilter);
			progressMonitor.worked(1);
		}
		progressMonitor.done();
	}

	@Override
	public int open() {
		setInitialPattern("");
		return super.open();
	}

	@Override
	public String getElementName(Object item) {
		return item.toString();
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

	public void setLaunchConfigurations(List<ILaunchConfiguration> launchConfigurations) {
		this.launchConfigurations = launchConfigurations;
	}

	private class FuzzyMatchItemsFilter extends ItemsFilter {
		@Override
		public boolean matchItem(Object item) {
			return stringFuzzyMatcher.fuzzyMatch(item.toString(), getRealPattern());
		}

		@Override
		public boolean isConsistentItem(Object item) {
			return true;
		}

		@Override
		public boolean isSubFilter(ItemsFilter filter) {
			// false to trigger fillContentProvider every time
			return false;
		}

		@Override
		public String getPattern() {
			// Make pattern always be non-empty so we also show results for the
			// empty pattern
			return super.getPattern() + "_";
		}

		public String getRealPattern() {
			return super.getPattern();
		}
	}

	private static class ResourceSelectionHistory extends SelectionHistory {
		@Override
		protected Object restoreItemFromMemento(IMemento element) {
			return null;
		}

		@Override
		protected void storeItemToMemento(Object item, IMemento element) {
		}

		@Override
		public synchronized void accessed(Object object) {
			// never save history
		}
	}

	private static class FuzzyComparator implements Comparator<Object> {
		private final StringFuzzyMatcher stringFuzzyMatcher;
		private String pattern = "";

		public FuzzyComparator(StringFuzzyMatcher stringFuzzyMatcher) {
			this.stringFuzzyMatcher = stringFuzzyMatcher;
		}

		@Override
		public int compare(Object a, Object b) {
			return stringFuzzyMatcher.getFuzzyScoreComparator(pattern).compare(a, b);
		}

		public void setPattern(String pattern) {
			this.pattern = pattern;
		}
	}

	private static class IconLabelProvider extends LabelProvider {
		final IDebugModelPresentation debugModelPresentation = DebugUITools.newDebugModelPresentation();

		@Override
		public Image getImage(Object element) {
			return debugModelPresentation.getImage(element);
		}
	}
}
