package net.mehvahdjukaar.moonlight.core.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;


public class ClientBoundOpenScreenPacket implements Message {
    public final BlockPos pos;

    public ClientBoundOpenScreenPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    public ClientBoundOpenScreenPacket(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        handleOpenScreenPacket(this);
    }

    @Environment(EnvType.CLIENT)
    public static void handleOpenScreenPacket(ClientBoundOpenScreenPacket message) {
        var level = Minecraft.getInstance().level;
        var p = Minecraft.getInstance().player;
        if (level != null && p != null) {
            BlockPos pos = message.pos;
            if (level.getBlockEntity(pos) instanceof IScreenProvider tile) {
                tile.openScreen(level, pos, p);
            }
        }
    }


}