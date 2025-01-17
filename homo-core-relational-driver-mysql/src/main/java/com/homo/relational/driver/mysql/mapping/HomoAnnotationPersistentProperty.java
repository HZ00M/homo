package com.homo.relational.driver.mysql.mapping;

import com.homo.core.facade.relational.mapping.HomoId;
import com.homo.core.facade.relational.mapping.HomoTransient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.AbstractPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.Lazy;
import org.springframework.data.util.Optionals;
import org.springframework.data.util.StreamUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public abstract class HomoAnnotationPersistentProperty<P extends PersistentProperty<P>> extends AbstractPersistentProperty<P> {
    private final @Nullable String value;
    private final Map<Class<? extends Annotation>, Optional<? extends Annotation>> annotationCache = new ConcurrentHashMap<>();

    private final Lazy<Boolean> usePropertyAccess = Lazy.of(() -> {

        AccessType accessType = findPropertyOrOwnerAnnotation(AccessType.class);

        return accessType != null && AccessType.Type.PROPERTY.equals(accessType.value()) || super.usePropertyAccess();
    });

    private final Lazy<Boolean> isTransient = Lazy.of(() -> super.isTransient() || isAnnotationPresent(HomoTransient.class)
            || isAnnotationPresent(Value.class) || isAnnotationPresent(Autowired.class));

    private final Lazy<Boolean> isWritable = Lazy
            .of(() -> !isTransient() && !isAnnotationPresent(ReadOnlyProperty.class));

    private final Lazy<Boolean> isId = Lazy.of(() -> isAnnotationPresent(HomoId.class));

    public HomoAnnotationPersistentProperty(Property property, PersistentEntity<?,P> owner, SimpleTypeHolder simpleTypeHolder){
        super(property, owner, simpleTypeHolder);
        populateAnnotationCache(property);
        Value value = findAnnotation(Value.class);
        this.value = value == null ? null : value.value();
    }

    private void populateAnnotationCache(Property property) {
        Optionals.toStream(property.getGetter(), property.getSetter()).forEach(it -> {

            for (Annotation annotation : it.getAnnotations()) {

                Class<? extends Annotation> annotationType = annotation.annotationType();

                annotationCache.put(annotationType,
                        Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(it, annotationType)));
            }
        });

        property.getField().ifPresent(it -> {

            for (Annotation annotation : it.getAnnotations()) {

                Class<? extends Annotation> annotationType = annotation.annotationType();

                annotationCache.put(annotationType,
                        Optional.ofNullable(AnnotatedElementUtils.findMergedAnnotation(it, annotationType)));
            }
        });
    }
    @Nullable
    @Override
    public String getSpelExpression() {
        return value;
    }

    @Override
    public boolean isTransient() {
        return isTransient.get();
    }

    @Override public boolean isIdProperty() {
        return isId.get();
    }

    @Override
    public boolean isVersionProperty() {
        return false;
    }

    @Override
    public boolean isAssociation() {
        return false;
    }

    @Override
    public boolean isWritable() {
        return isWritable.get();
    }

    @Nullable
    public <A extends Annotation> A findAnnotation(@NotNull Class<A> annotationType) {

        Assert.notNull(annotationType, "Annotation type must not be null!");

        return doFindAnnotation(annotationType).orElse(null);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <A extends Annotation> Optional<A> doFindAnnotation(Class<A> annotationType) {

        Optional<? extends Annotation> annotation = annotationCache.get(annotationType);

        if (annotation != null) {
            return (Optional<A>) annotation;
        }

        return (Optional<A>) annotationCache.computeIfAbsent(annotationType, type -> getAccessors() //
                .map(it -> AnnotatedElementUtils.findMergedAnnotation(it, type)) //
                .flatMap(StreamUtils::fromNullable) //
                .findFirst());
    }

    @Nullable
    @Override
    public <A extends Annotation> A findPropertyOrOwnerAnnotation(@NotNull Class<A> annotationType) {

        A annotation = findAnnotation(annotationType);

        return annotation != null ? annotation : getOwner().findAnnotation(annotationType);
    }


    public boolean isAnnotationPresent(@NotNull Class<? extends Annotation> annotationType) {
        return doFindAnnotation(annotationType).isPresent();
    }

    @Override
    public boolean usePropertyAccess() {
        return usePropertyAccess.get();
    }

    @Nullable
    @Override
    public Class<?> getAssociationTargetType() {
        return null;
    }

    private Stream<? extends AnnotatedElement> getAccessors() {

        return Optionals.toStream(Optional.ofNullable(getGetter()), Optional.ofNullable(getSetter()),
                Optional.ofNullable(getField()));
    }
}
