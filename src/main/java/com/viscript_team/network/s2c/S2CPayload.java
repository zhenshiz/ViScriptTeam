package com.viscript_team.network.s2c;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_team.ViScriptTeam;
import com.viscript_team.client.ClientFactionNameTagCache;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.CompoundTag;

@UtilityClass
public class S2CPayload {
    public static final String MOD_ID = ViScriptTeam.MOD_ID + ":";
    public static final String SYNC_FACTION_NAME_TAGS = MOD_ID + "sync_faction_name_tags";

    @RPCPacket(SYNC_FACTION_NAME_TAGS)
    public static void syncFactionNameTags(RPCSender sender, CompoundTag tag) {
        if (sender.isServer()) {
            ClientFactionNameTagCache.apply(tag);
        }
    }
}
