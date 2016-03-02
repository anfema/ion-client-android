package com.anfema.ionclient.utils;

/**
 * 3-states-toggle
 * Is used to turn features in App on and off. If turned to DEFAULT, the setting defined in build.gradle file is used.
 * Features should be set to DEFAULT when building a release.
 */
public enum FeatureState
{
	DEFAULT, ON, OFF
}
