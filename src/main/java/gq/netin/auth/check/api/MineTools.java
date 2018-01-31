package gq.netin.auth.check.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import gq.netin.auth.check.Check.CheckAPI;
import gq.netin.auth.check.Verify;

/**
 *
 * @author netindev
 *
 */
public class MineTools implements Verify {

	private boolean result;

	@Override
	public boolean verify(String playerName) {
		try {
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(new URL(CheckAPI.MINETOOLS.getLink() + playerName).openStream()));
			List<String> list = new ArrayList<>();
			String read;
			while ((read = reader.readLine()) != null) {
				list.add(read);
			}
			for (String string : list) {
				if (string.contains("No UUID was found for that username")) {
					this.setResult(true);
					return false;
				} else if (string.contains("\"status\": \"OK\"")) {
					this.setResult(true);
					return true;
				}
			}
			this.setResult(false);
			return false;
		} catch (Exception e) {
			this.setResult(false);
			return false;
		}
	}

	private void setResult(boolean result) {
		this.result = result;
	}

	@Override
	public boolean getResult() {
		return this.result;
	}

}
