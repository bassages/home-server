package nl.homeserver.nativeimage;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import java.util.List;

public class Hibernate7RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // 1. The Core Helper - Needs full access to its internal static maps
        hints.reflection().registerType(
            TypeReference.of("org.hibernate.boot.model.internal.DialectOverridesAnnotationHelper"),
            builder -> builder.withMembers(
                MemberCategory.INVOKE_PUBLIC_METHODS,
                MemberCategory.INVOKE_DECLARED_METHODS,
                MemberCategory.ACCESS_DECLARED_FIELDS,
                MemberCategory.ACCESS_DECLARED_METHODS
            )
        );

        // 2. The Annotation Mirror/Proxy
        // Hibernate 7 MUST be able to proxy these to convert them to "Usage" models
        List<String> annotations = List.of(
            "org.hibernate.annotations.SQLInsert",
            "org.hibernate.annotations.SQLUpdate",
            "org.hibernate.annotations.SQLDelete",
            "org.hibernate.annotations.SQLRestriction"
        );
        for (String ann : annotations) {
            hints.proxies().registerJdkProxy(TypeReference.of(ann));
            hints.reflection().registerType(TypeReference.of(ann), 
                builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
        }

        // 3. The Models and DESCRIPTORS
        // The error "no override form" happens because Hibernate can't find the static DESCRIPTOR field
        List<String> internalModels = List.of(
            "SQLInsertAnnotation", "OverrideInsertAnnotation",
            "SQLUpdateAnnotation", "OverrideUpdateAnnotation",
            "SQLDeleteAnnotation", "OverrideDeleteAnnotation",
            "SQLRestrictionAnnotation", "OverrideRestrictionAnnotation",
            "CacheAnnotation"
        );

        for (String model : internalModels) {
            String fqn = "org.hibernate.boot.models.annotations.internal." + model;
            
            // The Wrapper
            hints.reflection().registerType(TypeReference.of(fqn), 
                builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, 
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS
                ));

            // The Descriptor (CRITICAL: Needs Field Access for the static DESCRIPTOR field)
            hints.reflection().registerType(TypeReference.of(fqn + "$Descriptor"), 
                builder -> builder.withMembers(
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, 
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.ACCESS_DECLARED_FIELDS,
                    MemberCategory.INVOKE_DECLARED_METHODS
                ));
        }
        
        // 4. Register the SPI context
        hints.reflection().registerType(TypeReference.of("org.hibernate.models.spi.ModelsContext"),
            builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
    }
}