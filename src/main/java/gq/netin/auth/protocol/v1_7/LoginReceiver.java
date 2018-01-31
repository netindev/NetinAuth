package gq.netin.auth.protocol.v1_7;

import org.bukkit.entity.Player;

import com.comphenix.tinyprotocol.NMSReflection;
import com.comphenix.tinyprotocol.NMSReflection.FieldAccessor;
import com.comphenix.tinyprotocol.v1_7.TinyProtocol;

import gq.netin.auth.Bukkit;
import gq.netin.auth.check.Check;
import gq.netin.auth.exception.InvalidCheckException;
import gq.netin.auth.util.Util;
import gq.netin.auth.version.Version;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.io.netty.channel.Channel;

/**
 *
 * @author netindev
 *
 */
public class LoginReceiver extends TinyProtocol {

	private Version version = Version.getPackageVersion();

	private FieldAccessor<GameProfile> profileField = NMSReflection.getField("{nms}.PacketLoginInStart",
			GameProfile.class, 0);

	@Override
	public Object onPacketInAsync(Player receiver, Channel channel, Object packet) {
		if (this.profileField.hasField(packet)) {
			if (this.receiveLogin(packet, channel)) {
				return null;
			}
		}
		return super.onPacketInAsync(receiver, channel, packet);
	}

	private boolean receiveLogin(Object packet, Channel channel) {
		boolean check;
		try {
			check = Check.fastCheck(this.profileField.get(packet).getName());
		} catch (InvalidCheckException e) {
			e.printStackTrace();
			return false;
		}
		Bukkit.getStorage().setPremium(this.profileField.get(packet).getName(), check);
		if (check) {
			return false;
		}
		try {
			Class<?> loginClass = Class.forName("gq.netin.auth.version.rewrite." + this.version.toString());
			loginClass.getConstructors()[0].newInstance(Util.networkList(channel.remoteAddress(),
					this.version.getServerConnection(), this.version.getNetworkManager()),
					this.profileField.get(packet).getName());
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
		return true;
	}

}
