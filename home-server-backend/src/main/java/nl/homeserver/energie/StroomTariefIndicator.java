package nl.homeserver.energie;

import java.util.EnumSet;

import static java.lang.String.format;

public enum StroomTariefIndicator {
    DAL((short)1),
    NORMAAL((short)2),
    ONBEKEND((short)9);

    private final short id;

    StroomTariefIndicator(final short id) {
        this.id = id;
    }

    public short getId() {
        return id;
    }

    public static StroomTariefIndicator byId(final short id) {
        return EnumSet.allOf(StroomTariefIndicator.class)
                      .stream()
                      .filter(stroomTariefIndicator -> stroomTariefIndicator.id == id)
                      .findFirst()
                      .orElseThrow(() -> new RuntimeException(format("StroomTariefIndicator with id %s does not exist", id)));
    }
}
