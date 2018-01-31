package gq.netin.auth.util;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import gq.netin.auth.Bukkit;
import gq.netin.auth.version.Version;

/**
 *
 * @author netindev
 *
 */
public class Config {

	private static final FileConfiguration[] CONFIGURATIONS = new FileConfiguration[3];
	private static final String TRANSLATION_PATH = Bukkit.getPlugin().getDataFolder() + "/translations/",
			DATA_PATH = Bukkit.getPlugin().getDataFolder() + "/data/";
	private static final String[] PATHS = { Config.TRANSLATION_PATH + "pt_br.yml",
			Config.TRANSLATION_PATH + "en_us.yml", Config.DATA_PATH + "data.yml" };

	public enum ConfigType {
		PT_BR(Config.CONFIGURATIONS[0]), EN_US(Config.CONFIGURATIONS[1]), DATA(Config.CONFIGURATIONS[2]), CONFIG(
				Bukkit.getPlugin().getConfig());

		private FileConfiguration fileConfiguration;

		private ConfigType(FileConfiguration fileConfiguration) {
			this.fileConfiguration = fileConfiguration;
		}

		public FileConfiguration getConfig() {
			return this.fileConfiguration;
		}
	}

	public static String[] getPaths() {
		return Config.PATHS;
	}

	public static void loadConfig() {
		Bukkit.getPlugin().saveDefaultConfig();
		if (!new File(Bukkit.getPlugin().getDataFolder() + "/translations").exists()) {
			new File(Bukkit.getPlugin().getDataFolder() + "/translations").mkdir();
		}
		if (!new File(Bukkit.getPlugin().getDataFolder() + "/data").exists()) {
			new File(Bukkit.getPlugin().getDataFolder() + "/data").mkdir();
		}
		for (int i = 0; i < Config.PATHS.length; i++) {
			File file = new File(Config.PATHS[i]);
			if (!file.exists()) {
				try {
					file.createNewFile();
					if (Version.is1_7()) {
						net.minecraft.util.org.apache.commons.io.FileUtils.copyInputStreamToFile(Bukkit.getPlugin()
								.getResource(file.getAbsolutePath()
										.replace(Bukkit.getPlugin().getDataFolder().getAbsolutePath(), "").substring(1)
										.replace("\\", "/")),
								file);
					} else {
						org.apache.commons.io.FileUtils.copyInputStreamToFile(Bukkit.getPlugin()
								.getResource(file.getAbsolutePath()
										.replace(Bukkit.getPlugin().getDataFolder().getAbsolutePath(), "").substring(1)
										.replace("\\", "/")),
								file);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (file.getAbsolutePath().contains(".yml")) {
				Config.CONFIGURATIONS[i] = YamlConfiguration.loadConfiguration(file);
			}
		}
	}

}
