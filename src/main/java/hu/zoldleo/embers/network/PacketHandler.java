package hu.zoldleo.embers.network;

import hu.zoldleo.embers.network.message.MessageBeamCannonFX;
import hu.zoldleo.embers.network.message.MessageCasterOrb;
import hu.zoldleo.embers.network.message.MessageCrystalCellGrowFX;
import hu.zoldleo.embers.network.message.MessageEmberGenOffset;
import hu.zoldleo.embers.network.message.MessageEmberRayFX;
import hu.zoldleo.embers.network.message.MessageItemSound;
import hu.zoldleo.embers.network.message.MessageResearchData;
import hu.zoldleo.embers.network.message.MessageResearchTick;
import hu.zoldleo.embers.network.message.MessageScalesData;
import hu.zoldleo.embers.network.message.MessageDialUpdateRequest;
import hu.zoldleo.embers.network.message.MessageWorldSeed;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// TODO: remove
public class PacketHandler {
	private static final String PROTOCOL_VERSION = "1";

    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(MessageCasterOrb.TYPE, MessageCasterOrb.CODEC, MessageCasterOrb::handle);
        registrar.playToServer(MessageDialUpdateRequest.TYPE, MessageDialUpdateRequest.CODEC, MessageDialUpdateRequest::handle);
        registrar.playToServer(MessageResearchTick.TYPE, MessageResearchTick.CODEC, MessageResearchTick::handle);

        registrar.playToClient(MessageBeamCannonFX.TYPE, MessageBeamCannonFX.CODEC, MessageBeamCannonFX::handle);
        registrar.playToClient(MessageCrystalCellGrowFX.TYPE, MessageCrystalCellGrowFX.CODEC, MessageCrystalCellGrowFX::handle);
        registrar.playToClient(MessageEmberGenOffset.TYPE, MessageEmberGenOffset.CODEC, MessageEmberGenOffset::handle);
        registrar.playToClient(MessageEmberRayFX.TYPE, MessageEmberRayFX.CODEC, MessageEmberRayFX::handle);
        registrar.playToClient(MessageItemSound.TYPE, MessageItemSound.CODEC, MessageItemSound::handle);
        registrar.playToClient(MessageResearchData.TYPE, MessageResearchData.CODEC, MessageResearchData::handle);
        registrar.playToClient(MessageScalesData.TYPE, MessageScalesData.CODEC, MessageScalesData::handle);
        registrar.playToClient(MessageWorldSeed.TYPE, MessageWorldSeed.CODEC, MessageWorldSeed::handle);
    }
}