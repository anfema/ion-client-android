package com.anfema.ionclient.pages.models

import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.Test

class MetaTest
{
    @Test
    @Throws(Exception::class)
    fun equalsContract()
    {
        EqualsVerifier.forClass(Meta::class.java).suppress(Warning.NONFINAL_FIELDS).verify();
    }
}