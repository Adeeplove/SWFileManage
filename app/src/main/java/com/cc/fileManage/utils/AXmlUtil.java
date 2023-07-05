package com.cc.fileManage.utils;

import android.text.TextUtils;
import android.util.Log;

import com.cc.fileManage.module.manifest.data.MfFile;
import com.cc.fileManage.utils.zip.GeneralZipUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.wrapper.XmlPullParserWrapper;
import org.xmlpull.v1.wrapper.XmlPullWrapperFactory;
import org.xmlpull.v1.wrapper.XmlSerializerWrapper;
import org.xmlpull.v1.wrapper.classic.StaticXmlSerializerWrapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import brut.androlib.AndrolibException;
import brut.androlib.res.AndrolibResources;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.decoder.ARSCDecoder;
import brut.androlib.res.decoder.AXmlResourceParser;
import brut.androlib.res.decoder.AndroidManifestResourceParser;
import brut.androlib.res.decoder.ResAttrDecoder;
import brut.androlib.res.util.ExtMXSerializer;
import brut.androlib.res.util.ExtXmlSerializer;

public class AXmlUtil {

    public static void decodeAXml(String path, OutputStream outputStream) throws FileNotFoundException {
        decodeAXmlWithResources(new FileInputStream(path), outputStream, null);
    }

