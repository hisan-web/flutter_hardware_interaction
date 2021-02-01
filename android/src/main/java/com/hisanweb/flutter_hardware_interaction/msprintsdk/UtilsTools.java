package com.hisanweb.flutter_hardware_interaction.msprintsdk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.util.Base64;
import android.util.Log;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;
import static com.hisanweb.flutter_hardware_interaction.msprintsdk.PrintCmd.PrintDiskImagefile;
import static javax.xml.transform.OutputKeys.ENCODING;


public class UtilsTools {
    private static String hexString = "0123456789ABCDEF";
    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        System.out.println(bmp.getConfig());
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        if(width>640){
            width = 640;
        }
//        if (height > 2000){
//            return null;
//        }
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray=new int[height*width];
        try{
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int grey = pixels[width * i + j];
                    int red = ((grey & 0x00FF0000 ) >>16);
                    gray[width*i+j]=red;
                }
            }
        }catch (Exception e){
            Log.e(TAG, "PrintBmp:" + e.getMessage());
        }

        int e=0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int g=gray[width*i+j];
                if (g>=128) {
                    pixels[width*i+j]=0xffffffff;
                    e=g-255;

                }else {
                    pixels[width*i+j]=0xff000000;
                    e=g-0;
                }
                if (j<width-1&&i<height-1) {
                    gray[width*i+j+1]+=3*e/8;
                    gray[width*(i+1)+j]+=3*e/8;
                    gray[width*(i+1)+j+1]+=e/4;
                }else if (j==width-1&&i<height-1) {
                    gray[width*(i+1)+j]+=3*e/8;
                }else if (j<width-1&&i==height-1) {
                    gray[width*(i)+j+1]+=e/4;
                }
            }

        }

        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, height);
        return resizeBmp;
    }


    public static int saveBmpFile(Bitmap bitmap, String strFileName) {
        //String filename = "/storage/emulated/0/Music/test.bmp";
        int iResult = -1;
        if (bitmap == null)
            return iResult;
        int nBmpWidth = bitmap.getWidth();
        int nBmpHeight = bitmap.getHeight();
        int bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4);
        try {
            File file = new File(strFileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileos = new FileOutputStream(strFileName);
            int bfType = 0x4d42;
            long bfSize = 14 + 40 + bufferSize;
            int bfReserved1 = 0;
            int bfReserved2 = 0;
            long bfOffBits = 14 + 40;
            writeWord(fileos, bfType);
            writeDword(fileos, bfSize);
            writeWord(fileos, bfReserved1);
            writeWord(fileos, bfReserved2);
            writeDword(fileos, bfOffBits);
            long biSize = 40L;
            long biWidth = nBmpWidth;
            long biHeight = nBmpHeight;
            int biPlanes = 1;
            int biBitCount = 24;
            long biCompression = 0L;
            long biSizeImage = 0L;
            long biXpelsPerMeter = 0L;
            long biYPelsPerMeter = 0L;
            long biClrUsed = 0L;
            long biClrImportant = 0L;
            writeDword(fileos, biSize);
            writeLong(fileos, biWidth);
            writeLong(fileos, biHeight);
            writeWord(fileos, biPlanes);
            writeWord(fileos, biBitCount);
            writeDword(fileos, biCompression);
            writeDword(fileos, biSizeImage);
            writeLong(fileos, biXpelsPerMeter);
            writeLong(fileos, biYPelsPerMeter);
            writeDword(fileos, biClrUsed);
            writeDword(fileos, biClrImportant);
            byte bmpData[] = new byte[bufferSize];
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
            for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol)
                for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth; wRow++, wByteIdex += 3) {
                    int clr = bitmap.getPixel(wRow, nCol);
                    bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);
                }
            fileos.write(bmpData);
            fileos.flush();
            fileos.close();
            iResult = 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return iResult;
    }

    protected static void writeWord(FileOutputStream stream, int value) throws IOException {
        byte[] b = new byte[2];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        stream.write(b);
    }

    protected static void writeDword(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    protected static void writeLong(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    public static String readTxt(String path){
        String str = "";
        try {
            File urlFile = new File(path);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(urlFile), "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String mimeTypeLine = null ;
            while ((mimeTypeLine = br.readLine()) != null) {
                str = str+mimeTypeLine;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  str;
    }

    public static String encodeCN(String data) {
        byte[] bytes;
        try {
            bytes = data.getBytes("gbk");
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (int i = 0; i < bytes.length; i++) {
                sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
                sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
                // sb.append(" ");
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String encodeStr(String data) {
        String result = "";
        byte[] b;
        try {
            b = data.getBytes("gbk");
            for (int i = 0; i < b.length; i++) {
                result += Integer.toString((b[i] & 0xff) + 0x100, 16)
                        .substring(1);
                // result += " ";
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isCN(String data) {
        boolean flag = false;
        String regex = "^[\u4e00-\u9fa5]*$";
//		String regex = "^[һ-��]*$";
        if (data.matches(regex)) {
            flag = true;
        }
        return flag;
    }

    //获取文件后缀名
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }


    /**
     * 字符串转含0x 16进制
     * @param paramString
     * @return
     */
    private static byte[] hexStr2Bytesnoenter(String paramString) {
        String[] paramStr = paramString.split(" ");
        byte[] arrayOfByte = new byte[paramStr.length];

        for (int j = 0; j < paramStr.length; j++) {
            arrayOfByte[j] = Integer.decode("0x" + paramStr[j]).byteValue();
        }
        return arrayOfByte;
    }

    /// <summary>
    /// byte数组转int数组
    /// </summary>
    /// <param name="src">源byte数组</param>
    /// <param name="offset">起始位置</param>
    /// <returns></returns>
    public static int[] bytesToInt(byte[] src, int offset)
    {
        int[] values=new int[src.length/4];
        for (int i = 0; i < src.length / 4; i++)
        {
            int value = (int)((src[offset] & 0xFF)
                    | ((src[offset + 1] & 0xFF) << 8)
                    | ((src[offset + 2] & 0xFF) << 16)
                    | ((src[offset + 3] & 0xFF) << 24));
            values[i] = value;
            offset += 4;
        }
        return values;
    }

    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return  转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String unicodeToUtf8 (String s) {
        Log.e("TAG",getEncoding(s));
        try {
            if(getEncoding(s) == "UTF-8"){
                return new String( s.getBytes("GBK") , "UTF-8");
            }else
            if(getEncoding(s) == "GBK"||getEncoding(s) == "GB2312"){
                return new String( s.getBytes("GBK") , "GBK");
            }else
            if(getEncoding(s) == "ISO-8859-1"){
                return new String( s.getBytes("ISO-8859-1") , "UTF-8");
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s = encode;
                return s;
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return "";
    }




    /**
     * 截取byte[]
     * @param data 被截取数组
     * @param start 起始位置
     * @param length 截取长度
     * @return
     */
    public static byte[] byteSub(byte[] data, int start, int length) {
        byte[] bt = new byte[length];

        if(start + length > data.length) {
            bt = new byte[data.length-start];
        }

        for(int i = 0; i < length &&(i + start) < data.length; i++) {
            bt[i] = data[i + start];
        }
        return bt;
    }

    /**
     * 判断字符串内是不是16进制的值
     * @param str
     * @return
     */
    public static boolean isHexStrValid(String str) {
        String pattern = "^[0-9A-F]+$";
        return Pattern.compile(pattern).matcher(str).matches();
    }



    // -------------------------------------------------------
    // 转hex字符串转字节数组
    static public byte[] hexToByteArr(String inHex)// hex字符串转字节数组
    {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen) == 1) {// 奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {// 偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    static public byte hexToByte(String inHex)// Hex字符串转byte
    {
        return (byte) Integer.parseInt(inHex, 16);
    }
    // 判断奇数或偶数，位运算，最后一位是1则为奇数，为0是偶数
    static public int isOdd(int num) {
        return num & 0x1;
    }


    public static byte[] PrintBase64(String file) {
        int imgWidth ,imgHeigh;
        String base64Data = readTxt(file).trim();
        Bitmap bitmap = null;
        try {
            byte[] bytes = Base64.decode(base64Data.split(",")[1], Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            bitmap = convertToBlackWhite(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        imgWidth = bitmap.getWidth();
        imgHeigh = bitmap.getHeight();
        int iDataLen = imgWidth * imgHeigh;
        int[] pixels = new int[iDataLen];
        bitmap.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeigh);
        int[] data1 = pixels;
        byte base64 [] =  PrintDiskImagefile(data1, imgWidth, imgHeigh);

//        mUsbDriver.write(PrintCmd.PrintFeedline(10));
//        mUsbDriver.write(PrintCmd.PrintCutpaper(0));
//        mUsbDriver.write(PrintCmd.SetClean());
//        mUsbDriver.closeUsbDevice();
        return base64;
    }

    public static int[] getBitmapParamsData(String imgPath) {
        int imgWidth ,imgHeigh;
        FileInputStream file = null;
        try {
            file = new FileInputStream(imgPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(file);//
        System.out.println(imgPath.substring(imgPath.indexOf(".") + 1));
        if (!imgPath.substring(imgPath.indexOf(".") + 1).equals("bmp")) {
            bitmap = convertToBlackWhite(bitmap);
        }
            imgWidth = bitmap.getWidth();
            imgHeigh = bitmap.getHeight();
            int iDataLen = imgWidth * imgHeigh;
            int[] pixels = new int[iDataLen];
            bitmap.getPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeigh);
            return pixels;
        }

//    -----------------------------------------------------------------------------------
    /**
     * 将字符串形式表示的十六进制数转换为byte数组
     */
    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toLowerCase();
        String[] hexStrings = hexString.split(" ");
        byte[] bytes = new byte[hexStrings.length];
        for (int i = 0; i < hexStrings.length; i++) {
            char[] hexChars = hexStrings[i].toCharArray();
            bytes[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
        }
        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
        // 改成小写 return (byte) "0123456789ABCDEF".indexOf(c);
    }

    // -------------------------------------------------------
    static public String byteArrToHex(byte[] inBytArr)// 字节数组转转hex字符串
    {
        StringBuilder strBuilder = new StringBuilder();
        int j = inBytArr.length;
        for (int i = 0; i < j; i++) {
            strBuilder.append(Byte2Hex(inBytArr[i]));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }
    // -------------------------------------------------------
    static public String Byte2Hex(Byte inByte)// 1字节转2个Hex字符
    {
        return String.format("%02x", inByte).toUpperCase();
    }
 
    //打开txt文件获取内容
    public static String ReadTxtFile(String strFilePath){
        String path = strFilePath;
        String str = "";
        List<String> newList=new ArrayList<String>();
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()){
            Log.d("TestFile", "The File doesn't not exist.");
        }
        else{
            try {
                // 获取文件
                FileInputStream fin = new FileInputStream(strFilePath);
                // 获得长度
                int length = fin.available();
                // 创建字节数组
                byte[] buffer = new byte[length];
                // 读取内容
                fin.read(buffer);
                // 获得编码格式
                String type = codetype(buffer);
                // 按编码格式获得内容
                str = EncodingUtils.getString(buffer, type);

            } catch (FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return str;
    }

    /**
     * // 获得编码格式
     * @param head
     * @return
     */
    private static String codetype(byte[] head) {
        String type = "";
        byte[] codehead = new byte[3];
        System.arraycopy(head, 0, codehead, 0, 3);
        if(codehead[0] == -1 && codehead[1] == -2) {
            type = "UTF-16";
        }
        else if(codehead[0] == -2 && codehead[1] == -1) {
            type = "UNICODE";
        }
        else if(codehead[0] == -17 && codehead[1] == -69 && codehead[2] == -65) {
            type = "UTF-8";
        }
        else {
            type = "GB2312";
        }
        return type;
    }

    //从resources中的raw 文件夹中获取文件并读取数据
    public static String getFromRaw(InputStream in){
        String result = "";
        try {
            //获取文件的字节数
            int lenght = in.available();
            //创建byte数组
            byte[]  buffer = new byte[lenght];
            //将文件中的数据读到byte数组中
            in.read(buffer);
            result = EncodingUtils.getString(buffer, ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 获取当前时间
     * @return
     */
    public static String data(){
        Date date = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss ");

        String sim = dateFormat.format(date);
        return sim;
    }
}
