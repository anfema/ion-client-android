package com.anfema.ionclient.pages.models.contents

import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.Test

class ConnectionTest
{
    @Test
    @Throws(Exception::class)
    fun equalsContract()
    {
        EqualsVerifier.forClass(Connection::class.java).verify();
    }
}