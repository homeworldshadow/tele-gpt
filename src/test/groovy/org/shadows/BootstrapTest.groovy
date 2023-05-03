package org.shadows

import spock.lang.Specification


/**
 * Fill the comment
 *
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