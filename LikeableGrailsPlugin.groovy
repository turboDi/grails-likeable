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

import grails.util.GrailsNameUtils
import ru.jconsulting.likeable.DomainTypeRegistry
import ru.jconsulting.likeable.Like
import ru.jconsulting.likeable.LikeException
import ru.jconsulting.likeable.Likeable

class LikeableGrailsPlugin {
    def version = "0.3.1"
    def grailsVersion = "2.3 > *"
    def pluginExcludes = [
        "grails-app/domain/ru/jconsulting/likeable/TestDomain.groovy",
        "grails-app/domain/ru/jconsulting/likeable/TestLiker.groovy",
        "grails-app/i18n/messages.properties",
        "grails-app/conf/IntegrationTestsConfig.groovy"
    ]

    def title = "Likeable Plugin"
    def author = "Dmitry Borisov"
    def authorEmail = "turbo_di@outlook.com"
    def description = "Adds like functionality to domain classes. A light version of Rateable plugin"
    def documentation = "http://grails.org/plugin/likeable"
    def license = "APACHE"
    def issueManagement = [ system: "GitHub", url: "https://github.com/turboDi/grails-likeable/issues" ]
    def scm = [ url: "https://github.com/turboDi/grails-likeable" ]

    def doWithSpring = {
        def config = application.config.grails.plugin.likeable

        if (!config.liker.evaluator) {
            config.liker.evaluator = { request.user }
        }

        // find all Likeable domains and add their classes to registry
        def domains = application.domainClasses.findAll {Likeable.isAssignableFrom(it.clazz)}.collect {it.clazz}
        likeableDomainRegistry(DomainTypeRegistry) {
            domainClassList = domains
        }
    }

    def doWithDynamicMethods = { ctx ->
        for (domainClass in application.domainClasses) {
            if (!Likeable.isAssignableFrom(domainClass.clazz)) {
                continue
            }

            domainClass.clazz.metaClass {

                getTotalLikes = { ->
                    def instance = delegate
                    if (instance.id == null) {
                        return 0
                    }

                    Like.createCriteria().get {
                        projections {
                            rowCount()
                        }
                        eq "likeRef", instance.id
                        eq "type", GrailsNameUtils.getPropertyName(instance.class)
                        cache true
                    }
                }

                getAllLikes = { params = [:] ->
                    def instance = delegate
                    if (instance.id == null) {
                        return []
                    }

                    params.cache = true

                    Like.findAllByLikeRefAndType(instance.id, GrailsNameUtils.getPropertyName(instance.class), params)
                }

                like = { liker, params = [flush: true] ->
                    def instance = delegate
                    if (!instance.id) {
                        throw new LikeException("You must save the entity [${delegate}] before calling like")
                    }
                    // try to find an existing like
                    def l = Like.createCriteria().get {
                        eq 'likerId', liker.id
                        eq "likeRef", instance.id
                        eq "type", GrailsNameUtils.getPropertyName(instance.class)
                        cache true
                    }
                    // if there is no existing value, create a new one
                    if (!l) {
                        l = instance.initLike(liker)
                        if (!l.validate()) {
                            throw new LikeException("You must save the entity [${liker}] before calling like")
                        }
                        l.save(params as Map)
                    }
                    // for an existing like, delete it
                    else {
                        l.delete(params as Map)
                    }
                    return instance
                }

                initLike = { liker ->
                    def instance = delegate
                    if (!instance.id) {
                        throw new LikeException("You must save the entity [${delegate}] before calling like")
                    }
                    new Like(likerId: liker.id, likeRef: instance.id, type: GrailsNameUtils.getPropertyName(instance.class))
                }

                userLike = { user ->
                    if (!user) return
                    def instance = delegate
                    Like.createCriteria().get {
                        eq 'likerId', user.id
                        eq "likeRef", instance.id
                        eq "type", GrailsNameUtils.getPropertyName(instance.class)
                        cache true
                    }
                }

                userLiked = { user ->
                    delegate.userLike(user) != null
                }
            }
        }
    }
}
