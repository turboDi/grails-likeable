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
import ru.jconsulting.likeable.DomainTypeRegistry

class LikeableGrailsPlugin {
    // the plugin version
    def version = "0.1.0-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/ru/jconsulting/likeable/TestDomain.groovy",
            "grails-app/domain/ru/jconsulting/likeable/TestLiker.groovy"
    ]

    def title = "Likeable Plugin"
    def author = "Dmitry Borisov"
    def authorEmail = "turbo_di@outlook.com"
    def description = "A plugin, that adds like functionality to domain classes. A light version of Rateable plugin"

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/likeable"

    def license = "APACHE"
    def issueManagement = [ system: "GitHub", url: "https://github.com/turboDi/grails-likeable/issues" ]
    def scm = [ url: "https://github.com/turboDi/grails-likeable" ]

    def doWithSpring = {
        def config = application.config

        if (!config.grails.likeable.liker.evaluator) {
            config.grails.likeable.liker.evaluator = { request.user }
        }

        def domains = application.domainClasses.findAll {Likeable.class.isAssignableFrom(it.clazz)}.collect {it.clazz}
        likeableDomainRegistry(DomainTypeRegistry) {
            domainClassList = domains
        }
    }

    def doWithDynamicMethods = { ctx ->
        for (domainClass in application.domainClasses) {
            if (Likeable.class.isAssignableFrom(domainClass.clazz)) {
                domainClass.clazz.metaClass {

                    getTotalLikes = { ->
                        def instance = delegate
                        if (instance.id != null) {
                            Like.createCriteria().get {
                                projections {
                                    rowCount()
                                }
                                eq "likeRef", instance.id
                                eq "type", GrailsNameUtils.getPropertyName(instance.class)
                                cache true
                            }
                        } else {
                            return 0
                        }
                    }

                    like = { liker ->
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
                            l = new Like(likerId: liker.id, likeRef: instance.id, type: GrailsNameUtils.getPropertyName(instance.class))
                            if (!l.validate()) {
                                throw new LikeException("You must save the entity [${liker}] before calling like")
                            }
                            l.save()
                        }
                        // for an existing like, delete it
                        else {
                            l.delete()
                        }
                        return instance
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

}
