package gq.netin.auth.check;

/**
 *
 * @author netindev
 *
 */
public abstract interface Verify {

	public abstract boolean verify(String playerName);

	public abstract boolean getResult();

}
