package io.baltoro.features;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Endpoint
{
	public String appName();
	public String path();
	public Class<?> collectionReturnType() default StringBuffer.class;
}
