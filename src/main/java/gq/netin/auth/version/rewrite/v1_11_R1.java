package gq.netin.auth.version.rewrite;

import java.nio.charset.Charset;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_11_R1.CraftServer;

import com.mojang.authlib.GameProfile;

import gq.netin.auth.Bukkit;
import gq.netin.auth.util.Messages;
import gq.netin.auth.util.Reflection;
import gq.netin.auth.util.Util;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.LoginListener;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.NetworkManager;
import net.minecraft.server.v1_11_R1.PacketLoginOutSuccess;

/**
 *
 * @author netindev
 *
 */
public class v1_11_R1 extends LoginListener {

	private static final MinecraftServer SERVER = ((CraftServer) Bukkit.getPlugin().getServer()).getServer();

	public v1_11_R1(NetworkManager networkManager, String player) {
		super(v1_11_R1.SERVER, networkManager);
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
		this.F_();
	}

	@Override
	public void F_() {
		GameProfile validProfile = (GameProfile) Reflection.getField("i", this, 1);
		EntityPlayer attemptLogin = v1_11_R1.SERVER.getPlayerList().attemptLogin(this, validProfile, this.hostname);
		if (attemptLogin != null) {
			this.networkManager.sendPacket(new PacketLoginOutSuccess(validProfile));
			v1_11_R1.SERVER.getPlayerList().a(this.networkManager,
					v1_11_R1.SERVER.getPlayerList().processLogin(validProfile, attemptLogin));
		}
	}

}
