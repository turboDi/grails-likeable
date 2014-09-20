package ru.jconsulting.likeable

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class DomainTypeRegistryTest {

    void setUp() {
        defineBeans {
            tested(DomainTypeRegistry) {
                domainClassList = [TestDomain]
            }
        }
    }

    void testGetClassByType(){
        def registry = grailsApplication.mainContext.getBean('tested')
        assertEquals TestDomain, registry.getClassByType("testDomain")
        shouldFail(LikeException) {
            registry.getClassByType("none")
        }
    }
}
