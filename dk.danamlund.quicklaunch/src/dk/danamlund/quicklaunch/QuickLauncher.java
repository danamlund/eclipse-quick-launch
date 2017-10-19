package dk.danamlund.quicklaunch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchManager;

public class QuickLauncher {
	private final String runMode;
	private final QuickLaunchConfigurationDialog dialog;
	private final ReloadLaunchersListener listener;

	public QuickLauncher(String runMode) {
		this.runMode = runMode;
		this.dialog = new QuickLaunchConfigurationDialog(runMode);

		reloadLaunchers();

		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		listener = new ReloadLaunchersListener();
		launchManager.addLaunchConfigurationListener(listener);
	}

	public void showQuickLaunchDialog() throws ExecutionException {
		dialog.open();
	}

	private void reloadLaunchers() {
		try {
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			List<ILaunchConfiguration> launchConfigurations = new ArrayList<>();
			for (ILaunchConfiguration launchConfiguration : launchManager.getLaunchConfigurations()) {
				if (launchConfiguration.supportsMode(runMode)) {
					launchConfigurations.add(launchConfiguration);
				}
			}
			dialog.setElements(launchConfigurations.toArray());
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
	}

	private class ReloadLaunchersListener implements ILaunchConfigurationListener {
		@Override
		public void launchConfigurationAdded(ILaunchConfiguration configuration) {
			reloadLaunchers();
		}

		@Override
		public void launchConfigurationChanged(ILaunchConfiguration configuration) {
			reloadLaunchers();
		}

		@Override
		public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
			reloadLaunchers();
		}
	}

	public void dispose() {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		launchManager.removeLaunchConfigurationListener(listener);
	}
}
