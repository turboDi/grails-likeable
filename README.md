Likeable Grails plugin
===============

A plugin, that adds like functionality to domain classes. A light version of Rateable plugin

Installation
-------------------------

    grails install-plugin likeable

Usage
-------------------------

To use the plugin, you should define user evaluator and user's Class in grails-app/conf/Config.groovy:

    grails.plugin.likeable.liker.evaluator = { request.user }
    grails.plugin.likeable.liker.className = 'foo.bar.User'

To add like functionality to domain type, you should implement the Likeable interface:

    import ru.jconsulting.likeable.Likeable
    class Post implements Likeable {
    }

After implementing marker interface domain's metaClass will gain likeable methods:

* like(user) - adds a like by provided user, or remove existing
* userLike(user) - returns Like instance if user liked the Likeable, or null otherwise
* userLiked(user) - returns true if user liked the Likeable, or false otherwise
* getTotalLikes() - returns likes count for the Likeable