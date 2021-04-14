package cn.desert.newpos.payui.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Asignación de recursos de la barra de navegación, como: título, icono izquierdo, icono derecho, máscara
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NavigationConfig {

	int leftIconId() default -1;

	int rightIconId() default -1;

	/**
	 * ID de recurso
	 * @return
	 */
	int titleId() default -1;

	/**
	 * titulo
	 */
	String titleValue() default "";

	String mask() default "";
	
}
