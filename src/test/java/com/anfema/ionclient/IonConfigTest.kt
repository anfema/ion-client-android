package com.anfema.ionclient

import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.Test

class IonConfigTest
{
    @Test
    @Throws(Exception::class)
    fun equalsContract()
    {
        EqualsVerifier.forClass(IonConfig::class.java).withOnlyTheseFields("baseUrl", "collectionIdentifier", "locale", "variation").verify();
    }
}