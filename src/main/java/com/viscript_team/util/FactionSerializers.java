package com.viscript_team.util;

import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.viscript_team.data.faction.EntityFactionEntry;
import com.viscript_team.data.faction.Faction;
import com.viscript_team.data.faction.PlayerFactionStandings;
import com.viscript_team.data.faction.StandingEntry;
import com.viscript_team.data.party.Party;
import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class FactionSerializers {

    public static synchronized void register() {
        // 序列化自定义集合元素时，需要先注册元素类型的 LDLib2 直接访问器。
        register(Faction.class, Faction::new);
        register(EntityFactionEntry.class, EntityFactionEntry::new);
        register(PlayerFactionStandings.class, PlayerFactionStandings::new);
        register(StandingEntry.class, StandingEntry::new);
        register(Party.class, Party::new);
    }

    private static <T> void register(Class<T> type, Supplier<T> factory) {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(type)
                .codec(PersistedParser.createCodec(factory))
                .streamCodec(PersistedParser.createStreamCodec(factory))
                .codecMark()
                .build(), 900);
    }
}
