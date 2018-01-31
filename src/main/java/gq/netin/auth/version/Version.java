package gq.netin.auth.version;

/**
 *
 * @author netindev
 *
 */
public enum Version {

	v1_7_R1("f", "ag"), v1_7_R2("f", "ah"), v1_7_R3("f", "ai"), v1_7_R4("f", "ai"), v1_8_R1("g", "ao"), v1_8_R2("f",
			"ap"), v1_8_R3("h", "aq"), v1_9_R1("h",
					"am"), v1_9_R2("h", "am"), v1_10_R1("h", "am"), v1_11_R1("h", "an"), v1_12_R1("h", "an");

	private static final Version VERSION;

	private Version(String networkManager, String serverConnection) {
		this.networkManager = networkManager;
		this.serverConnection = serverConnection;
	}

	private String networkManager;
	private String serverConnection;

	public String getServerConnection() {
		return this.serverConnection;
	}

	public String getNetworkManager() {
		return this.networkManager;
	}

	public static boolean is1_7() {
		return Version.getPackageVersion().toString().contains("1_7");
	}

	public static Version getPackageVersion() {
		return Version.VERSION;
	}

	static {
		final String packageVersion = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().replace(".", ",")
				.split(",")[3];
		Version tempVersion = null;
		for (Version versions : Version.values()) {
			if (packageVersion.equals(versions.toString())) {
				tempVersion = versions;
				break;
			}
		}
		VERSION = tempVersion;
	}

}
