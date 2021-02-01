package com.hisanweb.flutter_hardware_interaction.msprintsdk;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface JNAData1 extends Library {
    JNAData1 INSTANCE = (JNAData1) Native.load("msprintdata1", JNAData1.class);
    public String Data1GetPrintDataA();
    public int Data1Release();
    public int Data1PrintDataMatrix(String strData, int iSize);
    public int Data1PrintQrcode(String strData, int iLmargin, int iMside, int iRound);
    public int Data1PrintDiskbmpfile(String strPath);
    public int Data1SetNvbmp(int iNums, String strPath);
}
