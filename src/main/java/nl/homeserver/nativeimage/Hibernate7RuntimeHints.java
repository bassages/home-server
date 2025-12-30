package nl.homeserver.nativeimage;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class Hibernate7RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // 1. The main "Annotation Model" package (where the wrappers and descriptors live)
        hints.reflection().registerPackage("org.hibernate.boot.models.annotations.internal", 
            builder -> builder.withMembers(
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.PUBLIC_FIELDS,
                MemberCategory.INVOKE_PUBLIC_METHODS
            )
        );

        // 2. The Hibernate Annotations themselves (to allow reading their methods)
        hints.reflection().registerPackage("org.hibernate.annotations", 
            builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS)
        );

        // 3. The SPI and Models engine (handles the "Discovery" of your entity types)
        hints.reflection().registerPackage("org.hibernate.models.spi", 
            builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS)
        );
        
        // 4. Specifically for the Dialect Overrides helper that has been failing
        hints.reflection().registerType(
            org.hibernate.boot.model.internal.DialectOverridesAnnotationHelper.class,
            builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.PUBLIC_FIELDS)
        );
    }
}
