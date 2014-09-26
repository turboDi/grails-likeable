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

class Like {

    def grailsApplication

    Date dateCreated
    Long likerId
    Long likeRef
    String type

    static constraints = {
        likerId min: 0L
        likeRef min: 0L
        type blank: false
    }

    static mapping = {
        table "l_like"
        cache true
    }

    def getLiker() {
        String likerClass = grailsApplication.config.grails.plugin.likeable.liker.className
        getClass().classLoader.loadClass(likerClass).get(likerId)
    }

    @Override
    String toString() {
        return "$liker liked $type : $likeRef"
    }
}