    public static void decodeAXmlWithResources(String path, OutputStream outputStream, String resourcesPath) throws FileNotFoundException {
        decodeAXmlWithResources(new FileInputStream(path), outputStream, TextUtils.isEmpty(resourcesPath) ? null : new FileInputStream(resourcesPath));
    }

//    public static void decodeAXmlWithResources(InputStream inputStream, OutputStream outputStream,
//                                               InputStream resourcesStream) {
//        try {
//            MfFile mfFile = new MfFile();
//            mfFile.parse(GeneralZipUtil.inputStreamToByteArray(inputStream));
//            System.out.println(mfFile.toXmlString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    // decode AndroidManifest.xml
    public static ResTable decodeAXmlWithResources(InputStream inputStream, OutputStream outputStream, InputStream resourcesStream) {
        AXmlResourceParser mParser = new AndroidManifestResourceParser();
        mParser.setAttrDecoder(new ResAttrDecoder());
        //
        ResTable mainResTable = new ResTable();
        if(resourcesStream != null) {
            try {
                // 加载arsc
                loadMainPkg(mainResTable, resourcesStream);
                // 设置
                mParser.getAttrDecoder().setCurrentPackage(mainResTable.listMainPackages().iterator().next());
            } catch (Exception e) {
                e.printStackTrace();
                mParser.getAttrDecoder().setCurrentPackage(new ResPackage(mainResTable, 0, null));
            }
        } else {
            mParser.getAttrDecoder().setCurrentPackage(new ResPackage(mainResTable, 0, null));
        }
        ///
        ExtXmlSerializer mSerial = getResXmlSerializer();
        ///
        try {
            XmlPullWrapperFactory factory = XmlPullWrapperFactory.newInstance();
            XmlPullParserWrapper par = factory.newPullParserWrapper(mParser);
            final ResTable resTable = mParser.getAttrDecoder().getCurrentPackage().getResTable();
            ////
            XmlSerializerWrapper ser = new StaticXmlSerializerWrapper(mSerial, factory) {
                boolean hideSdkInfo = false;
                boolean hidePackageInfo = false;
                ////
                public void event(XmlPullParser pp) throws XmlPullParserException, IOException {
                    int type = pp.getEventType();
                    if (type == 2) {
                        if ("manifest".equalsIgnoreCase(pp.getName())) {
                            try {
                                this.hidePackageInfo = this.parseManifest(pp);
                            } catch (AndrolibException ignored) {}
                        }
                        else if("application".equalsIgnoreCase(pp.getName())) {
                            try {
                                for(int i = 0; i < pp.getAttributeCount(); ++i) {
                                    String attr_name = pp.getAttributeName(i);
                                    if (attr_name.equalsIgnoreCase("icon")) {
                                        resTable.addSdkInfo("icon", pp.getAttributeValue(i));
                                        break;
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                        else if ("uses-sdk".equalsIgnoreCase(pp.getName())) {
                            try {
                                this.hideSdkInfo = this.parseAttr(pp);
                                if (this.hideSdkInfo) {
                                    return;
                                }
                            } catch (AndrolibException ignored) {}
                        }
                    } else {
                        if (this.hideSdkInfo && type == 3 && "uses-sdk".equalsIgnoreCase(pp.getName())) {
                            return;
                        }
                        if (this.hidePackageInfo && type == 3 && "manifest".equalsIgnoreCase(pp.getName())) {
                            super.event(pp);
                            return;
                        }
                    }
                    super.event(pp);
                }

                private boolean parseManifest(XmlPullParser pp) throws AndrolibException {
                    for(int i = 0; i < pp.getAttributeCount(); ++i) {
                        String attr_name = pp.getAttributeName(i);
                        if (attr_name.equalsIgnoreCase("package")) {
                            resTable.setPackageRenamed(pp.getAttributeValue(i));
                        } else if (attr_name.equalsIgnoreCase("versionCode")) {
                            resTable.setVersionCode(pp.getAttributeValue(i));
                        } else if (attr_name.equalsIgnoreCase("versionName")) {
                            resTable.setVersionName(pp.getAttributeValue(i));
                        }
                    }
                    return true;
                }

                private boolean parseAttr(XmlPullParser pp) throws AndrolibException {
                    for(int i = 0; i < pp.getAttributeCount(); ++i) {
                        String a_ns = "http://schemas.android.com/apk/res/android";
                        String ns = pp.getAttributeNamespace(i);
                        if (a_ns.equalsIgnoreCase(ns)) {
                            String name = pp.getAttributeName(i);
                            String value = pp.getAttributeValue(i);
                            if (name != null && value != null) {
                                if (!name.equalsIgnoreCase("minSdkVersion") && !name.equalsIgnoreCase("targetSdkVersion") && !name.equalsIgnoreCase("maxSdkVersion") && !name.equalsIgnoreCase("compileSdkVersion")) {
                                    resTable.clearSdkInfo();
                                    return false;
                                }
                                resTable.addSdkInfo(name, value);
                            }
                        } else {
                            resTable.clearSdkInfo();
                            if (i >= pp.getAttributeCount()) {
                                return false;
                            }
                        }
                    }
                    return !resTable.getAnalysisMode();
                }
            };
            par.setInput(inputStream, null);
            ser.setOutput(outputStream, null);
            ////
            while(par.nextToken() != 1) {
                ser.event(par);
            }
            ser.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mainResTable;
    }

    private static ExtMXSerializer getResXmlSerializer() {
        ExtMXSerializer serial = new ExtMXSerializer();
        serial.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "    ");
        serial.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", System.getProperty("line.separator"));
        serial.setProperty("DEFAULT_ENCODING", "utf-8");
        serial.setDisabledAttrEscape(true);
        return serial;
    }

    // loadMainPkg
    private static void loadMainPkg(ResTable resTable, InputStream resourcesStream) throws AndrolibException {
        ResPackage[] pkgs = getResources(resourcesStream, resTable);
        ResPackage pkg;
        switch(pkgs.length) {
            case 0:
                pkg = null;
                break;
            case 1:
                pkg = pkgs[0];
                break;
            case 2:
                pkg = pkgs[1];
                break;
            default:
                pkg = new AndrolibResources().selectPkgWithMostResSpecs(pkgs);
        }

        if (pkg == null) {
            throw new AndrolibException("arsc files with zero packages or no arsc file found.");
        } else {
            resTable.addPackage(pkg, true);
        }
    }

    //resources.arsc
    private static ResPackage[] getResources(InputStream resourcesStream, ResTable resTable) {
        long startTime = System.currentTimeMillis();
        ResPackage[] resPackages = new ResPackage[0];
        try (resourcesStream) {
            resPackages = ARSCDecoder.decode(resourcesStream, false, true, resTable).getPackages();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("ARSCDecoder", "decode time "+(System.currentTimeMillis() - startTime) / 1000.0f+"ms");
        return resPackages;
    }
}
