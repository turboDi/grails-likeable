package ru.jconsulting.likeable

import grails.test.spock.IntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

class LikeableControllerTest extends IntegrationSpec {

    @Autowired
    LikeableController controller

    TestDomain domain
    TestLiker liker

    def setup() {
        domain = new TestDomain().save()
        liker = new TestLiker().save()
    }

    def "test like"() {
        given:
        controller.request.user = liker
        controller.params.type = "testDomain"
        controller.params.id = domain.id
        when:
        controller.like()
        then:
        '1' == controller.response.contentAsString
    }

    def "test like nonexistent"() {
        given:
        controller.request.user = liker
        controller.params.type = "testDomain"
        controller.params.id = 100500
        when:
        controller.like()
        then:
        404 == controller.response.status
    }

    def "test list all likes"() {
        setup:
        domain.like(liker)
        and:
        controller.params.type = "testDomain"
        controller.params.id = domain.id
        when:
        controller.listLikes()
        then:
        [domain.id] == controller.response.json*.likeRef
    }

    def "test list all likes of nonexistent"() {
        setup:
        domain.like(liker)
        and:
        controller.params.type = "testDomain"
        controller.params.id = 100500
        when:
        controller.listLikes()
        then:
        404 == controller.response.status
    }
}
