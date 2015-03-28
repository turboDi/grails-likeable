package ru.jconsulting.likeable

import grails.test.spock.IntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

/**
 *
 *
 * @author Dmitriy Borisov
 * @created 28.03.2015
 */

class LikeControllerTest extends IntegrationSpec {

    @Autowired
    LikeController controller

    TestDomain domain
    TestLiker liker

    def setup() {
        domain = new TestDomain().save()
        liker = new TestLiker().save()
        controller.params.format = 'json'
    }

    def "test like"() {
        given:
        controller.request.user = liker
        controller.params.type = "testDomain"
        controller.params.likeableId = domain.id
        when:
        controller.save()
        then:
        201 == controller.response.status
        domain.id == controller.response.json.likeRef
        domain.userLiked(liker)
    }

    def "test repeat like"() {
        setup:
        domain.like(liker)
        and:
        controller.request.user = liker
        controller.params.type = "testDomain"
        controller.params.likeableId = domain.id
        when:
        controller.save()
        then:
        422 == controller.response.status
    }

    def "test like nonexistent"() {
        given:
        controller.request.user = liker
        controller.params.type = "testDomain"
        controller.params.likeableId = 100500
        when:
        controller.save()
        then:
        404 == controller.response.status
    }

    def "test dislike"() {
        setup:
        domain.like(liker)
        and:
        controller.request.user = liker
        controller.params.type = "testDomain"
        controller.params.likeableId = domain.id
        when:
        controller.delete()
        then:
        204 == controller.response.status
        !domain.userLiked(liker)
    }

    def "test dislike nonexistent"() {
        given:
        controller.request.user = liker
        controller.params.type = "testDomain"
        controller.params.likeableId = 100500
        when:
        controller.save()
        then:
        404 == controller.response.status
    }

    def "test list all likes"() {
        setup:
        domain.like(liker)
        and:
        controller.params.type = "testDomain"
        controller.params.likeableId = domain.id
        when:
        controller.index()
        then:
        200 == controller.response.status
        [domain.id] == controller.response.json*.likeRef
    }

    def "test list all likes of nonexistent"() {
        controller.params.type = "testDomain"
        controller.params.likeableId = 100500
        when:
        controller.index()
        then:
        404 == controller.response.status
    }

}