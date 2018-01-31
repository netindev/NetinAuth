package gq.netin.auth.version.rewrite;

import java.nio.charset.Charset;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import gq.netin.auth.util.Messages;
import gq.netin.auth.util.Reflection;
import gq.netin.auth.util.Util;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.LoginListener;
import net.minecraft.server.v1_8_R1.MinecraftServer;
import net.minecraft.server.v1_8_R1.NetworkManager;
import net.minecraft.server.v1_8_R1.PacketLoginOutSuccess;

/**
 *
 * @author netindev
 *
 */
public class v1_8_R1 extends LoginListener {

	private static final MinecraftServer SERVER = MinecraftServer.getServer();

	public v1_8_R1(NetworkManager networkManager, String player) {
		super(v1_8_R1.SERVER, networkManager);
		if (this.networkManager == null) {
			Util.info(Messages.REFUSED_PLAYER_CONNECTION.replace("<player>", player));
			return;
		}
		Reflection.setField("k", this, this.networkManager, 0);
		Reflection.setField("i",
				new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + player).getBytes(Charset.forName("UTF-8"))),
						player),
				this, 1);
	}

	@Override
	public void b() {
		this.c();
	}

	@Override
	public void c() {
		GameProfile validProfile = (GameProfile) Reflection.getField("i", this, 1);
		EntityPlayer attemptLogin = v1_8_R1.SERVER.getPlayerList().attemptLogin(this, validProfile, this.hostname);
		if (attemptLogin != null) {
			this.networkManager.handle(new PacketLoginOutSuccess(validProfile));
			v1_8_R1.SERVER.getPlayerList().a(this.networkManager,
					v1_8_R1.SERVER.getPlayerList().processLogin(validProfile, attemptLogin));
		}
	}

}
