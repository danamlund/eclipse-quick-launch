package dk.danamlund.quicklaunch;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.ILaunchManager;

public class QuickRunLaunchConfigurationHandler extends AbstractHandler {
	private QuickLauncher quickLauncher = null;

	@Override
	public synchronized Object execute(ExecutionEvent event) throws ExecutionException {
		if (quickLauncher == null) {
			quickLauncher = new QuickLauncher(ILaunchManager.RUN_MODE);
		}
		quickLauncher.showQuickLaunchDialog();
		return null;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (quickLauncher != null) {
			quickLauncher.dispose();
		}
	}
}
