package org.free


import spock.lang.Specification

/**
 * @author bayura-ea
 */
class BootstrapTest extends Specification {

    def "Run the bot"() {
        when:
        Bootstrap.main(null)
        then:
        noExceptionThrown()
    }

}