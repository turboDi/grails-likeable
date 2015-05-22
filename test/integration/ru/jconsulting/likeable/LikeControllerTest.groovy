package ru.jconsulting.likeable

import grails.test.spock.IntegrationSpec
import org.codehaus.groovy.grails.commons.GrailsApplication
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

    @Autowired
    GrailsApplication grailsApplication

    TestDomain domain
    TestLiker liker

    def setup() {
        domain = new TestDomain().save()
        controller.metaClass.restrictedDomain = new TestDomain().save()
        grailsApplication.config.grails.plugin.likeable.liker.className = TestLiker.name
        grailsApplication.config.grails.plugin.likeable.permission.evaluator = { liker, likeable ->
            !restrictedDomain.equals(likeable)
        }
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
        controller.params.id = domain.userLike(liker).id
        when:
        controller.delete()
        then:
        204 == controller.response.status
        !domain.userLiked(liker)
    }

    def "test delete other's like"() {
        setup:
        def other = new TestLiker().save()
        domain.like(other)
        and:
        controller.request.user = liker
        controller.params.id = domain.userLike(other).id
        when:
        controller.delete()
        then:
        thrown(LikeException)
    }

    def "test delete nonexistent like"() {
        given:
        controller.request.user = liker
        controller.params.id = 100500
        when:
        controller.delete()
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

    def "test like restricted"() {
        given:
        controller.request.user = liker
        controller.params.type = "testDomain"
        controller.params.likeableId = controller.restrictedDomain.id
        when:
        controller.save()
        then:
        thrown(LikeException)
    }

}
