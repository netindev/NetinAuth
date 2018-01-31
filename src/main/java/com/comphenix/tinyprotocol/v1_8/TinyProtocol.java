package com.comphenix.tinyprotocol.v1_8;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.tinyprotocol.NMSReflection;
import com.comphenix.tinyprotocol.NMSReflection.FieldAccessor;
import com.comphenix.tinyprotocol.NMSReflection.MethodInvoker;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;

import gq.netin.auth.util.Util;
import gq.netin.auth.version.Version;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

/**
 * Represents a very tiny alternative to ProtocolLib.
 * <p>
 * It now supports intercepting packets during login and status ping (such as
 * OUT_SERVER_PING)!
 *
 * @author Kristian
 */
public abstract class TinyProtocol {

	private static final AtomicInteger ID = new AtomicInteger(0);

	private static MethodInvoker getPlayerHandle = NMSReflection.getMethod("{obc}.entity.CraftPlayer", "getHandle");
	private static FieldAccessor<Object> getConnection = NMSReflection.getField("{nms}.EntityPlayer",
			"playerConnection", Object.class);
	private static FieldAccessor<Object> getManager = NMSReflection.getField("{nms}.PlayerConnection", "networkManager",
			Object.class);
	private static FieldAccessor<Channel> getChannel = NMSReflection.getField("{nms}.NetworkManager", Channel.class, 0);

	private static Class<?> minecraftServerClass = NMSReflection.getUntypedClass("{nms}.MinecraftServer");
	private static Class<?> serverConnectionClass = NMSReflection.getUntypedClass("{nms}.ServerConnection");
	private static FieldAccessor<?> getMinecraftServer = NMSReflection.getField("{obc}.CraftServer",
			TinyProtocol.minecraftServerClass, 0);
	private static FieldAccessor<?> getServerConnection = NMSReflection.getField(TinyProtocol.minecraftServerClass,
			TinyProtocol.serverConnectionClass, 0);
	private static MethodInvoker getNetworkMarkers = NMSReflection.getTypedMethod(TinyProtocol.serverConnectionClass,
			null, List.class, TinyProtocol.serverConnectionClass);

	private static final Class<?> PACKET_LOGIN_IN_START = NMSReflection.getMinecraftClass("PacketLoginInStart");
	private static FieldAccessor<GameProfile> getGameProfile = NMSReflection
			.getField(TinyProtocol.PACKET_LOGIN_IN_START, GameProfile.class, 0);

	private final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
	private Listener listener;

	private final Set<Channel> uninjectedChannels = Collections
			.newSetFromMap(new MapMaker().weakKeys().<Channel, Boolean>makeMap());

	private List<?> networkManagers;

	private final List<Channel> serverChannels = Lists.newArrayList();
	private ChannelInboundHandlerAdapter serverChannelHandler;
	private ChannelInitializer<Channel> beginInitProtocol;
	private ChannelInitializer<Channel> endInitProtocol;

	private final String handlerName;

	protected volatile boolean closed;
	protected Plugin plugin;

	public TinyProtocol() {
		this.plugin = gq.netin.auth.Bukkit.getPlugin();

		this.handlerName = this.getHandlerName();

		this.registerBukkitEvents();

		try {
			this.registerChannelHandler();
			this.registerPlayers(this.plugin);
		} catch (IllegalArgumentException ex) {
			new BukkitRunnable() {
				@Override
				public void run() {
					TinyProtocol.this.registerChannelHandler();
					TinyProtocol.this.registerPlayers(TinyProtocol.this.plugin);
				}
			}.runTask(this.plugin);
		}
	}

