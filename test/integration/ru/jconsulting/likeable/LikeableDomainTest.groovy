package ru.jconsulting.likeable

class LikeableDomainTest extends GroovyTestCase {

    def grailsApplication

    TestLiker l1, l2
    TestDomain d

    void testTotalLikes() {
        saveTested()

        assertEquals 2, d.getTotalLikes()
        assertNotNull d.userLike(l1)
        assertNotNull d.userLike(l2)
        assertTrue d.userLiked(l1)
        assertTrue d.userLiked(l2)
    }

    void testDislike() {
        saveTested()
        d.like(l1)

        assertEquals 1, d.getTotalLikes()
        assertNull d.userLike(l1)
        assertNotNull d.userLike(l2)
        assertFalse d.userLiked(l1)
        assertTrue d.userLiked(l2)
    }

    void testToString() {
        saveTested()
        grailsApplication.config.grails.plugin.likeable.liker.className = TestLiker.name
        assertEquals "$l1 liked testDomain : $d.id", d.userLike(l1).toString()
    }

    void testGetTarget() {
        saveTested()

        assertEquals d, d.userLike(l1).getTarget()
        assertEquals d, d.userLike(l2).getTarget()
    }

    void testGetAllLikes() {
        saveTested()

        assertEquals 2, d.getAllLikes().size()
        assertEquals([d], d.getAllLikes(max: 1)*.target)
    }

    void testPerLikerUniqueness() {
        new Like(likerId: 1, likeRef: 1, type: 'testDomain').save()

        def like = new Like(likerId: 1, likeRef: 1, type: 'testDomain')

        assertFalse like.validate()
        assertNotNull like.errors['likerId']
    }

    void testInitLike() {
        saveTested()

        def like = d.initLike(l1)

        assertEquals d.id, like.likeRef
        assertEquals l1.id, like.likerId
        assertEquals 'testDomain', like.type
        assertNull like.id
    }

    private void saveTested() {
        l1 = new TestLiker().save()
        l2 = new TestLiker().save()
        d = new TestDomain().save()

        d.like(l1)
        d.like(l2)
    }
}
