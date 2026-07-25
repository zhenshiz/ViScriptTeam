package com.viscript_team.network.c2s;

import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import com.viscript_team.ViScriptTeam;
import com.viscript_team.data.faction.FactionSavedData;
import com.viscript_team.network.PartyScreenSync;
import com.viscript_team.util.PartyPlayerService;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.function.Function;

public final class C2SPayload {
    private static final String PREFIX = ViScriptTeam.MOD_ID + ":";
    public static final String CREATE_PARTY = PREFIX + "create_party_c2s";
    public static final String APPLY_TO_PARTY = PREFIX + "apply_to_party_c2s";
    public static final String ACCEPT_INVITATION = PREFIX + "accept_party_invitation_c2s";
    public static final String DECLINE_INVITATION = PREFIX + "decline_party_invitation_c2s";
    public static final String INVITE_PLAYER = PREFIX + "invite_party_player_c2s";
    public static final String ACCEPT_JOIN_REQUEST = PREFIX + "accept_party_join_request_c2s";
    public static final String REJECT_JOIN_REQUEST = PREFIX + "reject_party_join_request_c2s";
    public static final String LEAVE_PARTY = PREFIX + "leave_party_c2s";
    public static final String KICK_MEMBER = PREFIX + "kick_party_member_c2s";
    public static final String TRANSFER_LEADERSHIP = PREFIX + "transfer_party_leadership_c2s";
    public static final String DISBAND_PARTY = PREFIX + "disband_party_c2s";
    public static final String SET_FRIENDLY_FIRE = PREFIX + "set_party_friendly_fire_c2s";
    public static final String SEND_CHAT_MESSAGE = PREFIX + "send_party_chat_message_c2s";

    private C2SPayload() {
    }

    @RPCPacket(CREATE_PARTY)
    public static void createParty(RPCSender sender, String name) {
        handle(sender, player -> PartyPlayerService.createParty(player, name));
    }

    @RPCPacket(APPLY_TO_PARTY)
    public static void applyToParty(RPCSender sender, String partyId) {
        handle(sender, player -> PartyPlayerService.applyToParty(player, partyId));
    }

    @RPCPacket(ACCEPT_INVITATION)
    public static void acceptInvitation(RPCSender sender, String partyId) {
        handle(sender, player -> PartyPlayerService.acceptInvitation(player, partyId));
    }

    @RPCPacket(DECLINE_INVITATION)
    public static void declineInvitation(RPCSender sender, String partyId) {
        handle(sender, player -> PartyPlayerService.declineInvitation(player, partyId));
    }

    @RPCPacket(INVITE_PLAYER)
    public static void invitePlayer(RPCSender sender, String targetId) {
        handleTarget(sender, targetId, PartyPlayerService::invitePlayer);
    }

    @RPCPacket(ACCEPT_JOIN_REQUEST)
    public static void acceptJoinRequest(RPCSender sender, String targetId) {
        handleTarget(sender, targetId, PartyPlayerService::acceptJoinRequest);
    }

    @RPCPacket(REJECT_JOIN_REQUEST)
    public static void rejectJoinRequest(RPCSender sender, String targetId) {
        handleTarget(sender, targetId, PartyPlayerService::rejectJoinRequest);
    }

    @RPCPacket(LEAVE_PARTY)
    public static void leaveParty(RPCSender sender) {
        handle(sender, PartyPlayerService::leaveParty);
    }

    @RPCPacket(KICK_MEMBER)
    public static void kickMember(RPCSender sender, String targetId) {
        handleTarget(sender, targetId, PartyPlayerService::kickMember);
    }

    @RPCPacket(TRANSFER_LEADERSHIP)
    public static void transferLeadership(RPCSender sender, String targetId) {
        handleTarget(sender, targetId, PartyPlayerService::transferLeadership);
    }

    @RPCPacket(DISBAND_PARTY)
    public static void disbandParty(RPCSender sender) {
        handle(sender, PartyPlayerService::disbandParty);
    }

    @RPCPacket(SET_FRIENDLY_FIRE)
    public static void setFriendlyFire(RPCSender sender, boolean enabled) {
        handle(sender, player -> PartyPlayerService.setFriendlyFire(player, enabled));
    }

    @RPCPacket(SEND_CHAT_MESSAGE)
    public static void sendChatMessage(RPCSender sender, String message) {
        ServerPlayer player = sender.asPlayer();
        if (player == null) {
            return;
        }
        String partyId = FactionSavedData.get(player.serverLevel()).getPlayerParty(player.getUUID())
                .map(party -> party.getId())
                .orElse("");
        PartyPlayerService.Result result = PartyPlayerService.sendChatMessage(player, message);
        if (result.success() && !partyId.isEmpty()) {
            PartyScreenSync.refreshParty(player.server, partyId, player, new PartyPlayerService.Result(true, ""));
        } else {
            PartyScreenSync.refresh(player, result);
        }
    }

    private static void handle(RPCSender sender, Function<ServerPlayer, PartyPlayerService.Result> action) {
        ServerPlayer player = sender.asPlayer();
        if (player == null) {
            return;
        }
        PartyScreenSync.refreshAll(player.server, player, action.apply(player));
    }

    private static void handleTarget(RPCSender sender, String targetId, TargetAction action) {
        handle(sender, player -> {
            try {
                return action.apply(player, UUID.fromString(targetId));
            } catch (IllegalArgumentException ignored) {
                return PartyPlayerService.Result.invalidTarget();
            }
        });
    }

    @FunctionalInterface
    private interface TargetAction {
        PartyPlayerService.Result apply(ServerPlayer player, UUID targetId);
    }
}
