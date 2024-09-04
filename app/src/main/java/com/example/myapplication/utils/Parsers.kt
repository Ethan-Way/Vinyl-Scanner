package com.example.myapplication.utils

import org.json.JSONObject

fun parseRecord(record: String): Pair<String, String>? {
    val parts = record.split(" - ")
    return if (parts.size == 2) {
        val artist = parts[0].trim()
        val album = parts[1].trim()
        Pair(artist, album)
    } else {
        null
    }
}

fun extractLinks(json: String): Pair<String, String>? {
    val jsonObject = JSONObject(json)
    val albums = jsonObject.getJSONObject("albums")
    val items = albums.getJSONArray("items")

    if (items.length() > 0) {
        val albumItem = items.getJSONObject(0)

        // Extract the artist link
        val artistArray = albumItem.getJSONArray("artists")
        val artistObject = artistArray.getJSONObject(0)
        val artistLink = artistObject.getJSONObject("external_urls").getString("spotify")

        // Extract the album link
        val albumLink = albumItem.getJSONObject("external_urls").getString("spotify")

        return Pair(artistLink, albumLink)
    }

    return null
}

fun extractArtistApi(json: String): String? {
    val jsonObject = JSONObject(json)
    val albums = jsonObject.getJSONObject("albums")
    val items = albums.getJSONArray("items")

    if (items.length() > 0) {
        val albumItem = items.getJSONObject(0)

        val artistArray = albumItem.getJSONArray("artists")
        val artistObject = artistArray.getJSONObject(0)

        val artistCall = artistObject.getString("href")

        return artistCall
    }

    return null
}

// get the smallest artist image
fun extractArtistImage(json: String): String? {
    val jsonObject = JSONObject(json)
    val imagesArray = jsonObject.getJSONArray("images")
    var imageUrl: String? = null
    var smallestWidth = Int.MAX_VALUE

    for (i in 0 until imagesArray.length()) {
        val imageObect = imagesArray.getJSONObject(i)
        val width = imageObect.getInt("width")

        if (width < smallestWidth) {
            smallestWidth = width
            imageUrl = imageObect.getString("url")
        }
    }
    return imageUrl
}