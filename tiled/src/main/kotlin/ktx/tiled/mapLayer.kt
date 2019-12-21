package ktx.tiled

import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapProperties

/**
 * Extension method to directly access the [MapProperties] of a [MapLayer]. The type is automatically
 * derived from the type of the given default value. If the property is not defined the defaultValue will be returned.
 * @param key property name
 * @param defaultValue default value in case the property is missing
 * @return value of the property or defaultValue if property is missing
 */
inline fun <reified T> MapLayer.property(key: String, defaultValue: T): T = this.properties[key, defaultValue, T::class.java]

/**
 * Extension method to directly access the [MapProperties] of a [MapLayer]. If the property
 * is not defined then this method returns null.
 * @param key property name
 * @return value of the property or null if the property is missing
 */
inline fun <reified T> MapLayer.propertyOrNull(key: String): T? = this.properties[key, T::class.java]

/**
 * Extension method to directly access the [MapProperties] of a [MapLayer] and its [containsKey][MapProperties.containsKey] method
 * @param key property name
 * @return true if the property exists. Otherwise false
 */
fun MapLayer.containsProperty(key: String) = properties.containsKey(key)