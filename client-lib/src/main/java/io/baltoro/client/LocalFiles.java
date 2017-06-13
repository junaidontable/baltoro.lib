package io.baltoro.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.baltoro.features.LocalFile;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalFiles
{
	LocalFile[] value() default {};
}