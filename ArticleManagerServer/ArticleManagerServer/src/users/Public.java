/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package users;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.sql.*;
import org.mariadb.jdbc.Driver;
//import com.mysql.jdbc.Driver;
/**
 *
 * @author NThering
 */

// Responsible for handling user logins.
public class Public {
    /**	Checks the username and password against the stored password hashes on the database.  Returns -1 if login failed and an integer corresponding to that user's permissions if successful. */
    /** Passwords must be stored securely ( NOT IN PLAINTEXT ) so that a data breach would not compromise them. */
    
private static String driver = "org.mariadb.jdbc.Driver";
    private static String URL = "jdbc:mariadb://localhost/article_manager";
    private static String USER = "root";
    private static String PASS = "cop4331";
    private static String genPassword = null;

    public static Connection conn = null;

    public static int login(String username, String password)
    {
        /**	Checks the username and password against the stored password hashes on the database.  Returns -1 if login failed and an integer corresponding to that user's permissions if successful. */
        /** Passwords must be stored securely ( NOT IN PLAINTEXT ) so that a data breach would not compromise them. */
    	
    	return 0;
    	
    	/*
        try
        {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(URL, USER, PASS);

            PreparedStatement statement = connection.prepareStatement("SELECT 'name', 'password' FROM users WHERE 'name' = ?");
            ResultSet r1 = statement.executeQuery();
            statement.setString(1, username);
            byte[] salty = salt();
            if(r1.next())
            {
                if(r1.getString("password").equals(securePassword(password, salty))) {
                    System.out.println("Login successful");
                    return 0; //return 0 for user
                }

            }

            else{
                System.out.println("Invalid Login");
                return -1;
            }
        }

        catch (SQLException e)
        {
            System.out.println("SQL Exception: " + e.toString());
        }
        catch (ClassNotFoundException ce)
        {
            System.out.println("Class Not Found Exception: " + ce.toString());
        } catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
			System.exit(-1);
		}
        return -1;*/
    }

    /** Ensures that the username is unique and can be registered in the database, then registers it if so.  Returns true if registration successful and false if not. */
    public static boolean register(String username, String password)
    {
        try
        {
            Class.forName("org.mariadb.jdbc.Driver");
            Connection connection = DriverManager.getConnection(URL, USER, PASS);

            PreparedStatement st = connection.prepareStatement("select * from users where name= \"" + username + "\"");
            PreparedStatement st2;
            ResultSet r1 = st.executeQuery();
            String usernameCounter;
            byte[] salty = salt();
            if(r1.next())
            {
                usernameCounter = r1.getString("name");
                if(usernameCounter.equals(username))
                {
                    System.out.println("Username already exists");
                    return false;
                }
            } else{

                //add username to the database.
            	String secure = securePassword(password, salty);
                System.out.println("Username is available!");
                st2 = connection.prepareStatement("insert into users values (" + "\""+ username + "\"" + ", " + "\""+ secure +"\"" + ", " + "0 ," + "NULL" + ")");
                st2.executeQuery();
            }
            
        }

        catch (SQLException e)
        {
            System.out.println("SQL Exception: " + e.toString());
            return false;
        }

        catch (ClassNotFoundException ce)
        {
            System.out.println("Class Not Found Exception: " + ce.toString());
        } catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
			System.exit(-1);
		}
        return true;
    }

    public static String securePassword(String password, byte[] salt) throws NoSuchAlgorithmException
    {
        //utilizing MD5 algorithm to hash passwords.
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());

        byte []byteData = md.digest();

        //convert byte to hex
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < byteData.length; i++)
        {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        //Gets hashed password
        genPassword = sb.toString();

        return genPassword;
    }

    public static byte[] salt() throws NoSuchAlgorithmException, NoSuchProviderException
    {
        SecureRandom sec = SecureRandom.getInstance("SHA1PRNG", "SUN");

        //create salt array
        byte[]salt = new byte[16];

        //get random salt
        sec.nextBytes(salt);

        return salt;
    }
}
