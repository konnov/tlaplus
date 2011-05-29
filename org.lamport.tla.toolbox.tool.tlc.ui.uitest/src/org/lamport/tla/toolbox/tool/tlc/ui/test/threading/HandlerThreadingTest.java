package org.lamport.tla.toolbox.tool.tlc.ui.test.threading;

import java.io.File;

import junit.framework.Assert;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lamport.tla.toolbox.test.RCPTestSetupHelper;
import org.lamport.tla.toolbox.test.threading.MonitorAdaptor;

@RunWith(SWTBotJunit4ClassRunner.class)
public class HandlerThreadingTest {
	 
	private static SWTWorkbenchBot bot;

	private static final String specA = System
			.getProperty("org.lamport.tla.toolbox.tool.tlc.ui.test.PathToSpecA");
	private static final String specB = System
			.getProperty("org.lamport.tla.toolbox.tool.tlc.ui.test.PathToSpecB");
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		
		// If this assert fails see http://wiki.eclipse.org/JDT_weaving_features
		final String osgiFrameworkExtensions = System.getProperty("osgi.framework.extensions");
		Assert.assertNotNull(
				"Test requires Aspectj weaving hook property to be present as an indicator for active weaving support",
				osgiFrameworkExtensions);
		
		// Reset the workbench
		RCPTestSetupHelper.beforeClass();
		
		// check to make sure the given spec files exist
		Assert.assertTrue("Given spec file does not exist: " + specA, new File(
				specA).exists());
		Assert.assertTrue("Given spec file does not exist: " + specA, new File(
				specB).exists());
		
		bot = new SWTWorkbenchBot();
	}

	/**
	 * Adds a new spec to the toolbox, opens it
	 * and tests if parsing is done on a non-UI thread
	 * 
	 * @see http://bugzilla.tlaplus.net/show_bug.cgi?id=103
	 */
	@Test
	public void parseSpecInNonUIThread() {
		
		// Open specA 
		SWTBotMenu fileMenu = bot.menu("File");
		SWTBotMenu openSpecMenu = fileMenu.menu("Open Spec");
		SWTBotMenu addNewSpecMenu = openSpecMenu.menu("Add New Spec...");
		addNewSpecMenu.click();

		bot.textWithLabel("Root-module file:").setText(specA);
		bot.button("Finish").click();
		
		assertNoBackendCodeInUIThread();

		// Open specB
		addNewSpecMenu.click();

		bot.textWithLabel("Root-module file:").setText(specB);
		bot.button("Finish").click();
		
		assertNoBackendCodeInUIThread();
		
		// Go back to previous spec
		openSpecMenu.menu(getSpecName(specA));

		assertNoBackendCodeInUIThread();
	}

	/**
	 * Checks how many invocations of backend code have happend inside the UI thread
	 */
	private void assertNoBackendCodeInUIThread() {
		
		// Check if MonitorAspect has been enabled
		Assert.assertTrue("Test requires active MonitorAspect aspect!", MonitorAdaptor.aspectIsActive());

		Assert.assertFalse(
				"Backend code (e.g. parsing must not be executed in UI thread) times executed: "
						+ MonitorAdaptor.getTriggeredJoinPoints().size(),
				MonitorAdaptor.hasTriggeredBackendCode());
		// Resets the counting adapter for UI thread backend invocations
		MonitorAdaptor.reset();
	}

	private String getSpecName(final String aSpec) {
		String specName = aSpec.substring(0, aSpec.lastIndexOf("."));
		return specName.substring(specName.lastIndexOf(File.separator) + 1);
	}
}
