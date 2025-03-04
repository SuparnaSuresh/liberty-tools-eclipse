package io.openliberty.tools.eclipse.ui.launch;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.console.IHyperlink;

public class ConsoleLineTracker implements IConsoleLineTracker {

	private IConsole console;

	class AbstractHyperlink implements IHyperlink {
		@Override
		public void linkEntered() {
			// do nothing
		}

		@Override
		public void linkExited() {
			// do nothing
		}

		@Override
		public void linkActivated() {
			// do nothing
		}
	}

	@Override
	public void init(IConsole console) {
		this.console = console;
	}

	@Override
	public void lineAppended(IRegion line) {
		try {
			int offset = line.getOffset();
			int length = line.getLength();
			String consoleLog = console.getDocument().get(offset, length);
			String urlRegex = "https?://[a-zA-Z0-9.-]+(?:/[a-zA-Z0-9&%_.-]*)?";
			Pattern pattern = Pattern.compile(urlRegex);
			Matcher matcher = pattern.matcher(consoleLog);
			if (matcher.find()) {
				int urlStartPosition = matcher.start();

				String urlFinder = consoleLog.substring(urlStartPosition);
				String[] wordsAfterUrl = urlFinder.split(" ");
				if (wordsAfterUrl.length > 1) {
					urlFinder = wordsAfterUrl[0];
				}
				String url = urlFinder;
				Path path = Paths.get(url);
				URI uri = path.toUri();

				IHyperlink link = new AbstractHyperlink() {
					@Override
					public void linkActivated() {
						try {
							IWorkbenchBrowserSupport bs = PlatformUI.getWorkbench().getBrowserSupport();
							IWebBrowser b = bs.createBrowser(
									IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR,
									null, null, null);
							b.openURL(uri.toURL());
						} catch (Exception e) {

						}
					}
				};
				console.addLink(link, offset + urlStartPosition, urlFinder.length() - 1);
			}

		} catch (Exception e) {

		}
	}

	@Override
	public void dispose() {
		// do nothing
	}

}
