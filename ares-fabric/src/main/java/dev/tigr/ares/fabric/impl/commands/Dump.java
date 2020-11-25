package dev.tigr.ares.fabric.impl.commands;

/**
 * @author Tigermouthbear 6/19/20
 */
public class Dump {
    public Dump() {
		/*super("dump", "Dumps all resources in Java classpath to .minecraft/dump");

		register(LiteralArgumentBuilder.literal("dump").executes(c -> {
			EXECUTOR.execute(() -> {
				UTILS.printMessage("Starting dump...");
				long start = System.currentTimeMillis();

				Set<String> set = new Reflections("", new ResourcesScanner()).getResources(name -> true);

				int i = 1;
				for(String name: set) {
					File file = new File("dump/" + name);
					file.getParentFile().mkdirs();

					InputStream in = Dump.class.getResourceAsStream("/" + name);

					if(in == null) {
						UTILS.printMessage("Cannot find resource!");
						continue;
					}

					FileOutputStream out;
					try {
						out = new FileOutputStream(file);

						byte[] buffer = new byte[1024];
						int len = in.read(buffer);
						while(len != -1) {
							out.write(buffer, 0, len);
							len = in.read(buffer);
						}

						out.close();
					} catch(IOException e) {
						e.printStackTrace();
					}

					if(i++ % 10 == 0) UTILS.printMessage(i + "/" + set.size() + " resources remaining");
				}

				UTILS.printMessage("Dumped " + set.size() + " resources in " + (System.currentTimeMillis() - start) + " milliseconds!");
			});

			return 1;
		}));*/
    }
}
