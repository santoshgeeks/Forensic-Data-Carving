
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author santosh rai
 */
public class KMPTest {
    public static void main(String[] abc){
        try{
       // findFile("25504446", "2525454F46", "\\\\.\\A:", ".pdf", 0,"\\\\.\\G:\\data1");
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }


    public static void startCarving(File drivePath, String fileType, File outputDir) {
        try {
            String absoluePath = outputDir.getAbsolutePath();
            System.out.println(outputDir);
            String absoluteDir = outputDir.getAbsolutePath();
            System.out.println("caling only with drive and filetype :"+absoluteDir);
            String dir = null;
            if (drivePath.getName().contains(":") && drivePath.getName().length() <= 3) {
                dir = "\\\\.\\" + drivePath.getName().charAt(0) + ":";
            } else {
                dir = drivePath.getAbsolutePath();

            }
            switch(fileType){
                case "image":
                    findFile("89504E470D0A1A0A", "49454E44", dir, ".png", 0,absoluePath);
                    findFile("47494638", "003B", dir, ".gif", 0,absoluePath);
                   // findFile("47494638","003B","\\\\.\\A:",".gif",0,absoluePath);
                    break;
                case "html":
                    findFile("3C21444F","746D6C3E",dir,".html",0, absoluePath);
                    break;
                case "pdf":
                    findFile("25504446","2525454F46",dir,".pdf",10, absoluePath);
                    break;
                case "doc":
                     //findFile("504B030414000600080000002100","504B0506",dir,".docx",44, absoluePath);
                    findFile("504B0304","504B0506",dir,".docx",44, absoluePath);
                    break;
                case "ppt":
                    //findFile("504B030414000600080000002100","504B0506",dir,".pptx",44, absoluePath);
                    findFile("504B0304","504B0506",dir,".pptx",44, absoluePath);
                    break;
                case "excel":
                     findFile("504B0304","504B0506",dir,".xlsx",44, absoluePath);
                    break;
                case "thumbdb":
                    break;
                case "zip":
                    findFile("504B0304","504B0506",dir,".zip",46, absoluePath);
                    break;
                case "bmp":
                    break;
                case "avi":
                    break;
                case "dat":
                    break;
                case "mp4":
                    findFile("0000002066747970","",dir,".mp4",2^500, absoluePath);
                    break;
                case "mov":
                    break;
                case "mp3":
                    //findFile("FFF1","MP3_FF",dir,".mp3",46, absoluePath);
                    //findFile("mp41","MP3_FF",dir,".mp3",46, absoluePath);
                    findFile("FFFB90640000","3480000004AAAAAA",dir,".mp3",764, absoluePath);
                    findFile("FFFA90640000","010c",dir,".mp3",764, absoluePath);
                    
                    break;
                case "wmv":
                    break;
            }
        } catch (Exception ex) {
            System.out.println("Something is not right!" + ex.getMessage());
        }
    }
    private static void findFile(String startHex, String endHex, String diskName, String fileExt, int extraChars,String outputDir) throws FileNotFoundException, IOException {
        File diskRoot = new File(diskName);
        RandomAccessFile diskAccess = new RandomAccessFile(diskRoot, "rws");

        int length = 1024;
        int currentByte = 0;
        Boolean found_Start = false;
        Boolean found_End = false;
        byte[] myFile = new byte[1024 * 10240];

        int fileIndex = 0;
        int fileStartOff = -1, fileEndOff = -1;
        int fileCount = 0;
        while (true) {
            byte[] content = new byte[length];
            int bytesLen = diskAccess.read(content, 0, length);
            String hex = "";
            for (byte b : content) {
                hex += String.format("%02X", b);
            }
            /*
             Size of one chunk is 1024 byte. 1 byte = 2 charecters in hex.
             Hence, 1024 byte = 2048 chars in hex.
             We have to find start and end pointer in this 2048 charecters in each chunk
             */
            if (!found_Start) {

                fileStartOff = new BoyerMoore(startHex).search(hex);
                if (fileStartOff != -1) {
                    found_Start = true;
                    System.out.println("Found start");
                }
            } else if (!found_End) {
                fileEndOff = new BoyerMoore(endHex).search(hex);
                if (fileEndOff != -1) {
                    found_End = true;
                }
            }
            if (found_Start && !found_End) {
                for (byte b : content) {
                    myFile[fileIndex++] = b;
                }
            } else if (found_Start && found_End) {//reached at the end of one file
                for (byte b : content) {
                    myFile[fileIndex++] = b;
                }
                byte[] aFile = new byte[fileIndex];
                for (int i = 0; i < fileIndex; i++) {
                    aFile[i] = myFile[i];
                }
                fileEndOff += extraChars;//+endHex.length();
                storeThis(aFile, fileIndex, fileStartOff, fileEndOff, "File" + fileCount++ + fileExt,outputDir);
                found_Start = false;
                found_End = false;
                myFile = new byte[1024 * 10240];
                fileStartOff = -1;
                fileEndOff = -1;
                fileIndex = 0;
            }
            currentByte++;
            if (bytesLen == -1)//Reached at the end of disk
            {
                break;
            }
        }

        System.out.println("Total::" + currentByte * length);
    }

    private static void storeThis(byte[] file, int size, int fileStartOff, int fileEndOff, String fileName,String outputDir) throws IOException {
        File outFile = new File(outputDir +File.separator+fileName);
        System.out.println("storing file into:"+outFile);
        double chunkCount = file.length / 2048;
        int actualByte = file.length - (2048 - fileEndOff) / 2;
        byte[] actualFile = new byte[actualByte];
        for (int i = 0; i < actualByte; i++) {
            actualFile[i] = file[i];
        }
        System.out.println("TotalChunks=" + chunkCount);
        FileOutputStream fop = new FileOutputStream(outFile, true);
        System.out.println(fileStartOff + ", " + fileEndOff);
        System.out.println("Storing:" + size / 1024 + "KB");
        fop.write(actualFile);
        fop.flush();
        fop.close();
    }
}
