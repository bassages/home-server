package nl.homeserver.energy.meterreading;

import javax.annotation.Nullable;
import java.time.LocalDate;

record MeterstandOpDag(
        LocalDate dag,
        @Nullable
        Meterstand meterstand
) { }
