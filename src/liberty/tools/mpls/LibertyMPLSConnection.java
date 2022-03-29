package liberty.tools.mpls;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

import liberty.tools.LibertyDevPlugin;

public class LibertyMPLSConnection extends ProcessStreamConnectionProvider {

	public LibertyMPLSConnection() {
		List<String> commands = new ArrayList<>();
		commands.add(computeJavaPath());
		//commands.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000");
		commands.add("-classpath");
		try {
			//URL url = FileLocator.toFileURL(getClass().getResource("/server/org.eclipse.lsp4mp.ls-uber.jar"));
			//commands.add(new java.io.File(url.getPath()).getAbsolutePath());
			//commands.add("org.eclipse.lsp4mp.ls.MicroProfileServerLauncher");
			//setCommands(commands);
			//setWorkingDirectory(System.getProperty("user.dir"));
			commands.add(computeClasspath());
			commands.add("org.eclipse.lsp4mp.ls.MicroProfileServerLauncher");
			setCommands(commands);
			setWorkingDirectory(System.getProperty("user.dir"));			
		} catch (IOException e) {
			LibertyDevPlugin.getDefault().getLog().log(new Status(IStatus.ERROR,
					LibertyDevPlugin.getDefault().getBundle().getSymbolicName(), e.getMessage(), e));
		}
	}

	private String computeClasspath() throws IOException {
		StringBuilder builder = new StringBuilder();
		URL url = FileLocator.toFileURL(getClass().getResource("/server/org.eclipse.lsp4mp.ls-uber.jar"));
		builder.append(new java.io.File(url.getPath()).getAbsolutePath());
		//builder.append(File.pathSeparatorChar);
		//url = FileLocator.toFileURL(getClass().getResource("/server/org.eclipse.lsp4mp.jdt.core-0.4.0-SNAPSHOT.jar"));
		//builder.append(new java.io.File(url.getPath()).getAbsolutePath());
		return builder.toString();
	}
	
	private String computeJavaPath() {
		String javaPath = "java";
		boolean existsInPath = Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator))).map(Paths::get)
				.anyMatch(path -> Files.exists(path.resolve("java")));
		if (!existsInPath) {
			File f = new File(System.getProperty("java.home"),
					"bin/java" + (Platform.getOS().equals(Platform.OS_WIN32) ? ".exe" : ""));
			javaPath = f.getAbsolutePath();
		}
		return javaPath;
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {
		/*QuarkusGeneralClientSettings settings = new QuarkusGeneralClientSettings();
		QuarkusCodeLensSettings codeLensSettings = new QuarkusCodeLensSettings();
		codeLensSettings.setUrlCodeLensEnabled(true);
		settings.setCodeLens(codeLensSettings);
		return settings;*/
		Map<String, Object> root = new HashMap<>();
		Map<String, Object> settings = new HashMap<>();
		Map<String, Object> quarkus = new HashMap<>();
		Map<String, Object> tools = new HashMap<>();
		Map<String, Object> trace = new HashMap<>();
		trace.put("server", "verbose");
		tools.put("trace", trace);
		Map<String, Object> codeLens = new HashMap<>();
		codeLens.put("urlCodeLensEnabled", "true");
		tools.put("codeLens", codeLens);
		quarkus.put("tools", tools);
		settings.put("microprofile", quarkus);
		root.put("settings", settings);
		Map<String, Object> extendedClientCapabilities = new HashMap<>();
		Map<String, Object> commands = new HashMap<>();
		Map<String, Object> commandsKind = new HashMap<>();
		commandsKind.put("valueSet", Arrays.asList("microprofile.command.configuration.update", "microprofile.command.open.uri"));
		commands.put("commandsKind", commandsKind);
		extendedClientCapabilities.put("commands", commands);
        extendedClientCapabilities.put("completion", new HashMap<>());
        extendedClientCapabilities.put("shouldLanguageServerExitOnShutdown", Boolean.TRUE);
		root.put("extendedClientCapabilities", extendedClientCapabilities);
		return root;
	}

	@Override
	public String toString() {
		return "Liberty MP Language Server: " + super.toString();
	}

}
