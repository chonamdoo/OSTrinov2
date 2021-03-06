package com.odd.ostrinov2.tools

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.odd.ostrinov2.Ost

import java.util.ArrayList

class DBHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private var ostList: List<Ost>? = null
    private var showList: List<String>? = null
    private var tagsList: List<String>? = null

    val allOsts: List<Ost>
        get() {

            val ostList = ArrayList<Ost>()

            val selectQuery = "SELECT $KEY_OST_ID,$KEY_OST_TITLE,$KEY_SHOW,$KEY_OST_TAG,$KEY_OST_URL FROM " +
                    "$OST_TABLE INNER JOIN $SHOW_TABLE ON $KEY_SHOW_ID = $FKEY_OST_SHOW_ID"
            val cursor = writableDatabase.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(0)
                    val title = cursor.getString(1)
                    val show = cursor.getString(2)
                    val tags = cursor.getString(3)
                    val url = cursor.getString(4)
                    val ost = Ost(title, show, tags, url)
                    ost.id = id

                    ostList.add(ost)

                } while (cursor.moveToNext())
            }
            cursor.close()
            return ostList
        }


    val allShows: List<String>
        get() {

            val showList = ArrayList<String>()

            val cursor = writableDatabase.rawQuery("SELECT * FROM $SHOW_TABLE", null)
            if (cursor.moveToFirst()) {
                do {

                    showList.add(cursor.getString(1))

                } while (cursor.moveToNext())
            }
            cursor.close()
            return showList
        }

    val allTags: List<String>
        get() {
            val tagsTable = ArrayList<String>()
            val cursor = writableDatabase.rawQuery("SELECT * FROM $TAGS_TABLE", null)
            if (cursor.moveToFirst()) {
                do {
                    tagsTable.add(cursor.getString(1))

                } while (cursor.moveToNext())
            }
            cursor.close()
            return tagsTable
        }

    init {
        ostList = ArrayList()
        showList = ArrayList()
        tagsList = ArrayList()
    }

    //creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_SHOW_TABLE = "CREATE TABLE $SHOW_TABLE($KEY_SHOW_ID INTEGER UNIQUE PRIMARY KEY," +
                "$KEY_SHOW TEXT )"
        db.execSQL(CREATE_SHOW_TABLE)

        val CREATE_TAGS_TABLE = "CREATE TABLE $TAGS_TABLE($KEY_TAG_ID INTEGER UNIQUE PRIMARY KEY," +
                "$KEY_OST_TAG TEXT )"
        db.execSQL(CREATE_TAGS_TABLE)

        val CREATE_OST_TABLE = "CREATE TABLE $OST_TABLE($KEY_OST_ID INTEGER PRIMARY KEY," +
                "$KEY_OST_TITLE TEXT,$FKEY_OST_SHOW_ID INTEGER,$KEY_OST_TAG TEXT, $KEY_OST_URL TEXT," +
                "FOREIGN KEY($FKEY_OST_SHOW_ID) REFERENCES $SHOW_TABLE($KEY_SHOW_ID))"
        db.execSQL(CREATE_OST_TABLE)

        /*val CREATE_TAGS_OST_TABLE = "CREATE TABLE $OST_TAGS_TABLE($FKEY_TAG_ID INTEGER " +
                "KEY,$FKEY_OST_ID TEXT, UNIQUE($FKEY_TAG_ID, $FKEY_OST_ID ))"
        db.execSQL(CREATE_TAGS_OST_TABLE)*/

        val CREATE_PLAYLIST_TABLE = "CREATE TABLE $PLAYLIST_TABLE($KEY_PLAYLIST_ID INTEGER UNIQUE " +
                "PRIMARY KEY,$KEY_PLAYLIST_NAME TEXT )"
        db.execSQL(CREATE_PLAYLIST_TABLE)

        val CREATE_PLAYLIST_OST_TABLE = "CREATE TABLE $PLAYLIST_OST_TABLE($KEY_PLAYLIST_ID INTEGER," +
                "$KEY_TAG_ID INTEGER, UNIQUE($KEY_PLAYLIST_ID, $KEY_TAG_ID ))"
        db.execSQL(CREATE_PLAYLIST_OST_TABLE)
        Log.i("DBHandlerOnCreate", "Created tables")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS " + OST_TABLE)

        onCreate(db)
    }

    fun addNewOst(newOst: Ost) {
        val values = ContentValues()

        val show = newOst.show
        val tags = newOst.tags

        addNewTagsandShows(show, tags)

        values.put(KEY_OST_TITLE, newOst.title)
        values.put(KEY_OST_TAG, tags)
        values.put(FKEY_OST_SHOW_ID, getShowId(show))
        values.put(KEY_OST_URL, newOst.url)

        //inserting Row
        writableDatabase.insert(OST_TABLE, null, values)
        Log.i("AddNewOst", "row inserted")

    }

    private fun addNewShow(newShow: String) {
        val values = ContentValues()

        values.put(KEY_SHOW, newShow)
        writableDatabase.insert(SHOW_TABLE, null, values)
        //println(allShows)
        Log.i("AddNewShow", "added new show")

    }

    private fun getShowId(show: String): Int {
        val cursor: Cursor = writableDatabase.rawQuery("SELECT $KEY_SHOW_ID " +
                "FROM $SHOW_TABLE WHERE $KEY_SHOW = ?", arrayOf(show))
        val showId: Int
        if (cursor.moveToFirst()) {
            showId = cursor.getInt(0)
        } else {
            Log.i("Retrieveerror", "empty cursor, no entry with name \$show")
            showId = 0
        }
        cursor.close()
        return showId
    }

    private fun getTagsId(tagString: String): Int {
        val cursor: Cursor = writableDatabase.rawQuery("SELECT $KEY_TAG_ID " +
                "FROM $TAGS_TABLE WHERE $KEY_OST_TAG = ?", arrayOf(tagString))
        val tagsId: Int
        if (cursor.moveToFirst()) {
            tagsId = cursor.getInt(0)
        } else {
            Log.i("Retrieveerror", "empty cursor, no entry with name \$show")
            tagsId = 0
        }
        cursor.close()
        return tagsId
    }

    private fun addNewTag(newTag: String) {
        val values = ContentValues()

        values.put(KEY_OST_TAG, newTag)
        writableDatabase.insert(TAGS_TABLE, null, values)
        Log.i("AddNewTag", "added new tag")
    }

    fun emptyTable() {
        //Truncate does not work in sqllite
        val TRUNCATE_TABLE = "DROP TABLE IF EXISTS $OST_TABLE"
        writableDatabase.execSQL(TRUNCATE_TABLE)

        val TRUNCATE_TABLE2 = "DROP TABLE IF EXISTS $SHOW_TABLE"
        writableDatabase.execSQL(TRUNCATE_TABLE2)

        val TRUNCATE_TABLE3 = "DROP TABLE IF EXISTS $TAGS_TABLE"
        writableDatabase.execSQL(TRUNCATE_TABLE3)

        val TRUNCATE_TABLE4 = "DROP TABLE IF EXISTS $PLAYLIST_TABLE"
        writableDatabase.execSQL(TRUNCATE_TABLE4)

        val TRUNCATE_TABLE5 = "DROP TABLE IF EXISTS $PLAYLIST_OST_TABLE"
        writableDatabase.execSQL(TRUNCATE_TABLE5)

        val TRUNCATE_TABLE6 = "DROP TABLE IF EXISTS $OST_TAGS_TABLE"
        writableDatabase.execSQL(TRUNCATE_TABLE6)

        onCreate(writableDatabase)
    }

    fun deleteOst(delID: Int): Boolean = writableDatabase.delete(OST_TABLE,
            KEY_OST_ID + "=" + delID, null) > 0

    fun getOst(id: Int): Ost? {

        val selectQuery = "SELECT $KEY_OST_ID,$KEY_OST_TITLE,$KEY_SHOW,$KEY_OST_TAG,$KEY_OST_URL FROM " +
                "$OST_TABLE INNER JOIN $SHOW_TABLE ON $KEY_SHOW_ID = $FKEY_OST_SHOW_ID WHERE $KEY_OST_ID IS $id"

        val cursor = writableDatabase.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            val ostId = cursor.getInt(0)
            val title = cursor.getString(1)
            val show = cursor.getString(2)
            val tags = cursor.getString(3)
            val url = cursor.getString(4)
            val ost = Ost(title, show, tags, url)
            ost.id = ostId
            cursor.close()
            return ost
        } else {
            Log.i("Retrieveerror", "empty cursor, no entry with id \$id")

        }
        return null
    }

    fun updateOst(ost: Ost) {

        addNewTagsandShows(ost.show, ost.tags)

        val values = ContentValues()

        values.put(KEY_OST_TITLE, ost.title)
        values.put(FKEY_OST_SHOW_ID, getShowId(ost.show))
        values.put(KEY_OST_TAG, ost.tags)
        values.put(KEY_OST_URL, ost.url)

        writableDatabase.update(OST_TABLE, values,"$KEY_OST_ID=?", arrayOf(ost.id.toString()) )
    }

    fun checkiIfOstInDB(ost: Ost): Boolean {
        ostList = allOsts
        val ostString = ost.toString().toLowerCase()
        for (ostFromDB in ostList!!) {
            if (ostFromDB.toString().toLowerCase() == ostString) {
                return true
            }
        }
        return false
    }


    private fun checkIfShowInDB(show: String): Boolean {
        showList = allShows
        val showString = show.toLowerCase()
        allShows.forEach{print("$it, ")}
        showList!!.forEach {
            if (it.toLowerCase() == showString) {
                return true
            }
        }
        return false
    }

    private fun checkIfTagInDB(tag: String): Boolean {
        tagsList = allTags
        val tagString = tag.toLowerCase()
        tagsList!!.forEach {
            if (it.toLowerCase() == tagString) {
                return true
            }
        }
        return false
    }

    private fun addNewTagsandShows(show: String, tagString: String) {
        var tagString = tagString
        println("Add new tags and shows")
        if (!checkIfShowInDB(show)) {
            addNewShow(show)
        }

        if (tagString.endsWith(",")) {
            tagString = tagString.trim(',') //substring(0, tagString.length - 1)
        }
        val tags = tagString.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        tags.forEach { print("$it, ")}
        tags.forEach {
            if (!checkIfTagInDB(it)) {
                addNewTag(it)
            }
        }
    }

    fun reCreateTagsAndShowTables() {
        ostList = allOsts
        val TRUNCATE_TABLE = "DROP TABLE " + TAGS_TABLE + ""
        writableDatabase.execSQL(TRUNCATE_TABLE)
        val TRUNCATE_TABLE2 = "DROP TABLE " + SHOW_TABLE + ""
        writableDatabase.execSQL(TRUNCATE_TABLE2)

        val CREATE_SHOW_TABLE = "CREATE TABLE $SHOW_TABLE($KEY_SHOW_ID INTEGER UNIQUE PRIMARY KEY," +
                "$KEY_SHOW TEXT )"
        writableDatabase.execSQL(CREATE_SHOW_TABLE)

        val CREATE_TAGS_TABLE = "CREATE TABLE $TAGS_TABLE($KEY_TAG_ID INTEGER UNIQUE PRIMARY KEY," +
                "$KEY_OST_TAG TEXT )"
        writableDatabase.execSQL(CREATE_TAGS_TABLE)

        for (ost in ostList!!) {
            addNewTagsandShows(ost.show, ost.tags)
        }
    }

    companion object {

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ostdb"
        private const val OST_TABLE = "ostTable"
        private const val SHOW_TABLE = "showTable"
        private const val TAGS_TABLE = "tagsTable"
        private const val PLAYLIST_TABLE = "playListTable"
        private const val PLAYLIST_OST_TABLE = "playListOstTable"
        //private const val OST_SHOW_TABLE = "ostShowTable"
        private const val OST_TAGS_TABLE = "ostTagsTable"

        private const val KEY_OST_ID = "ostid"
        private const val KEY_OST_TITLE = "title"
        private const val KEY_SHOW = "show"
        private const val KEY_OST_URL = "url"
        private const val KEY_OST_TAG = "tag"

        private const val KEY_PLAYLIST_ID = "playlistId"
        private const val KEY_PLAYLIST_NAME = "playlistName"
        private const val KEY_TAG_ID = "tagId"
        private const val KEY_SHOW_ID = "showId"

        private const val FKEY_OST_ID = "ost_id"
        private const val FKEY_TAG_ID = "tag_id"
        private const val FKEY_SHOW_ID = "show_id"
        private const val FKEY_OST_SHOW_ID = "ostShowId"
        private const val FKEY_OST_TAG_ID = "ostTagId"
    }
}
