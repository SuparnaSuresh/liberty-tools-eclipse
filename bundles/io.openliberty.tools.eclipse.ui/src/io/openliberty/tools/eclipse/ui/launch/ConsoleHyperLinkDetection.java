package io.openliberty.tools.eclipse.ui.launch;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import io.openliberty.tools.eclipse.logging.Trace;

public class ConsoleHyperLinkDetection implements IPatternMatchListenerDelegate {
	private TextConsole console;

	@Override
	public void connect(TextConsole console) {
		this.console = console;
	}

	@Override
	public void disconnect() {
		console = null;
	}

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
	public void matchFound(PatternMatchEvent event) {
		try {
			int offset = event.getOffset();
			int length = event.getLength();
			String externalUrl = console.getDocument().get(event.getOffset(), event.getLength());
			if (!(externalUrl.isBlank())) {
				String urlRegex = "https?://[a-zA-Z0-9.-]+(?:/[a-zA-Z0-9&%_.-]*)?";
				Pattern pattern = Pattern.compile(urlRegex);
				Matcher matcher = pattern.matcher(externalUrl);
				if (matcher.find()) {
					int urlStartPosition = matcher.start();
					String urlFinder = externalUrl.substring(urlStartPosition);
					String[] wordsAfterUrl = urlFinder.split("[\\s\\n]+");
					if (wordsAfterUrl.length > 1) {
						urlFinder = wordsAfterUrl[0];
					}
//					URI uri = URI.create(urlFinder);
					String url = urlFinder;
					IHyperlink link = new AbstractHyperlink() {
						@Override
						public void linkActivated() {
							try {
								IWorkbenchBrowserSupport bs = PlatformUI.getWorkbench().getBrowserSupport();
								IWebBrowser b = bs.createBrowser(
										IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR,
										null, null, null);
								b.openURL(new URL(url));
							} catch (Exception e) {
								String msg = "An error occurred while adding hyperlinks to external URLs";
								if (Trace.isEnabled()) {
									Trace.getTracer().trace(Trace.TRACE_TOOLS, msg, e);
								}

							}
						}
					};
					console.addHyperlink(link, offset + urlStartPosition, urlFinder.length() - 2);
				}
			}

		} catch (Exception e) {
			String msg = "An error occurred while adding hyperlinks to external URLs";
			if (Trace.isEnabled()) {
				Trace.getTracer().trace(Trace.TRACE_TOOLS, msg, e);
			}
		}
	}
}