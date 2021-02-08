package com.hisanweb.flutter_hardware_interaction;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Message;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSONArray;
import com.hisanweb.flutter_hardware_interaction.model.MsPrintDataModel;
import com.hisanweb.flutter_hardware_interaction.msprintsdk.PrintCmd;
import com.hisanweb.flutter_hardware_interaction.msprintsdk.UsbDriver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import io.flutter.Log;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import static android.content.ContentValues.TAG;
import static com.hisanweb.flutter_hardware_interaction.msprintsdk.PrintCmd.PrintDiskImagefile;
import static com.hisanweb.flutter_hardware_interaction.msprintsdk.PrintCmd.PrintFeedDot;
import static com.hisanweb.flutter_hardware_interaction.msprintsdk.UtilsTools.convertToBlackWhite;
import static com.hisanweb.flutter_hardware_interaction.msprintsdk.UtilsTools.data;
import static com.hisanweb.flutter_hardware_interaction.msprintsdk.UtilsTools.hexStringToBytes;
import static com.hisanweb.flutter_hardware_interaction.msprintsdk.UtilsTools.hexToByteArr;
import static com.hisanweb.flutter_hardware_interaction.msprintsdk.UtilsTools.unicodeToUtf8;

/** FlutterHardwareInteractionPlugin */
public class FlutterHardwareInteractionPlugin implements FlutterPlugin, MethodCallHandler {

  private final String TAG = "FlutterHardwareInteractionPlugin";

  private static final String METHOD_CHANNEL = "flutter/plugin/hardware_interaction";

  private static final String ACTION_USB_PERMISSION = "com.usb.sample.USB_PERMISSION";
  //
  private UsbDriver mUsbDriver;
  private UsbDevice mUsbDevice = null;

  private MethodChannel channel;

