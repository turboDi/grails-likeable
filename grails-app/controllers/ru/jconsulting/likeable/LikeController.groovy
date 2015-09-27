package ru.jconsulting.likeable

import grails.rest.RestfulController

/**
 * RESTful <tt>Likeable</tt> controller with CRUD functionality<br/>
 *
 * Users should restrict usage of <tt>update</tt> method as it makes no sense
 */
class LikeController extends RestfulController<Like> {

    def likeableDomainRegistry

    LikeController() {
        super(Like)
    }

    @Override
    protected Like queryForResource(Serializable id) {
        def likeable = evaluateLikeable(params)
        def liker = evaluateLiker()
        likeable.userLike(liker)
    }

    @Override
    protected Like createResource(Map params) {
        def likeable = evaluateLikeable(params)
        def liker = evaluateLiker()
        likeable.initLike(liker)
    }

    @Override
    protected List<Like> listAllResources(Map params) {
        def likeable = evaluateLikeable(params)
        likeable.getAllLikes(params)
    }

    /**
     * Evaluates <tt>Likeable</tt> instance based on input <tt>params</tt>
     *
     * @param type short type name of domain
     * @param id instance id
     *
     * @return evaluated likeable instance
     * @throws LikeException if there is no such likeable domain in registry
     *         LikeableNotFoundException if likeable instance evaluated to null
     */
    protected def evaluateLikeable(Map params) {
        // this query should run really fast even without hitting database,
        // because likeable instance is already in persistence context
        def likeable = likeableDomainRegistry.getClassByType(params.type).get(params.likeableId)
        if (!likeable) {
            throw new LikeableNotFoundException("There is no such Likeable instance with type='$params.type' and " +
                    "id='$params.id")
        }
        likeable
    }

    def onLikeableNotFound(LikeableNotFoundException e) {
        log.debug e.getMessage()
        notFound()
    }

    /**
     * Evaluates user in accordance to <tt>grails.likeable.liker.evaluator</tt> config of application
     *
     * @return evaluated user
     * @throws LikeException if user is evaluated to <tt>null</tt> or it is not a persisted entity
     */
    protected def evaluateLiker() {
        def evaluator = grailsApplication.config.grails.plugin.likeable.liker.evaluator
        def liker = null
        if (evaluator instanceof Closure) {
            evaluator.delegate = this
            evaluator.resolveStrategy = Closure.DELEGATE_ONLY
            liker = evaluator.call()
        }

        if (!liker) {
            throw new LikeException("No [grails.plugin.likeable.liker.evaluator] setting defined or the evaluator doesn't " +
                    "evaluate to an entity. Please define the evaluator correctly in grails-app/conf/Config.groovy " +
                    "or ensure like is secured via your security rules")
        }
        if (!liker.id) {
            throw new LikeException("The evaluated Like liker is not a persistent instance.")
        }
        return liker
    }
}
