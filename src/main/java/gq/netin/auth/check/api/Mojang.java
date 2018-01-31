package gq.netin.auth.check.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import gq.netin.auth.check.Check.CheckAPI;
import gq.netin.auth.check.Verify;

/**
 *
 * @author netindev
 *
 */
public class Mojang implements Verify {

	private boolean result;

	@Override
	public boolean verify(String playerName) {
		try {
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(new URL(CheckAPI.MOJANG_API.getLink() + playerName).openStream()));
			this.setResult(true);
			return reader.readLine() != null;
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
