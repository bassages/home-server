package nl.homeserver.energie;

import java.util.EnumSet;

public enum StroomTariefIndicator {
    DAL((short)1),
    NORMAAL((short)2),
    ONBEKEND((short)9);

    private final short id;

    StroomTariefIndicator(short id) {
        this.id = id;
    }

    public short getId() {
        return id;
    }

    public static StroomTariefIndicator byId(short id) {
        return EnumSet.allOf(StroomTariefIndicator.class)
                .stream()
                .filter(e -> e.id == id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("StroomTariefIndicator with id %s does not exist", id)));
    }
}
