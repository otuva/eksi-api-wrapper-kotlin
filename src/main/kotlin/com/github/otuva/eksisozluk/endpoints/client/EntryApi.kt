package com.github.otuva.eksisozluk.endpoints.client

import com.github.otuva.eksisozluk.NotAuthorizedException
import com.github.otuva.eksisozluk.annotations.RequiresLogin
import com.github.otuva.eksisozluk.endpoints.Routes
import com.github.otuva.eksisozluk.models.authentication.UserType
import com.github.otuva.eksisozluk.models.entry.Entry
import com.github.otuva.eksisozluk.models.entry.favorite.EntryFavoriteData
import com.github.otuva.eksisozluk.models.topic.Topic
import com.github.otuva.eksisozluk.responses.EntryFavoriteResponse
import com.github.otuva.eksisozluk.responses.GenericResponse
import com.github.otuva.eksisozluk.responses.TopicResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

/**
 * Entry related endpoints
 *
 * @param client The [HttpClient] instance
 * @param userType The [UserType] instance
 * */
public class EntryApi(private val client: HttpClient, private val userType: UserType) {
    /**
     * Get a single entry by id. Note that this function only returns the entry content and not title. | tek entry
     * To get the content with title use [getAsTopic]
     *
     * @param entryId The id of the entry
     *
     * @return [Entry] object
     * */
    public suspend fun get(entryId: Int): Entry {
        val url = Routes.api + Routes.Entry.entry.format(entryId)

        val response = client.get(url)

        val topicResponse: TopicResponse = response.body()

        return topicResponse.data!!.getFirstEntry()
    }

    /**
     * Get entry content with title. | başlıklı tek entry
     *
     * @param entryId The id of the entry
     *
     * @return [Topic] object
     * */
    public suspend fun getAsTopic(entryId: Int): Topic {
        val url = Routes.api + Routes.Entry.entry.format(entryId)

        val response = client.get(url)

        val topicResponse: TopicResponse = response.body()

        return topicResponse.data!!
    }

    /**
     * Like an entry | şükela
     *
     * @param entryId The id of the entry
     *
     * @return [GenericResponse] object. It's kinda useless.
     * */
    public suspend fun like(entryId: Int): GenericResponse {
        val url = Routes.api + Routes.Entry.vote

        val response = client.post(url) {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("Rate", "1")
                        append("Id", entryId.toString())
                    }
                )
            )
        }

        return response.body()
    }

    /**
     * Dislikes given entry. | çok kötü
     *
     * @param entryId The id of the entry
     *
     * @return [GenericResponse] object. It's kinda useless.
     * */
    public suspend fun dislike(entryId: Int): GenericResponse {
        val url = Routes.api + Routes.Entry.vote

        val response = client.post(url) {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("Rate", "-1")
                        append("Id", entryId.toString())
                    }
                )
            )
        }

        return response.body()
    }

    /**
     * Add entry to favorites.
     * You need to be logged in to do this.
     *
     * @param entryId The id of the entry
     *
     * @return [EntryFavoriteData] object
     * */
    @RequiresLogin
    public suspend fun favorite(entryId: Int): EntryFavoriteData {
        check(userType == UserType.Regular) { NotAuthorizedException("Anonymous users cannot do this.") }

        val url = Routes.api + Routes.Entry.favorite

        val response: EntryFavoriteResponse = client.post(url) {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("Id", entryId.toString())
                    }
                )
            )
        }.body()

        return response.data
    }

    /**
     * Remove entry from favorites.
     * You need to be logged in to do this.
     *
     * @param entryId The id of the entry
     *
     * @return [EntryFavoriteData] object
     * */
    @RequiresLogin
    public suspend fun unfavorite(entryId: Int): EntryFavoriteData {
        check(userType == UserType.Regular) { NotAuthorizedException("Anonymous users cannot do this.") }

        val url = Routes.api + Routes.Entry.unfavorite

        val response: EntryFavoriteResponse = client.post(url) {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("Id", entryId.toString())
                    }
                )
            )
        }.body()

        return response.data
    }
}