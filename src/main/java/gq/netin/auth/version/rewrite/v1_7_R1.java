package gq.netin.auth.version.rewrite;

import java.nio.charset.Charset;
import java.util.UUID;

import gq.netin.auth.util.Messages;
import gq.netin.auth.util.Reflection;
import gq.netin.auth.util.Util;
import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.LoginListener;
import net.minecraft.server.v1_7_R1.MinecraftServer;
import net.minecraft.server.v1_7_R1.NetworkManager;
import net.minecraft.server.v1_7_R1.PacketLoginOutSuccess;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;

/**
 *
 * @author netindev
 *
 */
public class v1_7_R1 extends LoginListener {

	private static final MinecraftServer SERVER = MinecraftServer.getServer();

	public v1_7_R1(NetworkManager networkManager, String player) {
		super(v1_7_R1.SERVER, networkManager);
		if (this.networkManager == null) {
			Util.info(Messages.REFUSED_PLAYER_CONNECTION.replace("<player>", player));
			return;
		}
		Reflection.setField("m", this, this.networkManager, 0);
		Reflection.setField("i",
				Reflection.newInstance(GameProfile.class,
						UUID.nameUUIDFromBytes(("OfflinePlayer:" + player).getBytes(Charset.forName("UTF-8")))
								.toString().replace("-", ""),
						player),
				this, 1);
	}

	@Override
	public void a() {
		this.c();
	}

	@Override
	public void c() {
		GameProfile validProfile = (GameProfile) Reflection.getField("i", this, 1);
		EntityPlayer attemptLogin = v1_7_R1.SERVER.getPlayerList().attemptLogin(this, validProfile, this.hostname);
		if (attemptLogin != null) {
			this.networkManager.handle(new PacketLoginOutSuccess(validProfile), new GenericFutureListener[0]);
			v1_7_R1.SERVER.getPlayerList().a(this.networkManager,
					v1_7_R1.SERVER.getPlayerList().processLogin(validProfile, attemptLogin));
		}
	}

}
