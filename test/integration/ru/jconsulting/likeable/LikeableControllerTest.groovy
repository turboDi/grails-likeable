package ru.jconsulting.likeable

class LikeableControllerTest extends GroovyTestCase {

    def likeableDomainRegistry
    def controller = new LikeableController()

    void testLike() {
        controller.likeableDomainRegistry = likeableDomainRegistry
        TestDomain d = new TestDomain().save()
        controller.request.user = new TestLiker().save()
        controller.params.type = "testDomain"
        controller.params.id = d.id
        controller.like()
        assertEquals "1", controller.response.contentAsString
    }

    void testNotFound() {
        controller.likeableDomainRegistry = likeableDomainRegistry
        controller.request.user = new TestLiker().save()
        controller.params.type = "testDomain"
        controller.params.id = 1
        controller.like()
        assertEquals 404, controller.response.status
    }
}
