package world.gregs.voidps.network.codec.game.decode

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.codec.Decoder

class ObjectOption1Decoder : Decoder(7) {

    override fun decode(context: ChannelHandlerContext, packet: Reader) {
        handler?.objectOption(
            context = context,
            run = packet.readBoolean(Modifier.SUBTRACT),
            x = packet.readShort(Modifier.ADD, Endian.LITTLE),
            y = packet.readShort(order = Endian.LITTLE),
            objectId = packet.readShort(),
            option = 1
        )
    }

}