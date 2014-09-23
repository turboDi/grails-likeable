/* Copyright 2014 Dmitry Borisov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.jconsulting.likeable

class LikeableController {

    def likeableDomainRegistry

    /**
     * Adds a <tt>Like</tt> to the instance (or removes it if present)
     *
     * @param type short type name of domain
     * @param id instance id
     * @return total likes of the instance or 404 if it doesn't exist
     */
    def like() {
        def liker = evaluateLiker()

        // this query should run really fast even without hitting database,
        // because likeable instance is already in persistence context
        def d = likeableDomainRegistry.getClassByType(params.type).get(params.id)

        if (!d) {
            render(status: 404, message: "${params.type} with id ${params.id} not found")
            return
        }

        d.like(liker)
        render "${d.getTotalLikes()}"
    }

    /**
     * Evaluates user in accordance to <tt>grails.likeable.liker.evaluator</tt> config of application
     *
     * @return evaluated user
     * @throws LikeException if user is evaluated to <tt>null</tt> or it is not a persisted entity
     */
    def evaluateLiker() {
        def evaluator = grailsApplication.config.grails.likeable.liker.evaluator
        def liker
        if (evaluator instanceof Closure) {
            evaluator.delegate = this
            evaluator.resolveStrategy = Closure.DELEGATE_ONLY
            liker = evaluator.call()
        }

        if (!liker) {
            throw new LikeException("No [grails.likeable.liker.evaluator] setting defined or the evaluator doesn't " +
                    "evaluate to an entity. Please define the evaluator correctly in grails-app/conf/Config.groovy " +
                    "or ensure like is secured via your security rules")
        }
        if (!liker.id) {
            throw new LikeException("The evaluated Like liker is not a persistent instance.")
        }
        return liker
    }
}
