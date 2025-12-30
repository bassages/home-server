package nl.homeserver.nativeimage;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.util.ClassUtils;

public class Hibernate7RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register the entire internal annotation model package
        // This solves the "does not have an override form" by catching all Descriptors and Wrappers
        String annotationInternalPkg = "org.hibernate.boot.models.annotations.internal";
        hints.reflection().registerPackage(annotationInternalPkg, 
            builder -> builder.withMembers(
                MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                MemberCategory.PUBLIC_FIELDS,
                MemberCategory.INVOKE_PUBLIC_METHODS
            )
        );

        // Register the Hibernate annotations themselves
        String hibernateAnnPkg = "org.hibernate.annotations";
        hints.reflection().registerPackage(hibernateAnnPkg, 
            builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS)
        );
    }
}