	private void createServerChannelHandler() {
		this.endInitProtocol = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				try {
					synchronized (TinyProtocol.this.networkManagers) {
						if (TinyProtocol.this.closed) {
							if (Version.getPackageVersion() == Version.v1_12_R1) {
								channel.eventLoop().submit(() -> TinyProtocol.this.injectChannelInternal(channel));
							} else {
								return;
							}
						}
						TinyProtocol.this.injectChannelInternal(channel);
					}
				} catch (final Exception e) {
					TinyProtocol.this.plugin.getLogger().log(Level.SEVERE, "Error: " + channel, e);
				}
			}
		};

		this.beginInitProtocol = new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				channel.pipeline().addLast(TinyProtocol.this.endInitProtocol);
			}
		};

		this.serverChannelHandler = new ChannelInboundHandlerAdapter() {
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				final Channel channel = (Channel) msg;

				channel.pipeline().addFirst(TinyProtocol.this.beginInitProtocol);
				ctx.fireChannelRead(msg);
			}
		};
	}

	private void registerBukkitEvents() {
		this.listener = new Listener() {
			@EventHandler(priority = EventPriority.LOWEST)
			public final void onPlayerLogin(PlayerLoginEvent e) {
				if (TinyProtocol.this.closed) {
					return;
				}
				final Channel channel = TinyProtocol.this.getChannel(e.getPlayer());

				if (!TinyProtocol.this.uninjectedChannels.contains(channel)) {
					TinyProtocol.this.injectPlayer(e.getPlayer());
				}
			}

			@EventHandler
			public final void onPluginDisable(PluginDisableEvent e) {
				if (e.getPlugin().equals(TinyProtocol.this.plugin)) {
					TinyProtocol.this.close();
				}
			}
		};

		this.plugin.getServer().getPluginManager().registerEvents(this.listener, this.plugin);
	}

	private void registerChannelHandler() {
		final Object mcServer = TinyProtocol.getMinecraftServer.get(Bukkit.getServer());
		final Object serverConnection = TinyProtocol.getServerConnection.get(mcServer);
		boolean looking = true;

		this.networkManagers = (List<?>) TinyProtocol.getNetworkMarkers.invoke(null, serverConnection);
		this.createServerChannelHandler();

		for (int i = 0; looking; i++) {
			final List<?> list = NMSReflection.getField(serverConnection.getClass(), List.class, i)
					.get(serverConnection);

			for (final Object item : list) {
				if (!ChannelFuture.class.isInstance(item)) {
					break;
				}

				final Channel serverChannel = ((ChannelFuture) item).channel();

				this.serverChannels.add(serverChannel);
				serverChannel.pipeline().addFirst(this.serverChannelHandler);
				looking = false;
			}
		}
	}

	private void unregisterChannelHandler() {
		if (this.serverChannelHandler == null) {
			return;
		}

		for (final Channel serverChannel : this.serverChannels) {
			final ChannelPipeline pipeline = serverChannel.pipeline();

			serverChannel.eventLoop().execute(() -> {
				try {
					pipeline.remove(TinyProtocol.this.serverChannelHandler);
				} catch (final NoSuchElementException e) {
				}
			});
		}
	}

	private void registerPlayers(Plugin plugin) {
		for (Player player : Util.getOnlinePlayers()) {
			this.injectPlayer(player);
		}
	}

	public Object onPacketOutAsync(Player reciever, Channel channel, Object packet) {
		return packet;
	}

	public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
		return packet;
	}

	/* sendPacket, receivePacket, hasInjected unused */

	protected String getHandlerName() {
		return "tiny-" + this.plugin.getName() + "-" + TinyProtocol.ID.incrementAndGet();
	}

	public void injectPlayer(Player player) {
		this.injectChannelInternal(this.getChannel(player)).player = player;
	}

	private PacketInterceptor injectChannelInternal(Channel channel) {
		try {
			PacketInterceptor interceptor = (PacketInterceptor) channel.pipeline().get(this.handlerName);

			if (interceptor == null) {
				interceptor = new PacketInterceptor();
				channel.pipeline().addBefore("packet_handler", this.handlerName, interceptor);
				this.uninjectedChannels.remove(channel);
			}
			return interceptor;
		} catch (final IllegalArgumentException e) {
			return (PacketInterceptor) channel.pipeline().get(this.handlerName);
		}
	}

	public Channel getChannel(Player player) {
		Channel channel = this.channelLookup.get(player.getName());

		if (channel == null) {
			final Object connection = TinyProtocol.getConnection.get(TinyProtocol.getPlayerHandle.invoke(player));
			final Object manager = TinyProtocol.getManager.get(connection);

			this.channelLookup.put(player.getName(), channel = TinyProtocol.getChannel.get(manager));
		}
		return channel;
	}

	public void uninjectPlayer(Player player) {
		this.uninjectChannel(this.getChannel(player));
	}

	public void uninjectChannel(final Channel channel) {
		if (!this.closed) {
			this.uninjectedChannels.add(channel);
		}

		channel.eventLoop().execute(() -> channel.pipeline().remove(TinyProtocol.this.handlerName));
	}

	public final void close() {
		if (!this.closed) {
			this.closed = true;
			for (final Player player : Util.getOnlinePlayers()) {
				this.uninjectPlayer(player);
			}
			HandlerList.unregisterAll(this.listener);
			this.unregisterChannelHandler();
		}
	}

	private final class PacketInterceptor extends ChannelDuplexHandler {
		public volatile Player player;

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			final Channel channel = ctx.channel();
			this.handleLoginStart(channel, msg);

			try {
				msg = TinyProtocol.this.onPacketInAsync(this.player, channel, msg);
			} catch (final Exception e) {
				TinyProtocol.this.plugin.getLogger().log(Level.SEVERE, "onPacketInAsync() error.", e);
			}

			if (msg != null) {
				super.channelRead(ctx, msg);
			}
		}

		@Override
		public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
			try {
				msg = TinyProtocol.this.onPacketOutAsync(this.player, ctx.channel(), msg);
			} catch (final Exception e) {
				TinyProtocol.this.plugin.getLogger().log(Level.SEVERE, "onPacketOutAsync() error.", e);
			}

			if (msg != null) {
				super.write(ctx, msg, promise);
			}
		}

		private void handleLoginStart(Channel channel, Object packet) {
			if (TinyProtocol.PACKET_LOGIN_IN_START.isInstance(packet)) {
				final GameProfile profile = TinyProtocol.getGameProfile.get(packet);
				TinyProtocol.this.channelLookup.put(profile.getName(), channel);
			}
		}
	}

}
