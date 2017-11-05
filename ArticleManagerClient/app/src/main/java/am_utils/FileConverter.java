package com.remaclek.kelcamer.academicarticlemanager;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created by Kelsey on 10/31/17.
 * // File pdffile = convertToPDF(createNewFile("helloworld2.txt"));
 //convertFromPDF(pdffile,3);
 */
public class FileConverter extends AppCompatActivity {
    String toast = "";


    FileConverter(){


    }

    public File convertToPDF(File inputFile ){

        if(!inputFile.exists()){
            return null;
        }

        String ext = getFileExt(inputFile.getName());
        if(ext.contains( "pdf")){
            return inputFile;
        }
        else if(ext.contains("txt")){

            String content = readFile(inputFile.getName());
            File pdf = createPDFFromString(content, inputFile.getName());
            return pdf;
        }
        else if(ext.contains("odt")){
            String content = readFile(inputFile.getName());
            File pdf = createPDFFromString(content, inputFile.getName());
            return pdf;
        }
        else if(ext.contains("html")){
            String content = readFile(inputFile.getName());
            File pdf = createPDFFromString(content, inputFile.getName());
            return pdf;

        }
        else{
            return null;
        }

    }


    /*
    1 = convert to .txt
    2 = convert to .odt
    3 = convert to .html
    default converts to .txt
    NOTE: CURRENTLY ONLY READS FROM ONE PAGE!
     */
    public String extractTextFromPDF(String filename, int filetype, int pageNumber){
        String result = "";

        filename = setPath(filename);
        try{
            PdfReader reader = new PdfReader(filename);

            result = PdfTextExtractor.getTextFromPage(reader, pageNumber);

            reader.close();

        }
        catch(IOException e){

            result = "fail";

        }




        return result;
    }

    public String setPath(String filepath){
        String correctedPath = "";
        if(!filepath.contains("Article Manager")){
            correctedPath = Environment.getExternalStorageDirectory() + "/Article Manager/" + filepath;
        }
        else{
            return filepath;
        }
        return correctedPath;

    }
    public void convertFromPDF(String filename, int filetype) {
        String result = extractTextFromPDF(filename, filetype, 1);

        switch (filetype) {
            case 1:
                // text file
                createNewFile(getFileWithoutExt(filename) + ".txt", result);

                break;
            case 2:
                createNewFile(getFileWithoutExt(filename) + ".odt", result);
                break;
            case 3:
                createNewFile(getFileWithoutExt(filename) + ".html", result);
                break;
            default:
                createNewFile(getFileWithoutExt(filename) + ".txt", result);
                break;
            case 4:
                break;


        }


    }


    public String readFile(String filename){


        File dir = createNewDirectory("Article Manager");
        String root = dir.getAbsolutePath() + "/" +filename;

        File sdcard = new File(root);
        //  Toast.makeText(getApplicationContext(), "Reading file " + sdcard.getPath(), Toast.LENGTH_LONG).show();

        //ConvertToPDF(sdcard);
        StringBuilder text = new StringBuilder();
        String result = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(sdcard));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
                result+=line;
            }
        }
        catch (IOException e) {
            //   Toast.makeText(getApplicationContext(),"Error reading file!",Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public File createPDFFromString(String data, String filename) {
        File dir = createNewDirectory("Article Manager");

        if(filename.contains(".")){
            filename = filename.substring(0, filename.indexOf("."));
        }
        String root = dir.getAbsolutePath() + "/" +filename+ ".pdf";
        try {

            File file = new File(root);

            file.createNewFile();



            FileOutputStream fOut = new FileOutputStream(file);


            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new
                    PdfDocument.PageInfo.Builder(100, 100, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // look up how to use default fonts in paint and make sure font sizes look good
         //   paint.setLetterSpacing((float)0.1);
            paint.setTextSize((float)3.0);
            paint.getFontMetrics();
            canvas.drawText(data, 10, 10, paint);


            document.finishPage(page);
            document.writeTo(fOut);
            document.close();
            return file;


        } catch (IOException e) {
            // Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();

            Log.i("error", e.getLocalizedMessage());
        }
        return null;
    }

    public static String getFileExt(String fileName) {

        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    public static String getFileWithoutExt(String fileName) {

        String removeDot = fileName.substring(0, fileName.lastIndexOf("."));
        String removeSlash = removeDot.substring(fileName.lastIndexOf("/"));
        return removeSlash;
    }
    public File createNewDirectory(String nameOfDir){
        // get the path to sdcard
        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        File dir = new File(sdcard.getAbsolutePath() + "/" + nameOfDir + "/");

        if(!dir.exists())
        {
            // create this directory if not already created
            dir.mkdir();
        }


        return dir;
    }

    public File createNewFile(String name, String data){
        // get the path to sdcard
        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        File dir = createNewDirectory("Article Manager");

        // create the file in which we will write the contents
        File file = new File(dir, name);

        toast = file.getAbsolutePath();


        try {
            FileOutputStream os = new FileOutputStream(file);

            os.write(data.getBytes());
            os.close();
        }
        catch(IOException e){
            return null;
        }

      //  Toast.makeText(getApplicationContext(),file.getAbsolutePath() + " created.", Toast.LENGTH_LONG).show();

        return file;
    }
}