  private Context mContext;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), METHOD_CHANNEL);
    channel.setMethodCallHandler(this);

    mContext = flutterPluginBinding.getApplicationContext();

    // 初始化打印机
    mUsbDriver = new UsbDriver((UsbManager) mContext.getSystemService(Context.USB_SERVICE), mContext);
    PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
    mUsbDriver.setPermissionIntent(permissionIntent);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "systemShutdown":
        setIntentAction("wits.action.shutdown");
        result.success(true);
        break;
      case "systemReboot":
        setIntentAction("wits.action.reboot");
        result.success(true);
        break;
      case "systemHideUI":
        setIntentAction("hide.systemui");
        result.success(true);
        break;
      case "systemShowUI":
        setIntentAction("show.systemui");
        result.success(true);
        break;
      case "systemOpenGestureUI":
        setIntentAction("com.zc.open_gesture");
        result.success(true);
        break;
      case "systemCloseGestureUI":
        setIntentAction("com.zc.close_gesture");
        result.success(true);
        break;
      case "systemReadModel":
        String model = Build.MODEL;
        result.success(model);
        break;
      case "systemReadSerial":
        String serialNo = Build.SERIAL;
        result.success(serialNo);
        break;
      case "msPrinterWrite":
        String data = call.argument("data");
        Log.d(TAG,"收到Flutter传来数据："+data);
        List<MsPrintDataModel> msPrintDataModels = JSONArray.parseArray(data, MsPrintDataModel.class);
        msPrinterWrite(msPrintDataModels);
        result.success(true);
        break;
      case "msPrinterOpen":
        msPrinterOpen();
        result.success(true);
        break;
      case "msPrinterGetStatus":
        int status = msPrinterGetStatus();
        result.success(status);
        break;
      default: result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }


  private void setIntentAction(String action) {
    if (action==null) {
      return;
    }
    Intent intent = new Intent(action);
    mContext.sendBroadcast(intent);
  }

  /**
   * 美松打印机打印
   */
  @SuppressLint("NewApi")
  private void msPrinterWrite(List<MsPrintDataModel> dataModelList) {
    Log.d(TAG,dataModelList.toString());
    int iDriverCheck = usbDriverCheck();
    if (iDriverCheck == -1) {
      return;
    }
    if (iDriverCheck == 1) {
      return;
    }
    // 设置汉子模式
    mUsbDriver.write(PrintCmd.SetReadZKmode(0));
    PrintFeedDot(30);
    dataModelList.forEach(new Consumer<MsPrintDataModel>() {
      @Override
      public void accept(MsPrintDataModel dataModel) {
        String type = dataModel.getType();
        String data = dataModel.getData();
        int lineFeed = dataModel.getLineFeed();
        if (type.equals("SetHT")) {
          List<Integer> sheets = dataModel.getSheet();
          int len = sheets.size();
          byte[] bByte = new byte[len];
          for (int i = 0; i < sheets.size(); i++) {
            bByte[0] = sheets.get(i).byteValue();
          }
          mUsbDriver.write(PrintCmd.SetHTseat(bByte, len));
        } else if (type.equals("NextHT")) {
          mUsbDriver.write(PrintCmd.PrintNextHT());
        } else if (type.equals("String")) {
          mUsbDriver.write(PrintCmd.PrintString(data, lineFeed));
        } else if (type.equals("ImageBase64")) {
          Bitmap bitmap = null;
          int width, heigh;
          byte[] bytes = Base64.decode(data.split(",")[1], Base64.DEFAULT);
          bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
          bitmap = convertToBlackWhite(bitmap);
          width = bitmap.getWidth();
          heigh = bitmap.getHeight();
          int iDataLen = width * heigh;
          int[] pixels = new int[iDataLen];
          bitmap.getPixels(pixels, 0, width, 0, 0, width, heigh);
          int[] data1 = pixels;
          mUsbDriver.write(PrintDiskImagefile(data1, width, heigh));
        } else if (type.equals("QrCode") && data != null && !data.equals("")) {
          mUsbDriver.write(PrintCmd.PrintQrcode(unicodeToUtf8(data), 0, 8, 0));
        } else if (type.equals("FeedDot")) {
          mUsbDriver.write(PrintCmd.PrintFeedDot(Integer.valueOf(data)));
        } else if (type.equals("FeedLine")) {
          mUsbDriver.write(PrintCmd.PrintFeedline(Integer.valueOf(data)));
        } else {
          return;
        }
      }
    });


//    m_sbData = new StringBuilder("店号：8888          机号：100001");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("电话:0755-12345678");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    PrintFeedDot(20);
//    m_sbData = new StringBuilder("收银：01-店长");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("时间：" + data());
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("-------------------------------");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    byte[] bByte = new byte[3];
//    bByte[0] = 12;
//    bByte[1] = 18;
//    bByte[2] = 26;
//    mUsbDriver.write(PrintCmd.SetHTseat(bByte, 3));
//
//    m_sbData = new StringBuilder("代码");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("单价");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("数量");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("金额");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//
//    m_sbData = new StringBuilder("48572819");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("2.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("3.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("6.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("怡宝矿泉水");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("48572820");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("2.50");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("2.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("5.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("百事可乐(罐装)");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("-------------------------------");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("合计：");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("5.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("11.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//
//    m_sbData = new StringBuilder("优惠：");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder(" 0.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//
//    m_sbData = new StringBuilder("应付：");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("11.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//
//    m_sbData = new StringBuilder("微信支付：");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder("11.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//
//    m_sbData = new StringBuilder("找零：");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 1));
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    mUsbDriver.write(PrintCmd.PrintNextHT());
//    m_sbData = new StringBuilder(" 0.00");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//
//    m_sbData = new StringBuilder("-------------------------------");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("会员：");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("券号：");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("-------------------------------");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    PrintFeedDot(20);
//    m_sbData = new StringBuilder("手机易捷通：ejeton.com.cn ");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("客户热线：400-6088-160");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("微信号：ejeton ");
//    mUsbDriver.write(PrintCmd.PrintString(m_sbData.toString(), 0));
//    m_sbData = new StringBuilder("http://weixin.qq.com/r/R3VZQQDEi130rUQi9yBV");
//    mUsbDriver.write(PrintCmd.PrintQrcode(unicodeToUtf8(m_sbData.toString()), 10, 5, 0));
    mUsbDriver.write(PrintCmd.PrintFeedline(5));
    mUsbDriver.write(PrintCmd.PrintCutpaper(1));
  }


  /**
   * 获取usb权限
   */
  private int usbDriverCheck() {
    int iResult = -1;
    try {

      if (!mUsbDriver.isUsbPermission()) {
        UsbManager manager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        mUsbDevice = null;
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
          UsbDevice device = deviceIterator.next();
          if ((device.getProductId() == 8211 && device.getVendorId() == 1305)
                  || (device.getProductId() == 8213 && device.getVendorId() == 1305)) {
            mUsbDevice = device;
          }
        }
        if (mUsbDevice != null) {
          iResult = 1;
          if (mUsbDriver.usbAttached(mUsbDevice)) {
            if (mUsbDriver.openUsbDevice(mUsbDevice))
              iResult = 0;
          }
        }
      } else {
        if (!mUsbDriver.isConnected()) {
          if (mUsbDriver.openUsbDevice(mUsbDevice))
            iResult = 0;
        } else {
          iResult = 0;
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "usbDriverCheck:" + e.getMessage());
    }

    return iResult;
  }


  private int msPrinterGetStatus() {
    int iValue = 0;
    byte[] bRead1 = new byte[1];
    if (mUsbDriver.read(bRead1, PrintCmd.GetStatus1()) > 0) {
      iValue = PrintCmd.CheckStatus1(bRead1[0]);
      return iValue;
    }

    if (iValue == 0) {
      if (mUsbDriver.read(bRead1, PrintCmd.GetStatus2()) > 0) {
        iValue = PrintCmd.CheckStatus2(bRead1[0]);
        return iValue;
      }
    }

    if (iValue == 0) {
      if (mUsbDriver.read(bRead1, PrintCmd.GetStatus3()) > 0) {
        iValue = PrintCmd.CheckStatus3(bRead1[0]);
        return iValue;
      }
    }
    if (iValue == 0) {
      if (mUsbDriver.read(bRead1, PrintCmd.GetStatus4()) > 0) {
        iValue = PrintCmd.CheckStatus4(bRead1[0]);
        return iValue;
      }
    }
    return iValue;
  }

  private void msPrinterOpen() {
    byte[] bytes = hexToByteArr("1378");
    mUsbDriver.write(bytes);
  }
}
