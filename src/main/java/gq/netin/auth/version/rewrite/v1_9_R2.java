package gq.netin.auth.version.rewrite;

import java.nio.charset.Charset;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_9_R2.CraftServer;

import com.mojang.authlib.GameProfile;

import gq.netin.auth.Bukkit;
import gq.netin.auth.util.Messages;
import gq.netin.auth.util.Reflection;
import gq.netin.auth.util.Util;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.LoginListener;
import net.minecraft.server.v1_9_R2.MinecraftServer;
import net.minecraft.server.v1_9_R2.NetworkManager;
import net.minecraft.server.v1_9_R2.PacketLoginOutSuccess;

/**
 *
 * @author netindev
 *
 */
public class v1_9_R2 extends LoginListener {

	private static final MinecraftServer SERVER = ((CraftServer) Bukkit.getPlugin().getServer()).getServer();

	public v1_9_R2(NetworkManager networkManager, String player) {
		super(v1_9_R2.SERVER, networkManager);
		if (this.networkManager == null) {
			Util.info(Messages.REFUSED_PLAYER_CONNECTION.replace("<player>", player));
			return;
		}
		Reflection.setField("m", this, this.networkManager, 0);
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
		EntityPlayer attemptLogin = v1_9_R2.SERVER.getPlayerList().attemptLogin(this, validProfile, this.hostname);
		if (attemptLogin != null) {
			this.networkManager.sendPacket(new PacketLoginOutSuccess(validProfile));
			v1_9_R2.SERVER.getPlayerList().a(this.networkManager,
					v1_9_R2.SERVER.getPlayerList().processLogin(validProfile, attemptLogin));
		}
	}

}
