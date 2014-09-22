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
        grailsApplication.config.grails.likeable.liker.className = TestLiker.name
        assertEquals "$l1 liked testDomain : $d.id", d.userLike(l1).toString()
    }

    private void saveTested() {
        l1 = new TestLiker().save()
        l2 = new TestLiker().save()
        d = new TestDomain().save()

        d.like(l1)
        d.like(l2)
    }
}
