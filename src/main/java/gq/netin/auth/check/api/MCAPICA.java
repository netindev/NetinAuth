package gq.netin.auth.check.api;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import gq.netin.auth.check.Check.CheckAPI;
import gq.netin.auth.check.Verify;

/**
 *
 * @author netindev
 *
 */
public class MCAPICA implements Verify {

	private boolean result;

	@Override
	public boolean verify(String playerName) {
		try {
			BufferedImage image = ImageIO.read(new URL(CheckAPI.MCAPI_CA.getLink() + playerName));
			try {
				if (image.getHeight() > 0) {
					this.setResult(true);
					return true;
				}
			} catch (Exception e) {
				this.setResult(true);
				return false;
			}
		} catch (Exception e) {
			this.setResult(false);
			return false;
		}
		return this.result;
	}

	private void setResult(boolean result) {
		this.result = result;
	}

	@Override
	public boolean getResult() {
		return this.result;
	}

}
