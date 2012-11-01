/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.client.shell;

import java.io.IOException;

import org.overlord.sramp.client.shell.commands.InvalidCommandArgumentException;


/**
 * An interactive shell for working with an s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampShell {

	/**
	 * Main entry point.
	 * @param args
	 */
	public static void main(String [] args) {
		final SrampShell shell = new SrampShell();
		Thread shutdownHook = new Thread(new Runnable() {
			@Override
			public void run() {
				shell.shutdown();
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		try {
			shell.run(args);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.out.println("Exiting s-ramp shell due to an error.");
		}
	}

	private ShellCommandFactory factory = new ShellCommandFactory();
	private ShellContextImpl context = new ShellContextImpl();
	private ShellCommandReader reader;

	/**
	 * Constructor.
	 */
	public SrampShell() {
	}

	/**
	 * Runs the shell.
	 * @param args
	 * @throws Exception
	 */
	public void run(String[] args) throws Exception {
		reader = createCommandReader(args);
		displayWelcomeMessage();
		boolean done = false;
		while (!done) {
			ShellCommand command = reader.read();
			try {
				if (command == null) {
					done = true;
				} else {
					command.execute(context);
				}
			} catch (InvalidCommandArgumentException e) {
				System.out.println("Invalid argument:  " + e.getMessage());
				System.out.print("Usage:  ");
				command.printUsage();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	/**
	 * Creates an appropriate {@link ShellCommandReader} based on the command line
	 * arguments and the current runtime environment.
	 * @param args
	 * @throws IOException
	 */
	protected ShellCommandReader createCommandReader(String[] args) throws IOException {
		ShellCommandReader commandReader = null;
		if (args.length >= 2 && "-f".equals(args[0])) {
			String filePath = args[1];
			commandReader = new FileShellCommandReader(factory, filePath);
		} else if (args.length == 1 && "-simple".equals(args[0])) {
			if (System.console() != null) {
				commandReader = new ConsoleShellCommandReader(factory);
			} else {
				commandReader = new StdInShellCommandReader(factory);
			}
		} else {
			if (System.console() != null) {
				commandReader = new InteractiveShellCommandReader(factory);
			} else {
				commandReader = new StdInShellCommandReader(factory);
			}
		}
		commandReader.open();
		return commandReader;
	}

	/**
	 * Shuts down the shell.
	 */
	public void shutdown() {
		System.out.print("S-RAMP shell shutting down...");
		try { this.reader.close(); } catch (IOException e) { }
		this.context.destroy();
		System.out.println("done.");
	}

	/**
	 * Displays a welcome message to the user.
	 */
	private void displayWelcomeMessage() {
		System.out.println("******************************************************************************************************");
		System.out.println("*                     _________         __________    _____      _____ __________                    *");
		System.out.println("*                    /   _____/         \\______   \\  /  _  \\    /     \\\\______   \\                   *");
		System.out.println("*                    \\_____  \\   ______  |       _/ /  /_\\  \\  /  \\ /  \\|     ___/                   *");
		System.out.println("*                    /        \\ /_____/  |    |   \\/    |    \\/    Y    \\    |                       *");
		System.out.println("*                   /_______  /          |____|_  /\\____|__  /\\____|__  /____|                       *");
		System.out.println("*                           \\/                  \\/         \\/         \\/                             *");
		System.out.println("* JBoss S-RAMP Kurt Stam and Eric Wittmann, Licensed under the Apache License, V2.0, Copyright 2012  *");
		System.out.println("******************************************************************************************************\n");
	}
}
