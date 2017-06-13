package io.baltoro.features;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.baltoro.client.LocalFiles;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(LocalFiles.class)
public @interface LocalFile
{
	String webPath() default "/";
	String localPath() default "";
	boolean isDirecotry() default true;
}

