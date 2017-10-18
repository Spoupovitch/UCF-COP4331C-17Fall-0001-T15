/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;
import am_utils.ArticleInfo;
import java.io.File;
import java.util.ArrayList;
/**
 *
 * @author NThering
 */
public class Public {

    /** Returns a list to the client of all articles in a given category/sub-category, this includes their ID number on the server. */
    ArrayList<ArticleInfo> GetArticlesFromCategory( int articleCategory, int subCategory )
    {
        return null;
    }
        
    /** Sends the article to the database for entry, returns 0 if successful and an int corresponding to the type of error if it was not.  If articleID is not 0 this upload is meant to replace an existing upload. 
     if ArticleID matches an articleID already in the database, replace it if the uploader is the owner of that article or is an admin.  Refuse to insert the new article if not.*/
    int InsertArticle( File articleFile, ArticleInfo articleInfo, int userPermissions )
    {
        return 1;
    }
    
    /** Deletes the article under the specified ID, if the user has ownership of it or is an admin. */
    int DeleteArticle( int articleID, String userUsername, int userPermissions )
    {
        return 1;
    }
        
    /** Gets information on the given article from the database. */
    ArticleInfo GetArticleInfo( int articleID )
    {
        return null;
    }
        
    /** Returns a handle to the given article file. */
    File DownloadArticle( int articleID )
    {
        return null;
    }
}